/*
 * Copyright (c) 2006-2014 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 //dependencies: jquery.touchswipe.js, foundation tabs, foundation reveal

(function($) {

  var $FV, $FVPending, selected_tab, $win, FigViewerInit, FVBuildHdr, FVBuildAbs, FVBuildRefs, FVDisplayPane, FVBuildFigs, FVSize, FVChangeSlide, FVFigDescripton, FVThumbPos, FVDisplayFig, FVLoadMedImg, FVLoadLargeImg, FVSizeImgToFit, FVSwitchImg, FVFigFunctions, FVDragInit, FVDragStop, FVSizeDragBox;

  $FV = {};
  $FVPending = false;
  selected_tab = $('.tab-title.active').attr('id');
  $win = $(window);
  // FigViewerInit is initiated when user clicks on anything to open the lightbox. Click events are at the bottom of this page.
  // ref=uri of specific figure clicked on; if not specific figure, is set to null
  // state=abst, figs, or refs; external_page = true if not on article page
  FigViewerInit = function(doi, ref, pane, external_page) {
    var findActive, rerunMathjax, loadJSON, displayModal;

    // allow only one instance of FigViewerInit to be pending.
    if ($FVPending) {
      return;
    }
    $FVPending = true;

    $FV = $('#fig-viewer');
    $FV.cont = $('#fig-viewer-content');
    $FV.nav = $('.fv-nav');

    findActive = $('.fv-nav').find('li');
    $.each(findActive, function(){
      var activateLi = $(this).hasClass(pane);
      if (activateLi === true){
        $(this).addClass('active');
      }
    });

     $(findActive).on('click', function(){
        FVDisplayPane(this.className);
     });

    $FV.figs_ref = ref; // reference for specific figure, if null, defaults to first figure
    $FV.txt_expanded = false; // figure descriptions are closed
    $FV.thmbs_vis = false; // figure thumbnails are hidden
    $FV.external_page = external_page ? true : false;

    rerunMathjax = function() {
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

    loadJSON = function() {
      //var apiurl = '/article/lightbox.action?uri=' + doi;
      var apiurl = 'http://localhost:8081/wombat/DesktopPlosPathogens/article?id=' + doi;
                    //journal.pmed.0010019
//      /wombat/DesktopPlosPathogens/article/figure/image?size=inline&amp;id=info:doi/10.1371/journal.ppat.1000621.g001
        console.log(apiurl);
      $.ajax({
        url:apiurl,
        dataFilter:function (data, type) {
          return data.replace(/(^\/<\*|\*\/$)/g, '');
        },
        dataType:'html',
        error: function (jqXHR, textStatus, errorThrown) {
          console.log(errorThrown);
          $FVPending = false;
        },
        success:function (data) {

          $FV.url = data.URL;

          var articleTitle = $('#artTitle').text();
          var authors = $('#author-list').find('.author-name');
          var authList = $(authors).text();

          FVBuildHdr(articleTitle, authList, doi);
         // FVBuildHdr(data.articleTitle, data.authors, data.uri);
         // FVBuildFigs(data);

          // from article tab where references,abstract and metadata exists, no need to fetch
          // them again from the server.
          if (typeof selected_tab != 'undefined' && selected_tab == 'tabArticle') {
            $FVPending = false;
            FVBuildAbs(doi, $('.article .abstract'), $('.article .articleinfo'));
            FVBuildRefs($('.article .references'));
            displayModal();

            //rerunMathjax();
          }
          else {
            var articleUrl = '/article?id=' + doi;
            console.log('fetch full article: ' + articleUrl);
            $.ajax({url: articleUrl, success: function(fullArticleHtml) {
              $FVPending = false;
              var article = $(fullArticleHtml);
              FVBuildAbs(doi, article.find('.article .abstract'), article.find('.article .articleinfo'));
              FVBuildRefs(article.find('.article .references'));
              displayModal();
              //rerunMathjax();

            }, error: function() {
              $FVPending = false;
            }});
          }
        }
      });
    };

    displayModal = function () {

     /* if(typeof(_gaq) !== 'undefined'){
        _gaq.push(['_trackEvent',"Lightbox", "Display Modal", ""]);
      }*/



     // FVSize();
      //FVDisplayPane(state);
      $FV.removeClass('abst figs refs').addClass(pane);
      // debounce resize event
      var resizeDelay;
      $win.bind('resize.modal', function() {
        clearTimeout(resizeDelay);
        resizeDelay = setTimeout(function() {
         // FVSize();
        }, 100);
      });
    };

    /* $(this).bind('keydown', function (e) {
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
     */
    loadJSON();

  };

  FVSize = function () {
    var win_h = $win.height();
    var frame_h = parseInt($FV.cont.css('marginTop')) + parseInt($FV.cont.css('marginTop'));
    var hdr_h = $FV.hdr.innerHeight();
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
  FVBuildHdr = function(title, authors, articleDoi) {
    $FV.hdr = $('.fv-title');

    var authArray = authors.trim().split(',');

    if ($FV.external_page) {
      var articleLink = "http://dx.plos.org/" + articleDoi.replace("info:doi/", "");
      var h1 = '<a href="' + articleLink + '">' + title + '</a>';

    } else {

      var h1 =  title;
    }

    $('#fvTitle').html(h1);
    var authorList = $('#fvAuthors');

    $.each(authArray, function (index, author) {
      var auth = $('<li> ' + author.trim() + '</li>');
      $(auth).appendTo(authorList);
    });

    truncate_elem.remove_overflowed('#fvAuthors');
  };

  // build figures pane
  FVBuildFigs = function(data) {
    var path, img_size, showInContext, title_txt, image_title, $thmb, slide, data, txt, content, txt_more, title, toggleLess, context_hash, doi, $fig, staging, dl, context_lnk;
    $FV.figs_pane = $('#fig-viewer-figs');
    $FV.thumbs_el = $('#fig-viewer-thmbs');
    $FV.thumbs_cont = $('#fig-viewer-thmbs-content');
    $FV.controls_el = $('#fig-viewer-controls');
    $FV.slides_el = $('#fig-viewer-slides');
    $FV.staging_el = $('<div class="staging" />'); // hidden container for loading large images
    $FV.figs_set = [];  // all figures array
    path = '/wombat/DesktopPlosPathogens/';
    img_size = '';
    // /wombat/DesktopPlosPathogens/article/figure/image?size=inline&amp;id=info:doi/10.1371/journal.ppat.1000621.g001
    //http://localhost:8081/wombat/DesktopPlosMedicine/article/figure/image?size=large&id=info:doi/10.1371/journal.pmed.0010019.t001
    showInContext = function (uri) {
      uri = uri.split('/');
      uri = uri.pop();
      uri = uri.split('.');
      uri = uri.slice(1);
      uri = uri.join('-');
      return '#' + uri;
    };

  //  $.each(data.secondaryObjects, function () {
      title_txt = (this.title ? '<b>' +this.title + ':</b> ' : '') + this.transformedCaptionTitle;

      image_title = this.title + ' ' + this.plainCaptionTitle;

     /* $thmb = $('<div class="thmb"' + ' data-uri="' + this.uri + '"><div class="thmb-wrap"><img src="' + path + this.uri + '&representation=PNG_I' + '" alt="' + image_title + '" title="' + image_title + '"></div></div>').on('click', function () {
        FVChangeSlide($(this));
      });*/
      //$FV.thumbs_cont.append($thmb);
      slide = $('<div class="slide" />');
      data_sect = $('<div class="data" />');
      txt = $('<div class="txt" />');
      content = $('<div class="content" />');
      txt_more = $('<div class="text-more" />');
      title = '<div class="title">' + title_txt + '</div>';
      toggleLess = $('<div class="toggle less" title="view less" />');
     // context_hash = showInContext(this.uri);
     /* if ($FV.external_page) { // the image is on another page
        context_hash = '/article/' + $FV.url + context_hash;
      }*/
     // doi = '<p class="doi">' + this.doi.replace('info:doi/','doi:') + '</p>';
        doi = '<p class="doi">doi:10.1371/journal.ppat.1000621</p>'
      ///wombat/DesktopPlosPathogens/article/figure/image?size=inline&id=info:doi/10.1371/journal.ppat.1000621.g001

      // we're not building the images here, just divs with the src of medium & large verisons in data attributes  <img src="resource/img/journal.pntd.0000085.g002.png"/>
     /* $fig = $('<div class="figure" data-img-src="' + path+ 'medium&id=' + this.uri  + '" data-img-lg-src="' + path +'large&id=' +this.uri+'" data-img-txt="' + image_title + '"></div>');

      $fig.data('state', 0) // track image loading state of figure
        .data('off-top', 0)
        .data('off-left', 0);
      $FV.figs_set.push($fig);*/

      staging = '<div class="staging" />'; // hidden container for loading large image

      dl = '<div class="download">'
        + '<h3>Download:</h3>'
        + '<div class="item"><a href="' + "/article/" + this.uri + "/powerpoint" + '" title="PowerPoint slide"><span class="btn">PPT</span></a></div>'
        + '<div class="item"><a href="' + "/article/" + this.uri + "/largerimage" + '" title="large image"><span class="btn">PNG</span><span class="size">' + /*convertToBytes(this.sizeLarge)*/ + '</span></a></div>'
        + '<div class="item"><a href="' + "/article/" + this.uri + "/originalimage" + '" title="original image"><span class="btn">TIFF</span><span class="size">' + /*convertToBytes(this.sizeTiff)*/ + '</span></a></div>'
        + '</div>';

      context_lnk = '<a class="btn lnk_context" href="' + context_hash + '" onclick="FVClose();">Show in Context</a>';

     // slide.append($fig);
      slide.append(staging);
      content.append(title);
      txt_more.append(toggleLess);
      txt_more.append(title);

      if (!/^\s*$/.test(this.transformedDescription)) {
        txt_more.append('<div class="desc">' + this.transformedDescription + '</div>');
        content.append('<div class="desc">' + this.transformedDescription + '</div>');
      }

      txt_more.append(doi);
      txt.append(content);
      txt.append(txt_more);
      data_sect.append(txt);
      data_sect.append(context_lnk);
      data_sect.append(dl);
      slide.append(data);
      $FV.slides_el.append(slide);
//    }); end of $.each on  #238

    // thumbnail close button
    $('.btn-thmb-close').on('click',function() {
      $FV.figs_pane.toggleClass('thmbs-vis');
      $FV.thmbs_vis = $FV.thmbs_vis ? false : true;
    });
    $FV.thumbs_el.append($FV.thumbs_cont);

    $FV.slides = $FV.slides_el.find('div.slide'); // all slides
    $FV.figs = $FV.slides_el.find('div.figure'); // all figures
    $FV.thumbs = $FV.thumbs_el.find('div.thmb'); // all thumbnails
    $FV.thumbs.active = null; // used to track active thumb & figure

    // figures controls
    $('.thmb-btn').on('click',function() {
      $FV.figs_pane.toggleClass('thmbs-vis');
      $FV.thmbs_vis = $FV.thmbs_vis ? false : true;
      FVThumbPos($FV.thumbs.active);
    });
    $FV.nxt = $('.next').on('click',function() {
      FVChangeSlide($FV.thumbs.active.next());
    });
    $FV.prv = $('.prev').on('click',function() {
      FVChangeSlide($FV.thumbs.active.prev());
    });

    /*$FV.loading = $('<div class="loading-bar"></div>').appendTo($FV.controls_el);
    $FV.zoom = $('<div id="fv-zoom" />');
    $FV.zoom.min = $('<div id="fv-zoom-min" />').appendTo($FV.zoom);
    $FV.zoom.sldr = $('<div id="fv-zoom-sldr" />').appendTo($FV.zoom);
    $FV.zoom.max = $('<div id="fv-zoom-max" />').appendTo($FV.zoom);
    $FV.controls_el.append($FV.zoom);*/

    $FV.figs_pane.append($FV.slides_el);
    $FV.figs_pane.append($FV.thumbs_el);
    //$FV.figs_pane.append($FV.controls_el);
    $FV.figs_pane.append($FV.staging_el);

    $FV.cont.append($FV.figs_pane);

    if ($.support.touchEvents) {
      $FV.slides_el.swipe({
        swipeLeft:function(event, direction, distance, duration, fingerCount) {
          if ($FV.thumbs.active.next().length) {
            t = $FV.thumbs.active.next();
            FVChangeSlide(t);
          }
        },
        swipeRight:function(event, direction, distance, duration, fingerCount) {
          if ($FV.thumbs.active.prev().length) {
            t = $FV.thumbs.active.prev();
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
  FVBuildAbs = function(doi, abstractText, metadata) {
    $FV.abst_pane = $('<div id="fig-viewer-abst" class="pane"4 />');
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

    var lnk_pdf = '<div class="fv-lnk-pdf"><a href="/article/fetchObject.action?uri=' + doi + '&representation=PDF" target="_blank" class="btn">Download: Full Article PDF Version</a></div>';
    $abst_content.append(lnk_pdf);

    var $abst_info = $('<div class="info" />');
    $abst_info.html(metadata.html());

    $FV.abst_pane.append($abst_content);
    $FV.abst_pane.append($abst_info);
    $('#panel-abst').append($FV.abst_pane);
    //$FV.cont.append();

  };

  // build references pane
  FVBuildRefs = function(references) {
    $FV.refs_pane = $('<div id="fig-viewer-refs" class="pane"/>');
    var $refs_content = $('<ol class="references" />');
    $refs_content.html(references.html());
    $FV.refs_pane.append('<h3>References</h3>');
    $FV.refs_pane.append($refs_content);
    $('#panel-refs').append($FV.refs_pane);
  };


  // toggle between panes
  FVDisplayPane = function(pane) {
    $FV.removeClass('abst figs refs').addClass(pane);
    if (pane == 'figs') {   console.log('pane is figs');
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
  FVChangeSlide = function($thmb) {

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
  FVFigDescripton = function(sld) {
    var $btn_less = sld.find('div.toggle.less');
    var $content = sld.find('div.content');

    var truncate = function() {
      //If called on the same element twice, ignore second call
      if($content.data('ellipsis_appended') != 'true') {
        $content.ellipsis({ ellipsis_text:'<span class="toggle more">... show more</span>' });
        $content.find('span.more').click(function() {
          $FV.slides_el.addClass('txt-expand');
          $FV.txt_expanded = true;
        });
        $content.data('ellipsis_appended','true');
      }
    };

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
        truncate();
      });
    }
  };



/**
 * Bring a thumbnail image into view if it's scrolled out of view.
 * @param thmb the thumbnail image to bring into view
 */
FVThumbPos = function($thmb) {
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
FVDisplayFig = function(i) {
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
FVLoadMedImg = function(i) {
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
FVLoadLargeImg = function(i) {
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
FVSizeImgToFit = function(el, down_only) {
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
FVSwitchImg = function($fig) {
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
FVFigFunctions = function($fig) {
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

FVDragInit = function($fig, $img) {
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

FVDragStop = function($fig, $img) {
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
FVSizeDragBox = function($fig, $img) {
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
/*var FVClose = function() {
 // $FV.hide();

  // remove helper class added in displayModal()
  $('body').removeClass('modal-active');

  $win.unbind('resize.modal');
  //will record the timeStamp for when the modal is closed
  if(typeof event !== 'undefined') {
    close_time = event.timeStamp;
  }

  $FVPending = false;
};*/

  /**
   * Drop words until the element selected fits within its container and then append an ellipsis
   *
   * @param topElement the very top element being worked on
   * @param parts the elements currently being worked on
   */
  var ellipsis_recurse = function(topElement, parts) {
    var ellipsis_added = false;

    //traverse from the last element up
    for(var a = parts.length - 1; a > -1; a--) {
      var element = parts[a];

      //ellipsis added, no need to keep going
      if(ellipsis_added) {
        break;
      }

      //If this is a text node, work on it, otherwise recurse
      //http://www.w3schools.com/jsref/prop_node_nodetype.asp
      if(element.nodeType != 3) {
        ellipsis_added = ellipsis_recurse(topElement, $(element).contents());

        if(ellipsis_added) {
          //ellipsis added, no need to keep looping
          break;
        }
      } else {
        var words = $(element).text().split(" ");

        while(ellipsis_added == false && words.length > 0) {
          //Keep popping words until things fit, or the length is zero
          //Could get a performance increase by doing this by halves instead of one by one?
          words.pop();

          $(element).text(function() {
            this.nodeValue = words.join(" ");
          });

          //If all words have been popped that need to be, append the new node for the ellipsis
          if(topElement.scrollHeight <= topElement.clientHeight) {

            var ellipsis = $($.fn.ellipsis.settings.ellipsis_text).uniqueId();
            $(element).after(ellipsis);
            ellipsis_added = true;

            //The new text has introduced a line break, pop more words!
            while(topElement.scrollHeight > topElement.clientHeight) {
              if(words.length > 0) {
                words.pop();

                $(element).text(function() {
                  this.nodeValue = words.join(" ");
                });
              } else {
                //No more words to pop. Remove the element, pass through and add the ellipsis someplace else
                $('#' + ellipsis.attr('id')).remove();
                ellipsis_added = false;
              }
            }
          }
        }
      }
    }

    return ellipsis_added;
  }

  /**
   * Drop words until the element selected fits within its container and then append an ellipsis
   */
  $.fn.ellipsis = function(options) {
    $.fn.ellipsis.settings = $.extend({}, $.fn.ellipsis.settings, options);
    ellipsis_recurse(this[0], $(this).contents());
    return this;
  };

  $.fn.ellipsis.settings = {
    ellipsis_text : '<span>...</span>'
  };

//Why is this bound universally?  That seems strange.
//-Joe
$(document).bind('keydown', function (e) {
  if (e.which == 27) {
    FVClose();
  }
});

function initMainContainer() {
  var $figure_thmbs = $('#figure-thmbs');

  $figure_thmbs.detach();
  $figure_thmbs.insertBefore($('.article .articleinfo'));

  if ($figure_thmbs.length) {
    $lnks = $figure_thmbs.find('.item a');
    $wrap = $figure_thmbs.find('div.wrapper');
    if ($lnks.length) {
      $figure_thmbs.css('visibility', 'visible');
      $('<h3>Figures</h3>').insertBefore($figure_thmbs);

      $lnks.on('click', function (e) {
        e.preventDefault();
        doi = $(this).data('doi');
        ref = $(this).data('uri');
        FigViewerInit(doi, ref, 'figs');
      });
    } else {
      $figure_thmbs.addClass('collapse');
    }
  }

  // inline figures
  var $fig_inline = $('#article-block').find('div.figure');
  if ($fig_inline.length) {
    $lnks = $fig_inline.find('a');
    $lnks.on('click', function (e) {   console.log('click inline');
      e.preventDefault();
      ref = $(this).data('uri');
      doi = $(this).data('doi');
      FigViewerInit(doi, ref, 'figs');
    });
    $lnks.append('<div class="expand" />');
  }

  // figure search results
  var $fig_results = $('#fig-search-results, .article-block .actions, #subject-list-view .actions');
  if ($fig_results.length) {
    $fig_results.find('a.figures').on('click', function (e) {
      console.log($(this).data('doi'));
      FigViewerInit(doi, null, 'figs', true);
      e.preventDefault();
      return false;
    });
    $fig_results.find('a.abstract').on('click', function (e) {
      doi = $(this).data('doi');
      FigViewerInit(doi, null, 'abst', true);
      e.preventDefault();
      return false;
    });
  }
  //wombat: <li id="nav-figures"><a data-doi="10.1371/journal.pmed.0030255">Figures</a></li>
  //Ambra: <li id="nav-figures"><a data-doi="info:doi/10.1371/journal.pmed.1001685">Figures</a></li>
  // figure link in article floating nav
  var $nav_figs = $('#nav-figures').find('a');
  if ($nav_figs.length) {
    $nav_figs.on('click', function () {
      var doi = $nav_figs.data('doi');

      FigViewerInit(doi, null, 'figs');
    });
  }


  // figure link in the toc
  var $toc_block_links = $('#toc-block div.links');
  if ($toc_block_links.length) {
    $toc_block_links.find('a.figures').on('click', function () {
      var doi = $(this).data('doi');
      FigViewerInit(doi, null, 'figs', true);
    });

    $toc_block_links.find('a.abstract').on('click', function () {
      var doi = $(this).data('doi');
      FigViewerInit(doi, null, 'abst', true);
    });
  }


}
initMainContainer();

})(jQuery);