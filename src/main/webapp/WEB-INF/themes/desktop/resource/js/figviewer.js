/*
 jQuery.ThreeDots.min


 Author Jeremy Horn
 Version 1.0.10
 Date: 1/25/2009
 More: http://tpgblog.com/ThreeDots/
 compiled by http://yui.2clics.net/
 */

(function(e){e.fn.ThreeDots=function(h){var g=this;if((typeof h=="object")||(h==undefined)){e.fn.ThreeDots.the_selected=this;var g=e.fn.ThreeDots.update(h)}return g};e.fn.ThreeDots.update=function(u){var k,t=null;var m,j,s,q,o;var l,i;var r,h,n;if((typeof u=="object")||(u==undefined)){e.fn.ThreeDots.c_settings=e.extend({},e.fn.ThreeDots.settings,u);var p=e.fn.ThreeDots.c_settings.max_rows;if(p<1){return e.fn.ThreeDots.the_selected}var g=false;jQuery.each(e.fn.ThreeDots.c_settings.valid_delimiters,function(v,w){if(((new String(w)).length==1)){g=true}});if(g==false){return e.fn.ThreeDots.the_selected}e.fn.ThreeDots.the_selected.each(function(){k=e(this);if(e(k).children("."+e.fn.ThreeDots.c_settings.text_span_class).length==0){return true}l=e(k).children("."+e.fn.ThreeDots.c_settings.text_span_class).get(0);var y=a(k,true);var x=e(l).text();d(k,l,y);var v=e(l).text();if((h=e(k).attr("threedots"))!=undefined){e(l).text(h);e(k).children("."+e.fn.ThreeDots.c_settings.e_span_class).remove()}r=e(l).text();if(r.length<=0){r=""}e(k).attr("threedots",x);if(a(k,y)>p){curr_ellipsis=e(k).append('<span style="white-space:nowrap" class="'+e.fn.ThreeDots.c_settings.e_span_class+'">'+e.fn.ThreeDots.c_settings.ellipsis_string+"</span>");while(a(k,y)>p){i=b(e(l).text());e(l).text(i.updated_string);t=i.word;n=i.del;if(n==null){break}}if(t!=null){var w=c(k,y);if((a(k,y)<=p-1)||(w)||(!e.fn.ThreeDots.c_settings.whole_word)){r=e(l).text();if(i.del!=null){e(l).text(r+n)}if(a(k,y)>p){e(l).text(r)}else{e(l).text(e(l).text()+t);if((a(k,y)>p+1)||(!e.fn.ThreeDots.c_settings.whole_word)||(v==t)||w){while((a(k,y)>p)){if(e(l).text().length>0){e(l).text(e(l).text().substr(0,e(l).text().length-1))}else{break}}}}}}}if(x==e(e(k).children("."+e.fn.ThreeDots.c_settings.text_span_class).get(0)).text()){e(k).children("."+e.fn.ThreeDots.c_settings.e_span_class).remove()}else{if((e(k).children("."+e.fn.ThreeDots.c_settings.e_span_class)).length>0){if(e.fn.ThreeDots.c_settings.alt_text_t){e(k).children("."+e.fn.ThreeDots.c_settings.text_span_class).attr("title",x)}if(e.fn.ThreeDots.c_settings.alt_text_e){e(k).children("."+e.fn.ThreeDots.c_settings.e_span_class).attr("title",x)}}}})}return e.fn.ThreeDots.the_selected};e.fn.ThreeDots.settings={valid_delimiters:[" ",",","."],ellipsis_string:"...",max_rows:2,text_span_class:"ellipsis_text",e_span_class:"threedots_ellipsis",whole_word:true,allow_dangle:false,alt_text_e:false,alt_text_t:false};function c(k,h){if(e.fn.ThreeDots.c_settings.allow_dangle==true){return false}var l=e(k).children("."+e.fn.ThreeDots.c_settings.e_span_class).get(0);var g=e(l).css("display");var i=a(k,h);e(l).css("display","none");var j=a(k,h);e(l).css("display",g);if(i>j){return true}else{return false}}function a(i,j){var g=typeof j;if((g=="object")||(g==undefined)){return e(i).height()/j.lh}else{if(g=="boolean"){var h=f(e(i));return{lh:h}}}}function b(k){var j;var i=e.fn.ThreeDots.c_settings.valid_delimiters;k=jQuery.trim(k);var g=-1;var h=null;var l=null;jQuery.each(i,function(m,o){if(((new String(o)).length!=1)||(o==null)){return false}var n=k.lastIndexOf(o);if(n!=-1){if(n>g){g=n;h=k.substring(g+1);l=o}}});if(g>0){return{updated_string:jQuery.trim(k.substring(0,g)),word:h,del:l}}else{return{updated_string:"",word:jQuery.trim(k),del:null}}}function f(h){e(h).append("<div id='temp_ellipsis_div' style='position:absolute; visibility:hidden'>H</div>");var g=e("#temp_ellipsis_div").height();e("#temp_ellipsis_div").remove();return g}function d(k,l,m){var q=e(l).text();var i=q;var o=e.fn.ThreeDots.c_settings.max_rows;var h,g,n,r,j;var p;if(a(k,m)<=o){return}else{p=0;curr_length=i.length;curr_middle=Math.floor((curr_length-p)/2);h=q.substring(p,p+curr_middle);g=q.substring(p+curr_middle);while(curr_middle!=0){e(l).text(h);if(a(k,m)<=(o)){j=Math.floor(g.length/2);n=g.substring(0,j);p=h.length;i=h+n;curr_length=i.length;e(l).text(i)}else{i=h;curr_length=i.length}curr_middle=Math.floor((curr_length-p)/2);h=q.substring(0,p+curr_middle);g=q.substring(p+curr_middle)}}}})(jQuery);



/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var $FV = {};
var $FVPending = false;

var FigViewerInit = function(doi, ref, state, external_page) {
  // allow only one instance of FigViewerInit to be pending.
  if ($FVPending) {
    return;
  }
  $FVPending = true;

  $FV = $('<div id="fig-viewer" />');
  $FV.cont = $('<div id="fig-viewer-content" />');
  $FV.append($FV.cont);
  $FV.figs_ref = ref; // reference for specific figure, if null, defaults to first figure
  $FV.txt_expanded = false; // figure descriptions are closed
  $FV.thmbs_vis = false; // figure thumbnails are hidden
  $FV.external_page = external_page ? true : false;

  var rerunMathjax = function() {
    // rerun mathjax
    try {
      var domelem = $FV[0];
      if (domelem && typeof MathJax != "undefined") {
        MathJax.Hub.Queue(["Typeset",MathJax.Hub,domelem]);
      }
    } catch (e) {
      // ignore
    }
  };

  var loadJSON = function() {
    var apiurl = '/article/lightbox.action?uri=' + doi;

    $.ajax({
      url:apiurl,
      dataFilter:function (data, type) {
        return data.replace(/(^\/\*|\*\/$)/g, '');
      },
      dataType:'json',
      error: function (jqXHR, textStatus, errorThrown) {
        console.log(errorThrown);
        $FVPending = false;
      },
      success:function (data) {

        $FV.url = data.URL;

        FVBuildHdr(data.articleTitle, data.authors, data.uri);
        FVBuildFigs(data);

        // from article tab where references,abstract and metadata exists, no need to fetch
        // them again from the server.
        if (typeof selected_tab != "undefined" && selected_tab == "article") {
          $FVPending = false;
          FVBuildAbs(doi, $(".article .abstract"), $(".article .articleinfo"));
          FVBuildRefs($(".article .references"));
          displayModal();
          rerunMathjax();
        }
        else {
          var articleUrl = "/article/" + doi;
          console.log("fetch full article: " + articleUrl);
          $.ajax({url: articleUrl, success: function(fullArticleHtml) {
            $FVPending = false;
            var article = $(fullArticleHtml);
            FVBuildAbs(doi, article.find(".article .abstract"), article.find(".article .articleinfo"));
            FVBuildRefs(article.find(".article .references"));
            displayModal();
            rerunMathjax();
          }, error: function() {
            $FVPending = false;
          }});
        }
      }
    });
  };

  var displayModal = function () {

    if(typeof(_gaq) !== 'undefined'){
      _gaq.push(['_trackEvent',"Lightbox", "Display Modal", ""]);
    }

    $('body').append($FV);
    // add helper class to body element to prevent page scrolling when modal is open
    $('body').addClass('modal-active');

    FVSize();
    FVDisplayPane(state);

    // debounce resize event
    var resizeDelay;
    $win.bind('resize.modal', function() {
      clearTimeout(resizeDelay);
      resizeDelay = setTimeout(function() {
        FVSize();
      }, 100);
    });
  };

  $(this).bind('keydown', function (e) {
    if (e.which == 37 || e.which == 38) {
      if ($FV.thumbs.active.prev().length) {
        t = $FV.thumbs.active.prev()
        FVChangeSlide(t);
      }
      return false;
    }

    if (e.which == 39 || e.which == 40) {
      if ($FV.thumbs.active.next().length) {
        t = $FV.thumbs.active.next()
        FVChangeSlide(t);
      }
      return false;
    }
  });

  loadJSON();

};

var FVSize = function () {
  var win_h = $win.height()
  var frame_h = parseInt($FV.cont.css('marginTop')) + parseInt($FV.cont.css('marginTop'));
  var hdr_h = $FV.hdr.innerHeight()
  var fig_h = win_h - frame_h - $FV.slides.eq(0).find('div.data').innerHeight() - hdr_h;
  $FV.cont.css('height', win_h - frame_h);
  $FV.figs.css('height', fig_h - 4); // added border of 2px
  $FV.thumbs_cont.css('height', fig_h - parseInt($FV.thumbs_el.css('paddingTop')));
  $FV.abst_pane.css('height', win_h - frame_h - hdr_h);
  $FV.refs_pane.css('height', win_h - frame_h - hdr_h);
  if ($FV.thmbs_vis) {
    FVThumbPos($FV.thumbs.active);
  }
};

// build header elements
var FVBuildHdr = function(title, authors, articleDoi) {
  $FV.hdr = $('<div class="header" />');
  if ($FV.external_page) {
    var articleLink = "http://dx.plos.org/" + articleDoi.replace("info:doi/", "");
    var h1 = '<h1><a href="' + articleLink + '">' + title + '</a></h1>';
  } else {
    var h1 = '<h1>' + title + '</h1>';
  }
  var authorList = $('<ul class="authors"></ul>');
  $.each(authors, function (index, author) {
    $('<li>' + author + '</li>').appendTo(authorList);
  });

  var nav = '<ul class="nav">'
    + '<li class="abst">Abstract</li>'
    + '<li class="figs">Figures</li>'
    + '<li class="refs">References</li>'
    + '</ul>'

  $FV.hdr.append(h1);
  $FV.hdr.append(authorList);
  $FV.hdr.append(nav);

  $FV.hdr.find('.nav li').on('click', function() {
    FVDisplayPane(this.className);
  });

  $close = $('<span class="close" title="close" />').on('click', function() {
    FVClose();
  });
  $FV.hdr.append($close);

  $FV.cont.prepend($FV.hdr);
}

// build figures pane
var FVBuildFigs = function(data) {
  $FV.figs_pane = $('<div id="fig-viewer-figs" class="pane" />');
  $FV.thumbs_el = $('<div id="fig-viewer-thmbs" />');
  $FV.thumbs_cont = $('<div id="fig-viewer-thmbs-content" />');
  $FV.controls_el = $('<div id="fig-viewer-controls" />');
  $FV.slides_el = $('<div id="fig-viewer-slides" />');
  $FV.staging_el = $('<div class="staging" />'); // hidden container for loading large images
  $FV.figs_set = [];  // all figures array
  var path = '/article/fetchObject.action?uri='

  var showInContext = function (uri) {
    uri = uri.split('/');
    uri = uri.pop();
    uri = uri.split('.');
    uri = uri.slice(1);
    uri = uri.join('-');
    return '#' + uri;
  };

  $.each(data.secondaryObjects, function () {
    var title_txt = (this.title ? '<b>' +this.title + ':</b> ' : '') + this.transformedCaptionTitle;

    var image_title = this.title + ' ' + this.plainCaptionTitle;

    var $thmb = $('<div class="thmb"' + ' data-uri="' + this.uri + '"><div class="thmb-wrap"><img src="' + path + this.uri + '&representation=PNG_I' + '" alt="' + image_title + '" title="' + image_title + '"></div></div>').on('click', function () {
      FVChangeSlide($(this));
    })
    $FV.thumbs_cont.append($thmb);
    var slide = $('<div class="slide" />');
    var data = $('<div class="data" />');
    var txt = $('<div class="txt" />');
    var content = $('<div class="content" />');
    var txt_more = $('<div class="text-more" />');
    var title = '<div class="title">' + title_txt + '</div>';
    var toggleLess = $('<div class="toggle less" title="view less" />');
    var context_hash = showInContext(this.uri);
    if ($FV.external_page) { // the image is on another page
      context_hash = '/article/' + $FV.url + context_hash;
    }
    var doi = '<p>' + this.doi.replace('info:doi/','doi:') + '</p>';

    // we're not building the images here, just divs with the src of medium & large verisons in data attributes
    var $fig = $('<div class="figure" data-img-src="' + path + this.uri + '&representation=' + this.repMedium + '" data-img-lg-src="' + path + this.uri + '&representation=' + this.repLarge + '" data-img-txt="' + image_title + '"></div>');

    $fig.data('state', 0) // track image loading state of figure
      .data('off-top', 0)
      .data('off-left', 0);
    $FV.figs_set.push($fig);

    var staging = '<div class="staging" />'; // hidden container for loading large image

    var dl = '<div class="download">'
      + '<h3>Download:</h3>'
      + '<div class="item"><a href="' + "/article/" + this.uri + "/powerpoint" + '" title="PowerPoint slide"><span class="btn">PPT</span></a></div>'
      + '<div class="item"><a href="' + "/article/" + this.uri + "/largerimage" + '" title="large image"><span class="btn">PNG</span><span class="size">' + convertToBytes(this.sizeLarge) + '</span></a></div>'
      + '<div class="item"><a href="' + "/article/" + this.uri + "/originalimage" + '" title="original image"><span class="btn">TIFF</span><span class="size">' + convertToBytes(this.sizeTiff) + '</span></a></div>'
      + '</div>'

    var context_lnk = '<a class="btn lnk_context" href="' + context_hash + '" onclick="FVClose();">Show in Context</a>';

    slide.append($fig);
    slide.append(staging);
    content.append(title);

    if (!/^\s*$/.test(this.transformedDescription)) {
      txt_more.append('<div class="desc">' + this.transformedDescription + '</div>');
    }

    txt_more.append(doi);
    txt.append(toggleLess);
    content.append(txt_more);
    txt.append(content);
    data.append(txt);
    data.append(context_lnk);
    data.append(dl);
    slide.append(data);
    $FV.slides_el.append(slide);
  });

  // thumbnail close button
  $('<span class="btn-thmb-close" title="close" />').on('click',function() {
    $FV.figs_pane.toggleClass('thmbs-vis');
    $FV.thmbs_vis = $FV.thmbs_vis ? false : true;
  }).appendTo($FV.thumbs_el);
  $FV.thumbs_el.append($FV.thumbs_cont);

  $FV.slides = $FV.slides_el.find('div.slide'); // all slides
  $FV.figs = $FV.slides_el.find('div.figure'); // all figures
  $FV.thumbs = $FV.thumbs_el.find('div.thmb'); // all thumbnails
  $FV.thumbs.active = null; // used to track active thumb & figure

  // figures controls
  $('<span class="fig-btn thmb-btn"><i class="icn"></i> All Figures</span>').on('click',function() {
    $FV.figs_pane.toggleClass('thmbs-vis');
    $FV.thmbs_vis = $FV.thmbs_vis ? false : true;
    FVThumbPos($FV.thumbs.active);
  }).appendTo($FV.controls_el);
  $FV.nxt = $('<span class="fig-btn next"><i class="icn"></i> Next</span>').on('click',function() {
    FVChangeSlide($FV.thumbs.active.next());
  }).appendTo($FV.controls_el);
  $FV.prv = $('<span class="fig-btn prev"><i class="icn"></i> Previous</span>').on('click',function() {
    FVChangeSlide($FV.thumbs.active.prev());
  }).appendTo($FV.controls_el);

  $FV.loading = $('<div class="loading-bar"><!--[if lte IE 8]>LOADING<![endif]--></div>').appendTo($FV.controls_el);
  $FV.zoom = $('<div id="fv-zoom" />');
  $FV.zoom.min = $('<div id="fv-zoom-min" />').appendTo($FV.zoom);
  $FV.zoom.sldr = $('<div id="fv-zoom-sldr" />').appendTo($FV.zoom);
  $FV.zoom.max = $('<div id="fv-zoom-max" />').appendTo($FV.zoom);
  $FV.controls_el.append($FV.zoom);

  $FV.figs_pane.append($FV.slides_el);
  $FV.figs_pane.append($FV.thumbs_el);
  $FV.figs_pane.append($FV.controls_el);
  $FV.figs_pane.append($FV.staging_el);

  $FV.cont.append($FV.figs_pane);

  if ($.support.touchEvents) {
    $FV.slides_el.swipe({
      swipeLeft:function(event, direction, distance, duration, fingerCount) {
        if ($FV.thumbs.active.next().length) {
          t = $FV.thumbs.active.next()
          FVChangeSlide(t);
        }
      },
      swipeRight:function(event, direction, distance, duration, fingerCount) {
        if ($FV.thumbs.active.prev().length) {
          t = $FV.thumbs.active.prev()
          FVChangeSlide(t);
        }
      },
      tap:function(event, target) {
        target.click();
      },
      threshold:25
    });
  }

}


// build abstract pane
var FVBuildAbs = function(doi, abstractText, metadata) {
  $FV.abst_pane = $('<div id="fig-viewer-abst" class="pane cf" />');
  var $abst_content = $('<div class="abstract" />');


  if (abstractText.size() == 0) {
    // There is no abstract. Go back and hide the "view abstract" button created in FVBuildHdr.
    $FV.hdr.find('li.abstract').hide();
  }
  else {
    $abst_content.html(abstractText.html());
    $abst_content.find("h2").remove();
    $abst_content.find('a[name="abstract0"]').remove();
  }

  var lnk_pdf = '<div class="fv-lnk-pdf"><a href="/article/fetchObject.action?uri=' + doi + '&representation=PDF" target="_blank" class="btn">Download: Full Article PDF Version</a></div>'
  $abst_content.append(lnk_pdf);

  var $abst_info = $('<div class="info" />');
  $abst_info.html(metadata.html());

  $FV.abst_pane.append($abst_content);
  $FV.abst_pane.append($abst_info);
  $FV.cont.append($FV.abst_pane);

};

// build references pane
var FVBuildRefs = function(references) {
  $FV.refs_pane = $('<div id="fig-viewer-refs" class="pane cf" />');
  var $refs_content = $('<ol class="references" />');
  $refs_content.html(references.html());
  $FV.refs_pane.append('<h3>References</h3>');
  $FV.refs_pane.append($refs_content);
  $FV.cont.append($FV.refs_pane);
};


// toggle between panes
var FVDisplayPane = function(pane) {
  $FV.removeClass('abst figs refs').addClass(pane);
  if (pane == 'figs') {
    if ($FV.thumbs.active == null) { // no thumb is active so this is the 1st figure displayed
      // call FVChangeSlide() via thumbnail click, to display correct figure
      if ($FV.figs_ref) { // specific figure is requested
        $FV.thumbs_cont.find('div[data-uri="' + $FV.figs_ref + '"]').trigger('click');
      } else { // default to first figure
        $FV.thumbs.eq(0).trigger('click');
      }
    } else {
      // A figure was displayed, then a different pane was selected and then user returned to the figure pane
      // If a medium or large image finished loading while the figure pane was not visible -
      // figure building would stop (it requires figure pane to be visible to access image dimensions)
      // run FVDisplayFig() again to update figure status
      FVDisplayFig($FV.thumbs.index($FV.thumbs.active));
    }
  }
};

// change figure slides functionality
var FVChangeSlide = function($thmb) {

  if(typeof(_gaq) !== 'undefined'){
    _gaq.push(['_trackEvent',"Lightbox", "Slide Changed", ""]);
  }

  if ($FV.thumbs.active !== null) { // not the initial slide
    $FV.thumbs.active.removeClass('active');
    var old_fig = $FV.figs_set[$FV.thumbs.index($FV.thumbs.active)];
    var old_img = old_fig.find('img');
    if (old_img.hasClass('ui-draggable')) { // the slide we are leaving had a drag-enabled figure, reset it
      FVDragStop(old_fig, old_fig.find('img'));
    }
  }
  $FV.thumbs.active = $thmb;
  $FV.thumbs.active.addClass('active');

  $FV.slides.hide();
  var i = $FV.thumbs.index($thmb);
  var this_sld = $FV.slides.eq(i);
  this_sld.show();
  FVDisplayFig(i);
  FVFigDescripton(this_sld);

  $FV.thumbs.active.next().length ? $FV.nxt.removeClass('invisible') : $FV.nxt.addClass('invisible');
  $FV.thumbs.active.prev().length ? $FV.prv.removeClass('invisible') : $FV.prv.addClass('invisible');
  if ($FV.thmbs_vis) { // no point updating this if you con't see it
    FVThumbPos($thmb);
  }

};

// figure descriptin
var FVFigDescripton = function(sld) {
  var $btn_less = sld.find('div.toggle.less');
  var $desc = sld.find('div.desc');
  var $title = sld.find('div.title');
  sld.find('div.tease').remove();

  var truncate = function() {

    // test to see if heading is more than 1 line using CSS class
    $title.addClass('test-height');
    var h1 = $title.height();
    $title.removeClass('test-height');
    var h2 = $title.height();

    // if heading is one line truncate teaser text to 2 lines, otherwise to 1 line
    var row;
    if (h1 == h2) {
      row = 2;
    } else {
      row = 1;
    }
    var desc_text = $desc.html();
    var $tease = $('<div class="tease" />');
    $tease.append(desc_text);

    $tease.wrapInner('<span class="ellipsis_text" />');

    $tease.insertAfter($title);
    $tease.ThreeDots({
      max_rows:row,
      ellipsis_string:'... <span class="toggle more">show more</span>'
    });
    $tease.find('span.more').click(function() {
      $FV.slides_el.addClass('txt-expand');
      $FV.txt_expanded = true;
    });
  };

  // truncation only possible if teaser is visible
  // if descriptions are open teaser is hidden

  // check display of descriptions
  if (!$FV.txt_expanded) { // landed on this slide and descriptions are hidden.
    truncate();
    $btn_less.click(function() {
      $FV.slides_el.removeClass('txt-expand');
      $FV.txt_expanded = false;
    });
  } else { // landed on this slide and descriptions are visible. 
    // truncate following description reveal  
    $btn_less.click(function() {
      $FV.slides_el.removeClass('txt-expand');
      $FV.txt_expanded = false;
      sld.find('div.tease').remove();
      truncate();
    });

  }
};



/**
 * Bring a thumbnail image into view if it's scrolled out of view.
 * @param thmb the thumbnail image to bring into view
 */
var FVThumbPos = function($thmb) {
  var index = $FV.thumbs.index($thmb);
  var thmb_h = $thmb.outerHeight(true);
  var thmb_top = index * thmb_h;
  var thmb_bot = thmb_top + thmb_h;
  var current_scroll = $FV.thumbs_cont.scrollTop();
  var thumbs_h = $FV.thumbs_cont.innerHeight();
  if (thmb_top < current_scroll) {
    // thmb is above the top of the visible area, so snap it to the top
    $FV.thumbs_cont.scrollTop(thmb_top);
  } else if (current_scroll + thumbs_h < thmb_bot) {
    // thmb is below the bottom of the visible area, so snap it to the bottom
    $FV.thumbs_cont.scrollTop(thmb_bot - thumbs_h);
  }
};


// this function checks the status of figure image building/resizing, and directs to appropriate next step
var FVDisplayFig = function(i) {
  $FV.loading.show();
  $FV.zoom.hide();
  var this_fig = $FV.figs_set[i];
  var $img = this_fig.find('img')
  var state = this_fig.data('state');
  // state of figure
  // 0 - no image loaded
  // 1 - medium image loading
  // 2 - medium image loaded, not visible, not yet resized
  // 3 - medium image visible & resized, large image in process of loading
  // 4 - medium image visible, large image loaded in hidden staging div
  // 5 - large image visible
  switch(state) {
    case 0:
      FVLoadMedImg(i)
      break;
    case 1:
      // waiting on medium image to load
      break;
    case 2:
      FVSizeImgToFit(this_fig, false);
      $img.removeClass('invisible');
      this_fig.data('state', 3);
      FVLoadLargeImg(i);
      break;
    case 3:
      // waiting on large image to load
      break;
    case 4:
      FVSwitchImg($FV.figs_set[i]);
      $FV.loading.hide();
      this_fig.data('state', 5);
      break;
    case 5:
      $FV.loading.hide();
      FVSizeImgToFit(this_fig, true);
      if (this_fig.hasClass('zoom')) {
        FVFigFunctions(this_fig);
      }
      break;
  }
};


// build medium image, when loaded - size to fit, call load large image function
var FVLoadMedImg = function(i) {
  var src = $FV.figs_set[i].data('img-src');
  var txt = $FV.figs_set[i].data('img-txt');
  var $img = $('<img src="' + src + '" title="' + txt + '" alt="' + txt + '" class="med invisible">');
  $FV.figs_set[i].append($img); // add medium image (set to hidden in css)
  $FV.figs_set[i].data('state', 1);
  $FV.figs_set[i].imagesLoaded(function() {
    $FV.figs_set[i].data('state', 2);
    if (i == $FV.thumbs.index($FV.thumbs.active) && $FV.hasClass('figs')) { // true if this slide is still visible
      FVSizeImgToFit($FV.figs_set[i], false);
      $FV.figs_set[i].find('img').removeClass('invisible');
      $FV.figs_set[i].data('state', 3);
      FVLoadLargeImg(i);
    }
  });
};


// load large images in div.staging
var FVLoadLargeImg = function(i) {
  var src = $FV.figs_set[i].data('img-lg-src');
  var txt = $FV.figs_set[i].data('img-txt');
  var $img = $('<img src="' + src + '" title="' + txt + '" alt="' + txt + '" class="lg invisible">');
  $FV.figs_set[i].next('div.staging').append($img); // load large img into 'staging' div
  $FV.figs_set[i].next('div.staging').imagesLoaded(function() {
    $FV.figs_set[i].data('state', 4);
    if (i == $FV.thumbs.index($FV.thumbs.active) && $FV.hasClass('figs')) { // true if this slide is still visible
      FVSwitchImg($FV.figs_set[i]);
      $FV.loading.hide();
      $FV.figs_set[i].data('state', 5);
    }
  });
};

// size images to fit in parent element
var FVSizeImgToFit = function(el, down_only) {
  var img = el.find('img');
  var el_h = el.height();
  var el_w = el.width();
  var i_h = img.height();
  var i_w = img.width();

  // sizes image to fit, scalling up or down, and centering
  // setting size with height
  var sizeAndCenter = function() {
    // compare aspect ratios of parent and image and set dimensions accordingly
    if (el_w / el_h > i_w / i_h) {
      img.css({'height': el_h});
      // horizontally center after resizing, (zoom uses margin values so can't use auto)
      img.css({'marginLeft' : Math.round((el_w -  img.width()) / 2), 'marginTop' : 0});
    } else {
      // calculate height to make width match parent
      img.css({'height' : Math.round(el_w * (i_h / i_w))});
      // vertically center after resizing
      img.css({'marginTop' : Math.round((el_h - img.height()) / 2), 'marginLeft' : 0});
    }
  }

  if (down_only) { // this is a large image and we don't want to scale up.
    if (el_h > el.data('img_l_h') && el_w > el.data('img_l_w')) { // native size smaller than viewport
      img.css({'marginTop' : Math.round((el_h - i_h) / 2), 'marginLeft' : Math.round((el_w - i_w) / 2)}); // center
      el.removeClass('zoom'); // too small to zoom
    } else {
      sizeAndCenter();
      el.addClass('zoom');
    }
  } else { // this is a medium image, we will scale up or down
    sizeAndCenter();
  }
}

// switch medium image with large image
var FVSwitchImg = function($fig) {
  var $img_m = $fig.find('img');
  var img_m_h = $img_m.height();
  var $img_l = $fig.next('div.staging').find('img');
  // move large image into figure div (image hidden by css)
  $fig.append($img_l);
  var img_l_h = $img_l.height();
  var img_l_w = $img_l.width();
  // store native dimensions
  $fig.data('img_l_w', img_l_w).data('img_l_h', img_l_h);
  if (img_l_h < img_m_h) { // large image smaller than resized medium image
    $img_l.css({'marginTop' : Math.round(($fig.height() - img_l_h) / 2), 'marginLeft' : Math.round(($fig.width() - img_l_w) / 2)}); // center
  } else {
    $img_l.css({'height' : img_m_h, 'marginTop' : $img_m.css('marginTop'), 'marginLeft' : $img_m.css('marginLeft')}); // match dimensions and position of medium image
    $fig.addClass('zoom'); // zoomable & draggable
  }
  $fig.html($img_l.removeClass('invisible')); // replace
  var drag_bx = $('<div class="drag-bx" />'); // insert drag containment element
  $fig.wrapInner(drag_bx);
  if ($fig.hasClass('zoom')) {
    FVFigFunctions($fig);
  }
};


// zoom & drag figure
// this is called following either
// on the visible slide, a large image that is bigger than the slide has finished loading
// OR navigating to a slide whose large image is bigger than the slide and has already loaded.
var FVFigFunctions = function($fig) {
  var $img = $fig.find('img');
  var real_h = $fig.data('img_l_h'); // native height of image
  var real_w = $fig.data('img_l_w'); // native width of image
  var img_mt = parseInt($img.css('marginTop')); // top margin of sized to fit image
  var img_ml = parseInt($img.css('marginLeft')); // left margin of sized to fit image
  var resize_h = $img.height(); // height of sized to fit image
  var drag = false; // dragging not enabled
  var $drgbx = $fig.find('div.drag-bx');

  $FV.zoom.show();
  $FV.zoom.sldr.slider({
    min: resize_h,
    max: real_h,
    value: resize_h,
    slide: function(e, ui) {
      imgResize(ui.value);
    },
    stop: function(e, ui) {
      if (!drag) {
        // enable dragging
        FVDragInit($fig, $img);
      }
      if (ui.value == resize_h) { // slider is at minimum value
        // kill drag
        FVDragStop($fig, $img);
        drag = false;
      } else {
        FVSizeDragBox($fig, $img);
        drag = true;
      }
    }
  });

  // max(+) buttton
  $FV.zoom.max.on('click', function() {
    var value = $FV.zoom.sldr.slider("value");
    value = resize_h + (real_h - resize_h) / 4 * Math.ceil((value - resize_h) * 4 / (real_h - resize_h) + 0.1);
    value = Math.min(Math.ceil(value), real_h);

    $FV.zoom.sldr.slider('value', value );
    imgResize(value);
    FVDragInit($fig, $img);
    FVSizeDragBox($fig, $img);
    drag = true;
  });

  // min(-) buttton
  $FV.zoom.min.on('click', function() {
    if (!drag) { // dragging is not enabled, so image must be zoomed in, nothing to do here
      return false;
    }
    var value = $FV.zoom.sldr.slider("value");
    value = resize_h + (real_h - resize_h) / 4 * Math.floor((value - resize_h) * 4 / (real_h - resize_h) - 0.1);
    value = Math.max(Math.floor(value), resize_h);

    $FV.zoom.sldr.slider('value', value );
    if (value <= resize_h) {
      $img.css({
        'height': value,
        'marginTop': img_mt,
        'marginLeft': img_ml
      });
      FVDragStop($fig, $img);
      drag = false;
    }
    else {
      imgResize(value);
    }
  });

  var imgResize = function(x) {
    $img.css({
      'height': x,
      'marginTop': img_mt - Math.round((x - resize_h) / 2),
      'marginLeft': img_ml - Math.round(((x - resize_h) / 2) * (real_w / real_h))
    });
  }

  $fig.on('DOMMouseScroll mousewheel', function (event) {
    var value = $FV.zoom.sldr.slider("value");
    if (event.originalEvent.wheelDeltaY < 0) {
      value = Math.min(value + 20, real_h);
      imgResize(value);
      $FV.zoom.sldr.slider('value', value );
    }
    else if (event.originalEvent.wheelDeltaY > 0) {
      value = Math.max(value - 20, resize_h);
      imgResize(value);
      $FV.zoom.sldr.slider('value', value );
    }
  });
};

var FVDragInit = function($fig, $img) {
  $img.draggable({
    containment: 'parent',
    stop: function(e, ui) {
      // FVSizeDragBox() sets top/left values to match margins of figure
      // following dragging these are no longer in sync, top/left have changed
      // storing the difference between values, (if no dragging value is 0)
      // when a figure is resized the margins are updated and then the drag box is resized
      // to calculate dimensions/position of resized drag bax, the difference in values prior to figure resize is required
      $fig.data('off-top', Math.abs(parseInt($img.css('marginTop'))) - parseInt($img.css('top')))
        .data('off-left', Math.abs(parseInt($img.css('marginLeft'))) - parseInt($img.css('left')))
    }
  });
};

var FVDragStop = function($fig, $img) {
  $img.draggable('destroy');
  // reset
  $img.css({'top' : 0, 'left' : 0,});
  $fig.find('div.drag-bx').removeAttr('style');
  $fig.data('off-top', 0)
    .data('off-left', 0);
};

// Size & position div to contain figure dragging
// adds top/left declarations to image so that image remains in same position following containing div sizing/positioning
// runs following figure resize
var FVSizeDragBox = function($fig, $img) {
  var $drgbx = $fig.find('div.drag-bx');
  var fig_h = $fig.height();
  var fig_w = $fig.width();
  var img_h = $img.height();
  var img_w = $img.width()
  var img_mt = parseInt($img.css('marginTop'));
  var img_ml = parseInt($img.css('marginLeft'));
  var img_tp = isNaN(parseInt($img.css('top'))) ? 0 : parseInt($img.css('top'));
  var img_lt = isNaN(parseInt($img.css('left'))) ? 0 : parseInt($img.css('left'));
  if (fig_h > img_h) {
    $drgbx.css({
      'top' : img_mt * -1,
      'height': fig_h + img_mt
    });
    $img.css({
      'top' : img_mt - $fig.data('off-top')
    });
  } else {
    $drgbx.css({
      'top' :  (img_h - fig_h + img_mt) * -1,
      'height': ((img_h * 2) - fig_h) + img_mt
    });
    $img.css({
      'top' : img_h - fig_h + img_mt - $fig.data('off-top')
    });
  }
  if (fig_w > img_w) {
    $drgbx.css({
      'left' : img_ml * -1,
      'width': fig_w + img_ml
    });
    $img.css({
      'left' : img_ml - $fig.data('off-left')
    });
  } else {
    $drgbx.css({
      'left' :  (img_w - fig_w + img_ml) * -1,
      'width': ((img_w * 2) - fig_w) + img_ml
    });
    $img.css({
      'left' : img_w - fig_w + img_ml - $fig.data('off-left')
    });
  }

};


// close
var FVClose = function() {
  $FV.remove();

  // remove helper class added in displayModal()
  $('body').removeClass('modal-active');

  $win.unbind('resize.modal');
  //will record the timeStamp for when the modal is closed
  if(typeof event !== 'undefined') {
    close_time = event.timeStamp;
  }

  $FVPending = false;
};


//Why is this bound universally?  That seems strange.
//-Joe
$(document).bind('keydown', function (e) {
  if (e.which == 27) {
    FVClose();
  }
});