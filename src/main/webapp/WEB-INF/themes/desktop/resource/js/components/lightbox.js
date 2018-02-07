/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

/**
 *
 * DEPENDENCIES:  resource/js/vendor/underscore
 *                resource/js/vendor/jquery
 *                resource/js/vendor/jquery.panzoom
 *                resource/js/vendor/jquery.dotdotdot
 *                resource/js/vendor/jquery.mousewheel
 *                resource/js/vendor/foundation
 *
 */
var FigureLightbox = {};
(function ($) {

  FigureLightbox = {
    // All events are triggered on this container
    lbContainerSelector: '#figure-lightbox-container',

    /* internal selectors */
    lbSelector: '#figure-lightbox',
    lbTemplateSelector: '#figure-lightbox-template',
    contextTemplateSelector: '#image-context-template',
    lbCloseButtonSelector: '#figure-lightbox .lb-close',
    zoomRangeSelector: '#figure-lightbox .range-slider',
    $panZoomEl: null,
    imgData: null,
    maxPanzoomScale: 200,


    /* internal config variables */
    imgPath: WombatConfig.figurePath || 'IMG_PATH_NOT_LOADED'
  };

  FigureLightbox.insertLightboxTemplate = function () {
    var articleData = this.fetchArticleData();

    var lbTemplate = _.template($(this.lbTemplateSelector).html());
    $(this.lbContainerSelector).append(lbTemplate(articleData));
  };

  FigureLightbox.fetchArticleData = function () {
    // @TODO: Do not parse article. Fetch data via an ajax call
    var $articleContainer = $('.article');
    return {
      doi: this.imgData.doi,
      strippedDoi: this.imgData.strippedDoi,

      articleTitle: $articleContainer.find('#artTitle').text(),
      authorList: $articleContainer.find('.author-name').text(),
      figureList: this.imgList
    };
  };

  FigureLightbox.fetchImageData = function () {
    return {
      doi: this.imgData.doi,
      strippedDoi: this.imgData.strippedDoi,

      title: this.imgData.imgElement.find('.figcaption').html(),
      description: this.imgData.imgElement.find(".caption_target").nextUntil(".caption_object")
          .map(function(index, item) { return $(item).html(); }).toArray().join("\n"),
      fileSizes: {
        original: this.imgData.imgElement.find('.file-size[data-size="original"]').text(),
        large: this.imgData.imgElement.find('.file-size[data-size="large"]').text()
      }
    };
  };


  FigureLightbox.bindBehavior = function () {
    var that = this;
    // Escape key destroys and closes lightbox
    $(document).on('keyup.figure-lightbox', function (e) {
      if ($(that.lbSelector).hasClass('open')) {
        switch (e.which) {
          case 27: // esc
            that.close();
            break;
          case 37: // left
            that.prevImage();
            break;

          case 38: // up
            that.prevImage();
            break;

          case 39: // right
            that.nextImage();
            break;

          case 40: // down
            that.nextImage();
            break;

          default:
            return; // exit this handler for other keys
        }
        e.preventDefault();
      }
    });

    $(this.lbContainerSelector)
      .data('is-inited', true)
      // Bind close button
      .find(this.lbCloseButtonSelector).on('click', function () {
      that.close();
    }).end()

      // Bind buttons to change images
      .find('.change-img').on('click', function () {
      that.switchImage(this.getAttribute('data-doi'));
    }).end()


      // Bind button to show all images
      .find('.all-fig-btn').on('click', function () {
      var $figList = $('#figures-list');

      if ($figList.hasClass('figures-list-open')) {
        $figList.removeClass('figures-list-open');
      }
      else {
        $figList.addClass('figures-list-open');
      }

    }).end()

      // Bind mousewheel in figure list. Prevent image zooming
      .find('#figures-list').on('mousewheel', function (e) {
      e.stopPropagation();
    }).end()
      // Bind show in context button
      .find('#image-context').on('click', 'a.target_link', function () {
      target = $(this).attr('href');
      that.close();
    }).end()

      // Bind show in context button
      .find('#image-context').on('click', '.show-context', function () {
      that.close();
    }).end()

      // Bind next figure button
      .find('.next-fig-btn:not(.fig-btn-disabled)').on('click', function () {
      return that.nextImage();
    }).end()

      // Bind next figure button
      .find('.prev-fig-btn:not(.fig-btn-disabled)').on('click', function () {
      return that.prevImage();
    }).end()
      //Bind view-more and view less links
      .find('#image-context').on('click', '#view-more, #view-less', function () {
      that.toggleDescription();
    }).end()

      // Bind reset zoom button
      .find('.reset-zoom-btn').on('click', function () {
      return that.resetZoom();
    }).end()
      .find('#lb-zoom-min').on('click', function () {
      that.zoomOut();
    }).end()

      .find('#lb-zoom-max').on('click', function () {
      that.zoomIn();
    }).end()
      .on('image-switch.lightbox', function (e, data) {
        // Show both prev and next buttons
        var buttons = $(that.lbSelector).find('.fig-btn').show();

        if (data.index === 0) {
          buttons.filter('.prev-fig-btn').addClass('fig-btn-disabled'); // Hide prev button
        }
        else {
          buttons.filter('.prev-fig-btn').removeClass('fig-btn-disabled');
        }
        if (data.index === (that.imgList.length - 1)) {
          buttons.filter('.next-fig-btn').addClass('fig-btn-disabled'); // Hide next button
        }
        else {
          buttons.filter('.next-fig-btn').removeClass('fig-btn-disabled');
        }
        that.truncateDescription();

      });
  };

  FigureLightbox.nextImage = function () {
    var newIndex = this.getCurrentImageIndex() + 1;
    var nextImg = this.imgList[newIndex];
    if (!nextImg) {
      return false;
    }
    this.scrollDrawerToIndex(newIndex);
    this.switchImage(nextImg.getAttribute('data-doi'));
  };

  FigureLightbox.prevImage = function () {
    var newIndex = this.getCurrentImageIndex() - 1;
    var prevImg = this.imgList[newIndex];
    if (!prevImg) {
      return false;
    }
    this.scrollDrawerToIndex(newIndex);
    this.switchImage(prevImg.getAttribute('data-doi'));
  };

  FigureLightbox.scrollDrawerToIndex = function (index) {
    var $drawer = $(this.lbSelector + ' #figures-list');
    var itemTopPosition = $drawer.find('.change-img:eq(' + index + ')').position().top;

    if ($drawer.hasClass('figures-list-open')) {
      $drawer.scrollTop($drawer.scrollTop() + itemTopPosition);
    }
  };

  FigureLightbox.getCurrentImageIndex = function () {
    var that = this;
    var currentIx = null;
    this.imgList.each(function (ix, img) {
      if (img.getAttribute('data-doi') === that.imgData.strippedDoi) {
        currentIx = ix;
        return false;
      }
    });
    return currentIx;
  };

  FigureLightbox.switchImage = function (imgDoi, options) {
    var defaultOptions = {
      descriptionExpanded: this.descriptionExpanded || false
    };
    options = $.extend(defaultOptions, options);

    this.imgData = {
      doi: imgDoi
    };
    this.imgData.strippedDoi = this.imgData.doi.replace(/^info:doi\//, '');
    var currentIndex = this.getCurrentImageIndex();
    this.imgData.imgElement = $(this.imgList[currentIndex]);

    //Add active class to selected image in drawer
    $(this.lbSelector + ' #figures-list').find('.change-img-active').removeClass('change-img-active')
      .end().find('.change-img:eq(' + currentIndex + ')').addClass('change-img-active');

    // Get data to populate image context
    var imageData = this.fetchImageData();
    var templateFunctions = {
      showInContext: this.showInContext
    };
    var templateData = $.extend(imageData, templateFunctions, options);
    var lbTemplate = _.template($(this.contextTemplateSelector).html());
    // Remove actual img context
    $(this.lbSelector + ' #image-context').children().remove().end()
      // Append new img context
      .append(lbTemplate(templateData))
      //Add selector for links within captions
      .find('#figure-description-wrapper a[href^="#"]').addClass('target_link');
    if (this.descriptionExpanded) {
      this.retractDescription();
    }
    this.renderImg(this.imgData.doi);

    if (!this.descriptionExpanded) {
      this.truncateDescription();
    }

    $(this.lbContainerSelector).trigger('image-switch.lightbox', {
      index: currentIndex,
      element: this.imgData.imgElement
    });
  };

  FigureLightbox.truncateDescription = function () {
    var $viewMoreWrapper = $(this.lbSelector + ' #view-more-wrapper');
    //if (!$viewMoreWrapper.data('is-truncated')) {
    //  if ($viewMoreWrapper.find('img').length > 0) {
    //    // Workaround: If description has inline images reduce
    //    // the height of the container to avoid hiding the show
    //    // more button when the images are rendered and ocuppy more space
    //    $viewMoreWrapper.css({ maxHeight: function( index, value ) {
    //      return parseFloat( value ) * 3/4;
    //    }});
    //  }
    $viewMoreWrapper.dotdotdot({after: '#view-more', watch: true}).data('is-truncated', true);


  };

  FigureLightbox.toggleDescription = function () {
    if (this.descriptionExpanded) {
      this.retractDescription();
    } else {
      this.expandDescription();
    }
  };

  FigureLightbox.expandDescription = function () {

    this.descriptionExpanded = true;


    var animate_fast = 300;
    var animate_slow = 500;

    $('#view-more-wrapper,#show-context-container, #download-buttons').stop(true, true).fadeOut(animate_fast, function () {
      $('#view-less-wrapper').fadeIn(animate_slow);

      $('#image-context').addClass('full-display');

    });

    this.descriptionExpanded = true;

  };

  FigureLightbox.retractDescription = function () {
    var that = this;

    var animate_fast = 300;
    $('#view-less-wrapper').stop(true, true).fadeOut(animate_fast, function () {
      $('#image-context').removeClass('full-display');

       //TODO: use truncate description instead
      $('#view-more-wrapper,#show-context-container, #download-buttons').fadeIn(animate_fast);
      $('#view-less-wrapper').trigger("update");
    });
    this.descriptionExpanded = false;
  };

  FigureLightbox.isInited = function () {
    return $(this.lbContainerSelector).data('is-inited');
  };

  FigureLightbox.loadImage = function (lbContainer, imgDoi, cb) {
    $(this.lbContainerSelector).trigger('opened.lightbox');
    this.lbContainerSelector = lbContainer || this.lbContainerSelector;

    this.imgData = {
      doi: imgDoi
    };
    this.imgData.strippedDoi = this.imgData.doi.replace(/^info:doi\//, '');

    if (!this.isInited()) {
      this.imgList = $('.figure');
      this.insertLightboxTemplate();
      this.bindBehavior();
    }
    $(this.lbSelector)
      .foundation('reveal', 'open');
    this.switchImage(this.imgData.doi);

    if (typeof cb === 'function') {
      cb();
    }
  };

  FigureLightbox.renderImg = function (imgDoi) {
    var that = this;
    this.showLoader();
    var $image = $(this.lbSelector).find('img.main-lightbox-image')
      .attr('src', this.buildImgUrl(imgDoi))
      .one('load', function () {
        // Reset to original zoom
        that.resetZoom();
        that.hideLoader();
      });
    this.panZoom($image);

    // Reinitialize sliders
    $(document).foundation('slider', 'reflow');
  };

  FigureLightbox.showLoader = function () {
    $(this.lbSelector).find('.loader').addClass('showing');
  };

  FigureLightbox.hideLoader = function () {
    $(this.lbSelector).find('.loader').removeClass('showing');
  };

  FigureLightbox.close = function () {
    this.destroy();
    $(this.lbSelector).foundation('reveal', 'close');
    $(this.lbContainerSelector).trigger('closed.lightbox');
  };

  FigureLightbox.showInContext = function (imgDoi) {
    imgDoi = imgDoi.split('/');
    imgDoi = imgDoi[1].slice(8);
    imgDoi = imgDoi.replace(/\./g, '-');
    return '#' + imgDoi;
  };

  FigureLightbox.panZoom = function ($image) {
    var that = this;

    this.$panZoomEl = $image.panzoom({
      contain: false,
      minScale: 1,
      maxScale: that.maxPanzoomScale/20,
    });

    /* Bind panzoom and slider to mutually control each other */
    this.bindPanZoomToSlider();
    this.bindSliderToPanZoom();

    this.$panZoomEl.parent().off('mousewheel').on('mousewheel', function (e) {
      e.preventDefault();
      $(that.lbContainerSelector).trigger('mousewheel-zoom.lightbox', e);
      var delta = e.delta || e.originalEvent.wheelDelta;
      var zoomOut = delta ? delta < 0 : e.originalEvent.deltaY > 0;
      that.zoom(zoomOut, e);
    });
  };

  FigureLightbox.zoomIn = function () {
    this.zoom(false);
  };

  FigureLightbox.zoomOut = function () {
    this.zoom(true);
  };

  FigureLightbox.resetZoom = function () {
    this.$panZoomEl.panzoom('reset', false);
  };

  FigureLightbox.calculateViewportDimensions = function () {
    var imageContainerHeight = $(this.lbSelector).find('.img-container').height();
    var footerHeight = $(this.lbSelector).find('#lightbox-footer').height();
    var headerHeight = $(this.lbSelector).find('.lb-header').height();

    return {
      width: $(this.lbSelector).find('.img-container').width(),
      height: imageContainerHeight - headerHeight - footerHeight
    };
  };

  //Calculate the focal point in the middle of the parent to keep always the visible part in center
  FigureLightbox.calculateFocalPoint = function () {
    var $imageContainer = $(this.lbSelector).find('.img-container');
    var focus = {
      clientX: $imageContainer.width() / 2,
      clientY: $imageContainer.height() / 2
    };

    return focus;
  };

  FigureLightbox.zoom = function (zoomOut) {
    zoomOut = zoomOut || false;
    var focal = this.calculateFocalPoint();
    this.$panZoomEl.panzoom('zoom', zoomOut, {
      increment: 0.05,
      animate: false,
      focal: focal
    });
  };

  //Calculate the initial position in the center of the viewport
  FigureLightbox.calculateImageInitialPosition = function () {
    var viewportDimensions = this.calculateViewportDimensions();
    var imageHeight = this.$panZoomEl.height();
    var imageWidth = this.$panZoomEl.width();
    var imageTopPosition = (viewportDimensions.height - imageHeight) / 2;
    var imageLeftPosition = (viewportDimensions.width - imageWidth) / 2;

    var panzoomInstance = this.$panZoomEl.panzoom('instance');
    var matrix = panzoomInstance.getMatrix();
    matrix[4] = imageLeftPosition;
    matrix[5] = imageTopPosition;
    panzoomInstance.setMatrix(matrix);
  };

  FigureLightbox.bindPanZoomToSlider = function () {
    var that = this;
    var panzoomInstance = that.$panZoomEl.panzoom('instance');
    $(this.zoomRangeSelector).off('change.fndtn.slider').on('change.fndtn.slider', function () {
      // If values differ, change them
      var matrix = panzoomInstance.getMatrix();
      //Divide the slider value by 20 to keep the increment in 0.005
      var newSliderValue = parseFloat(this.getAttribute('data-slider') / 20);
      if (matrix[0] !== newSliderValue || matrix[3] !== newSliderValue) {
        $(that.lbContainerSelector).trigger('slider-zoom.lightbox');

        var zoomOut = false;
        var increment = 0;
        var focal = that.calculateFocalPoint();

        if (newSliderValue > matrix[3]) {
          increment = newSliderValue - matrix[3];
        }
        else {
          zoomOut = true;
          increment = matrix[3] - newSliderValue;
        }

        that.$panZoomEl.panzoom('zoom', zoomOut, {
          increment: increment,
          animate: false,
          focal: focal
        });
      }
    });
  };

  FigureLightbox.bindSliderToPanZoom = function () {
    var that = this;
    this.$panZoomEl.off('panzoomzoom').on('panzoomzoom', function (e, panzoom, scale) {
      //Multiply the scale value by 20 to keep proportional to the slider range
      $(that.zoomRangeSelector).foundation('slider', 'set_value', scale * 20);
      // Bug in foundation unbinds after set_value. Workaround: rebind everytime
      that.bindPanZoomToSlider();
    });

    this.$panZoomEl.off('panzoomreset').on('panzoomreset', function (e) {
      $(that.zoomRangeSelector).foundation('slider', 'set_value', 20);
      // Bug in foundation unbinds after set_value. Workaround: rebind everytime
      that.bindPanZoomToSlider();
      //Centers the image in the viewport everytime the panzoom resets
      that.calculateImageInitialPosition();
    });
  };

  FigureLightbox.buildImgUrl = function (imgDoi, options) {
    var defaultOptions = {
      path: this.imgPath,
      size: 'large'
    };
    options = _.extend(defaultOptions, options);
    var sizeParameterString = options.path.indexOf("?") === -1 ? '?size=' : '&size=';
    return options.path + sizeParameterString + options.size + '&id=' + imgDoi;
  };

  FigureLightbox.destroy = function () {
    // @TODO: Check when to destroy modal with images,
    ///add destroy when opening in other pages.
    /*    $(this.lbContainerSelector)
     // Unbind close button
     .find(this.lbCloseButtonSelector).off('click').end()
     // Unbind buttons to change images
     .find('.change-img').off('click').end()
     // Unbind button to show all images
     .find('.all-fig-btn').off('click').end()
     .off('image-switch.lightbox');
     this.$panZoomEl.panzoom('destroy');
     */
  };

})(jQuery);
