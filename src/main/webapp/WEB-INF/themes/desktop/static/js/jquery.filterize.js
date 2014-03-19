/*!
 * jQuery.filterize v1.0.0
 * https://github.com/digitalpulp/jquery-filterize
 *
 * Copyright 2013 Justin Blecher / Digital Pulp, Inc.
 * Released under the MIT license.
 * http://jquery.org/license
 *
 * Date: 2013-07-25
 */

;(function($) {

  // "global" counter (integer) for the guid generation function below
  var guid = 0;

  // simple guid-like utlity function that assembles a prefix plus an integer 
  // separated by a hyphen. this allows for 1) each instantiation of filterize 
  // to remain distinct and to not interfere with each other, and 2) to have 
  // each element (and the accompanying filter) that is replaced have 
  // distinct IDs.
  function generateGuid(prefix) {
    return prefix + "-" + (guid++);
  }


  // The following four functions were ripped from Modernizr.js and slightly 
  // renamed. see <http://modernizr.com>. we need them here because we depend 
  // on feature detection for deciding which filter implementation to use.
  var ns = {'svg': 'http://www.w3.org/2000/svg'};

  function testForSVG() {
      return !!document.createElementNS && !!document.createElementNS(ns.svg, 'svg').createSVGRect;
  }

  function testForInlineSVG() {
    var div = document.createElement('div');
    div.innerHTML = '<svg/>';
    return (div.firstChild && div.firstChild.namespaceURI) == ns.svg;
  }

  function testForSVGFilters(){
    var result = false;
    try {
      result = typeof SVGFEColorMatrixElement !== undefined &&
               SVGFEColorMatrixElement.SVG_FECOLORMATRIX_TYPE_SATURATE == 2;
    }
    catch(e) {}
    return result;
  }

  function testForCanvas() {
    var elem = document.createElement('canvas');
    return !!(elem.getContext && elem.getContext('2d'));
  }

  // ---------------------------------------------------------------------

  $.fn.extend({
    filterize: function(options) {

      // run some tests for the features we support and store results for later use
      var svg_test = testForSVG() && testForInlineSVG() && testForSVGFilters();
      var canvas_test = testForCanvas();

      // stores the name of the implementation we end up using. defaults to 
      // "none". possible values: "SVG", "Canvas"
      var implementation = "none";

      // bail out early if the browser is not capable
      if (!(svg_test || canvas_test)) {
        // console.log("bailing... browser not capable!");
        return;
      }

      var defaults = {
        // a base class that all elements have to style them all the same
        replacement_class_base    : 'filterize-replacement',

        // here lies the actual filter transform. anything that goes in a 
        // <filter> tag can go here... sorta.
        // 
        // FIXME: grok the filter stuff better!
        // 
        // filter examples:
        // 
        //  * http://srufaculty.sru.edu/david.dailey/svg/SVGOpen2010/Filters2.htm
        //  * http://ie.microsoft.com/testdrive/graphics/hands-on-css3/hands-on_svg-filter-effects.htm
        // 
        svg_filter        : "<feColorMatrix type='saturate' values='0' />",
        
        // function to transform pixels for canvas implementation. data 
        // argument is 'getImageData().data'; no need to return it. default 
        // function is grayscale.
        // 
        // NOTE: canvas functionality can be disabled via setting this option 
        // to a boolean false.
        canvas_function   : function(data, width, height) {
          // iterate over the pixel grid
          for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
              // compute index offset for pixel data, accocunting for four channels (RGBA)
              var i = (y * 4) * width + (x * 4);

              // compute an avg value (simple grayscale)
              var avg = (data[i] + data[i + 1] + data[i + 2]) / 3;

              // assign the avg value to the all the channels
              data[i] = data[i + 1] = data[i + 2] = avg;
            }
          }
        },

        // what tag to wrap the IMG tag with
        // wrapping_tag      : 'span', // or 'div'
        wrapping_tag      : 'div', // or 'div'

        // the class of the wrapping element
        wrap_class        : 'filterize-wrapper',

        // the inline CSS populated in the above tag
        // FIXME: does this just go in a CSS file instead? pro: less crap 
        // here. con: requires user to add rules to CSS file.
        wrapping_style    : 'position: relative; display: inline-block; overflow: hidden; ', // trailing space

        // callback function to execute after transformation completes
        callback          : null
      };

      // define a instance-specific class name to style all the images of this
      // instance the same way
      // 
      // NOTE: this must be run after the object is defined since we need to 
      // use a property of the object as a parameter to a function
      defaults.replacement_class = generateGuid(defaults.replacement_class_base);


      // allow defaults to be overriden
      options = $.extend(defaults, options);

      // replaces (wraps) the IMG with an new element and appends replacement 
      // element as a sibling node to the IMG. called for each image that is 
      // replaced.
      function filterize(img_el) {
        // pull the required attrs from the target IMG element
        var width  = img_el.width();
        var height = img_el.height();
        var src    = img_el[0].src;

        // console.log("filterizing " + src + " [" + width + "x" + height + "]");

        // holds the replacement element/markup
        var replacement;

        // generate the replacement using the appropriate implementation
        if (svg_test) {
          replacement = generateSVGMarkup(src, width, height);
          implementation = "SVG";

        // only execute the canvas fallback if it has not been disabled
        } else if (options.canvas_function !== false) {
          replacement = generateCanvasElement(src, width, height);
          implementation = "Canvas";
        }

        // wrap the existing img element in a new tag that occupies the same 
        // position/dimensions in the DOM
        // 
        // note: the original image is also set as the background of this
        // element, which allows us to composite the original and the 
        // filtered image (or just hide the filtered image on hover if 
        // desired).
        img_el.wrap('<' + options.wrapping_tag + ' \
          style="' + options.wrapping_style + 'width: ' + width + 'px; height: ' + height + 'px" \
          class="'+ options.wrap_class +'" />');
        
        // append the newly created SVG markup to the DOM as a parent of the 
        // newly created wrapping element
        img_el.parent().append(replacement);

        // signal we're done by adding a helper class. (so that when we're 
        // incomplete, images can be hidden.)
        // console.log("showing image " + src);
        img_el.parent().addClass('complete');
      }

      function generateSVGMarkup(src, width, height) {
        // generate some unique IDs for this replacement
        var filter_id = generateGuid("filterize-feColorMatrix");
        var svg_root_id = generateGuid("filterize-root");

        // generate the inline SVG markup to replace the IMG element.
        // based on Karl Horky's work <http://jsfiddle.net/KDtAX/487/>
        var svg_markup = [
          "<svg xmlns='http://www.w3.org/2000/svg' \
                id='" + svg_root_id + "' \
                class='" + options.replacement_class_base + " " + options.replacement_class + "' \
                viewBox='0 0 " + width + " " + height + "' \
                width='" + width + "' \
                height='" + height + "' \
          >",
            "<defs>",
              "<filter id='" + filter_id + "'>",
                options.svg_filter,
              "</filter>",
            "</defs>",
            "<image filter='url(&quot;#" + filter_id + "&quot;)' \
                    x='0' y='0'' \
                    width='" + width + "' \
                    height='" + height + "' \
                    xmlns:xlink='http://www.w3.org/1999/xlink' \
                    xlink:href='" + src + "' />",
          "</svg>"
        ].join("\n");

        return svg_markup;
      }

      function generateCanvasElement(src, width, height) {
        // boilerplate canvas setup
        var canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height; 
        // canvas.className = options.replacement_class_base + " " + options.replacement_class;
        var context = canvas.getContext('2d');

        // create a new image with the same data as the existing image
        var canvas_img = new Image();
        canvas_img.src = src;

        // draw the image on the canvas, specifying the width and height to 
        // ensure the image gets drawn at the effective CSS width/height.
        context.drawImage(canvas_img, 0, 0, width, height); 

        // get the pixel data from the canvas. warnings: 
        // 1) this is slow
        // 2) getImageData adheres to cross-domain polcies so this only works
        //    on  images that originate from the same domain as the site
        //    <http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#security-with-canvas-elements>
        // FIXME: wrap in try/catch block?
        var pixel_data = context.getImageData(0, 0, width, height);

        // process the pixel data with the specified function
        // see <http://ajaxian.com/archives/canvas-image-data-optimization-tip>
        options.canvas_function(pixel_data.data, width, height);

        // put the processed data back on the canvas
        context.putImageData(pixel_data, 0, 0, 0, 0, width, height);

        return canvas;
      }



      var $el = this;

      // iterate over the passed-in collection
      $el.each(function(index, el) {
        // assume we've got an IMG element here
        // FIXME: what if we don't? throw an error? bail?
        var $img = $(el);

        // execute the filterize when the images are actually loaded, one by
        // one. we need to execute after the images load because we need the 
        // image dimensions for SVG/Canvas iplementation. imagesLoaded seems
        // to do the most reliable job of firing when images load.
        // 
        // FIXME: see if we can simplify this by calling imagesLoaded on the 
        // collection ('this') while still having it execute for every image.
        // that'll at least get rid of this iterator.
        new imagesLoaded($img, $.proxy(filterize, null, $img));
      });


      // since we're here, all went well, so we can call our optional 
      // callback function now.
      // 
      // FIXME: since the use of imagesLoaded, this callback now happens 
      // immediately (it's asynchronous).
      if (typeof(options.callback) == 'function') {
        options.callback(implementation);
      }

      // return 'this' for jquery chaining purposes
      return this;

    } // filterize
  }); // fn.extend

}(jQuery));
