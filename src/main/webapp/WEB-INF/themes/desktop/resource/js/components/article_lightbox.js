
//dependencies: jquery.touchswipe.js, foundation reveal
// 'lb' = lightbox and is wombat terminology;
// 'FV' = figure viewer and is ambra terminology
(function($) {
  "use strict";

  var $FV, $FVPending, selected_tab, $win, FVBuildHdr, FVBuildAbs, FVBuildRefs, FVDisplayPane, FVBuildFigs, FVSize, FVChangeSlide, FVArrowKeys, FVFigDescription, FVThumbPos, FigController, FVLoadMedImg, FVLoadLargeImg, FVSizeImgToFit, FVSwitchImg, FVZoomControls, FVDragInit, FVDragStop, FVSizeDragBox, displayModal, get_ref, get_doi, close_time;

  $FV = {};
  $FVPending = false;
  selected_tab = $('.tab-title.active').attr('id');
  $win = $(window);

  var lightbox = {};
  // FigViewerInit is initiated when user clicks on anything to open the lightbox.
  // $FV.figure_specified = source of specific figure clicked on; if no specific figure, is set to null
  // state = abst, figs, or refs; external_page = true if user wasn't on article page
  lightbox.FigViewerInit = function(doi, specified_figure, state, external_page) {
    var rerunMathjax, loadJSON;

    //disable scrolling on web page behind fig viewer
    $('body').css('overflow', 'hidden').on('touchmove', function(e){ e.preventDefault() });

    $FV = $('body').find('#article-lightbox');

    $FV.cont = $('#lightbox-content');

    $('#article-lightbox').foundation('reveal', 'open', {
      url: 'article/lightbox',
      success: function(data) {
        // console.log('modal data loaded');
      },
      error: function() {
        // alert('failed loading modal');
      }
    });

    // allow only one instance of FigViewerInit to be pending.
    if ($FVPending) {
      return;
    }

    $FVPending = true;

    $FV.hdr = $('.fv-header');

    // if a specific figure is passed as an argument (specified_figure), cut off the extraneous info
    // if null, the first image is used

    if (specified_figure) {
      specified_figure = specified_figure.slice(9);
    }  else {
      specified_figure = null;
    }

    $FV.figure_specified = specified_figure;
    $FV.txt_expanded = false; // figure descriptions are closed
    $FV.thmbs_vis = false; // figure thumbnails are hidden
    $FV.external_page = external_page ? true : false;

    loadJSON = function() {

      var apiurl = siteUrlPrefix + 'article?id=' + doi;

      $.ajax({
        url:apiurl,
        dataFilter:function (data, type) {
          return data.replace(/(^\/<\*|\*\/$)/g, '');
        },
        cache: false,
        dataType:'html',
        error: function (jqXHR, textStatus, errorThrown) {

          //TODO: put in proper error handling
          //console.log(errorThrown);

          $FVPending = false;
        },
        success:function (data) {
          var main_start, main_end, data_main, article_title, authors, auth_list, article_body, abstract_data, abstract_info;

          main_start = data.indexOf('<main>') + 6;
          main_end = data.indexOf('</main>');
          data_main = data.substring(main_start, main_end);
          article_title = $(data_main).find('#artTitle').text();
          authors = $(data_main).find('.author-name');
          auth_list = $(authors).text();
          article_body = $(data_main).find('#artText');
          abstract_data = $(data_main).find('.abstract');
          abstract_info = $(data_main).find('.articleinfo');

          FVBuildHdr(article_title, auth_list, doi, state);

          FVBuildAbs(doi, abstract_data, abstract_info);

          FVBuildFigs(article_body, doi);

          FVBuildRefs($(data_main).find('.references'));

          $FVPending = false;

          displayModal();

          rerunMathjax();

        }
      });
    };

    rerunMathjax = function() {
      // rerun mathjax
      try {
        var dom_elem = $FV[0];
        if (dom_elem && typeof MathJax != "undefined") {
          MathJax.Hub.Queue(["Typeset",MathJax.Hub,domelem]);
        }
      } catch (e) {
        // ignore
      }
    };

    displayModal = function () {


      if(typeof(ga) !== 'undefined'){
        ga('send', 'event', 'Lightbox', 'Display Modal', '');
      }


      FVSize();
      FVDisplayPane(state);

      // debounce resize event
      var resizeDelay;
      $(window).on('resize.modal', function() {
        clearTimeout(resizeDelay);
        resizeDelay = setTimeout(function() {
          FVSize();
        }, 100);
      });
    };

    loadJSON();
  };

  FVSize = function () {
    var win_h, frame_h, hdr_h, data_h, modal_h, base_h, fig_h;

    win_h = $win.height();
    frame_h = 20; //account for the 10px border
    hdr_h = 46; //height of title header
    data_h = 120; // height of bottom controls
    modal_h = win_h - frame_h;
    base_h = modal_h - hdr_h;
    fig_h = base_h - data_h;

    //Set the height of the fig-viewer container: window height - border size
    $FV.cont.css('height', modal_h);
    //figure height is window minus 10px border, header, and the controls heights
    $FV.figs.css('height', fig_h);
    $FV.thumbs_cont.css('height', fig_h - parseInt($FV.thumbs_el.css('paddingTop')));
    $FV.abst_pane.css('height', base_h - 1);
    $FV.refs_pane.css('height', base_h);

    if ($FV.thmbs_vis) {
      FVThumbPos($FV.thumbs.active);
    }

  };

  // build header elements
  FVBuildHdr = function(title, authors, articleDoi, state) {
    var findActive, articleLink, h1, trimAuth, auth;

    findActive = $('.fv-nav').find('li');

    $.each(findActive, function(){

      var $panel_class = $(this).attr('class');

      if (state === $panel_class){
        $(this).addClass('tab_active').trigger('click');
      }
    });

    findActive.on('click', function(){

      var $tab_clicked = $(this);
      var $tab_sibs = $(this).siblings();
      var $panel_name = '#lightbox-'+ $tab_clicked.attr('class');

      if ( $tab_sibs.hasClass('tab_active') ){
        $tab_sibs.removeClass('tab_active');
      }

      $($panel_name).css('display','block');

      $.each($tab_sibs, function(){
        var $panel_sib = '#lightbox-'+$(this).attr('class');
        $($panel_sib).css('display','none');
      });

      $tab_clicked.addClass('tab_active');

    });

    if ($FV.external_page) {
      articleLink = "http://dx.plos.org/" + articleDoi.replace("info:doi/", "");
      h1 = '<a href="' + articleLink + '">' + title + '</a>';

    } else {

      h1 = title;
    }

    $('#fvTitle').append(h1);

    if (authors) {
      var authArray = authors.split(',');

      var authorList = $('#fvAuthors');

      var get_last = authArray.length - 1;

      $.each(authArray, function (index, author) {

        trimAuth = trimIt(author);

        if (index !== get_last) {
          auth = $('<span>' + trimAuth + ', </span>');
        } else {
          auth = $('<span>' + trimAuth + ' </span>');
        }

        $(auth).appendTo(authorList);

      });

    }  else { }

    $('.fv-close').on('click',function(){
      FVClose();
    });
  };

  // build abstract pane
  FVBuildAbs = function(doi, abstractText, metadata) {
    $FV.abst_pane = $('<div id="lightbox-abst" class="pane" />');
    var hdr, lnk_pdf, pdf_href, $abst_info, $abst_content = $('<div class="abstract" />');
    // hdr = $()
    if (abstractText.size() == 0) {
      // There is no abstract. Hide the "view abstract" button created in FVBuildHdr.
      $FV.hdr.find('li.abst').hide();
    }
    else {
      $abst_content.html(abstractText.html());
      $abst_content.find("h2").remove();
      $abst_content.find('a[name="abstract0"]').remove();
    }

    pdf_href = $('#downloadPdf').attr('href');
    lnk_pdf = '<div class="fv-lnk-pdf"><a href="' + pdf_href + '" target="_blank" class="btn">Download: Full Article PDF Version</a></div>';
    $abst_content.append(lnk_pdf);
    $abst_info = $('<div class="info" />');
    $abst_info.html(metadata.html());
    $FV.abst_pane.append($abst_info);
    $FV.abst_pane.append($abst_content);

    $('#lightbox-content').append($FV.abst_pane);

  };

  // build references pane
  FVBuildRefs = function(references) {
    var refs_content = references.html();

    $FV.refs_pane = $('<div id="lightbox-refs" class="pane"/>');
    $FV.refs_pane.append('<h3>References</h3>');
    $FV.refs_pane.append('<ol class="references">'+ refs_content +'</ol>');
    $('#lightbox-content').append($FV.refs_pane);
  };

  // add panel name to fig-viewer tag & display figure chosen (if on figs panel)
  FVDisplayPane = function(pane) {
    //display abstract (abst), figures (figs), or references (refs):
    $FV.addClass(pane);

    //special handling for the figure pane
    if (pane === 'figs') {

      if ($FV.thumbs.active === null) { // no thumb is active

        if ($FV.figure_specified) { // a specific figure is requested, so trigger corresponding thumb click:

          $FV.thumbs_cont.find('div[data-uri="' + $FV.figure_specified + '"]').trigger('click');

        } else { // default to first figure

          $FV.thumbs.eq(0).trigger('click');
        }
      } else {
        // run FigController() again to update figure status
        /* (the following is a backup plan. see FVChangeSlide for first line of defense.)
         1. User is on the Figures pane
         2. then Abstract or Reference panel is invoked,
         3. user returns to the figure pane
         a. If a medium or large image finished loading while the figure pane was not visible,
         figure building would stop (it requires figure pane to be visible to access image dimensions)
         */
        FigController($FV.thumbs.index($FV.thumbs.active));

      }
    } else { }
  };


  // build figures pane. needs to be broken into smaller parts.
  FVBuildFigs = function($data, doi) {
    var path, showInContext, article_page_figure, title_txt, image_title, text_title, img_ref, $thmb, thmb_close, slide, datacon, txt, txt_less, txt_more, fig_title, show_context, view_less, view_more, context_hash, doip, $fig_div, staging, download_btns, chk_desc, text_description;

    //set the markup
    $FV.figs_pane = $('<div id="lightbox-figs" class="pane" />');
    $FV.thumbs_el = $('<div id="fig-viewer-thmbs" />');
    $FV.thumbs_cont = $('<div id="fig-viewer-thmbs-content" />');
    $FV.controls_el = $('<div id="fig-viewer-controls" />');
    $FV.slides_el = $('<div id="fig-viewer-slides" />');
    $FV.staging_el = $('<div class="staging" />'); // hidden container for loading large images
    $FV.figs_array = [];  // array for all div.figure: .figure > .drag-bx > img. parent is .slide

    path = siteUrlPrefix+'article/figure/image?size=';

    showInContext = function (uri) {
      uri = uri.split('/');
      uri = uri[1].slice(8);
      uri = uri.replace(/\./g,'-');
      return '#' + uri;
    };
    //get the div.figure(s) in the article page html
    article_page_figure = $data.find('.figure');

    //iterate through each div.figure on the article page and build a slide div for it with the controls (next,prev, thumbs)
    $.each(article_page_figure, function () {
      var article_fig_data = $(this);

      // get all the image info needed to build the lightbox slide
      title_txt = $(article_fig_data).find('.figcaption');
      text_description = $(article_fig_data).find('.caption_target').next().html();
      image_title = title_txt.text();
      text_title = $(title_txt).html();
      img_ref = $(article_fig_data).data('doi');
      //'show in context' button:
      context_hash = showInContext(img_ref);

      /* save this for later
       if ($FV.external_page) { // the image is on another page
       context_hash = '/article/' + $FV.url + context_hash;
       }
       */

      /* Build a div with the references of medium & large img versions from the data attributes
       ..This div will be the container for the image that is visible in the lightbox.
       ..Both the medium size and large size are loaded:
       ...The medium is put into $fig_div
       ...The large size is put into div.staging to monitor its loading state.
       ...When the large size is completely loaded, it is moved to $fig_div;
       ...then the medium size is set to display none.
       */
      $fig_div = $('<div class="lb-figure" data-img-src="' + path + 'medium&id=info:doi/'
        + img_ref + '" data-img-lg-src="' + path +'large&id=info:doi/'
        + img_ref + '" data-img-txt="' + image_title + '"></div>');

      // track image loading state of figure
      $fig_div.data({
        'state' : 0,
        'off-top' : 0,
        'off-left' : 0
      });//.removeData('img_l_w', 'img_l_h');

      //add the empty $fig_div with the image info to $FV.figs_array
      $FV.figs_array.push($fig_div);

      //build thumbnail for thumbnail strip
      $thmb = $('<div class="thmb" data-uri="' + img_ref + '">'
        + '<div class="thmb-wrap">'
        + '<img src="' + path +'inline&id=info:doi/'+ img_ref + '" alt="' + image_title + '" title="' + image_title+ '"/>'
        + '</div></div>');

      $FV.thumbs_el.append($FV.thumbs_cont);
      $FV.thumbs_cont.append($thmb);

      //the markup for the slide caption (which should really be done with backbone or similar)
      slide = $('<div class="slide" />');
      datacon = $('<div class="datacon" />');
      txt = $('<div class="txt" />');
      txt_less = $('<div class="text-less" />');
      txt_more = $('<div class="text-more" />');
      fig_title = '<div class="fig_title">' + text_title + '</div>';
      view_more = '<span class="toggle more">... show more</span>';
      view_less = $('<div class="less" title="view less" />');
      doip = '<p class="doi">doi:'+img_ref+'</p>';
      staging = '<div class="staging" />'; // hidden container for loading large image
      show_context = '<a class="btn show_context" href="' + context_hash + '">Show in Context</a>';
      download_btns = '<div class="download">'
        + '<h3>Download:</h3>'
        + '<div class="item">'
        + '<a href="' + "article/figure/powerpoint?id=info:doi/" + img_ref + '" title="PowerPoint slide">'
        + '<span class="btn">PPT</span></a>'
        + '</div>'
        + '<div class="item">'
        + '<a href="' + "article/figure/image?size=large&id=info:doi/" + img_ref + '" title="large image">'
        + '<span class="btn">PNG</span>'
        /* + '<span class="size">' +convertToBytes(this.sizeLarge)  '</span></a>' */
        + '</div>'
        + '<div class="item">'
        + '<a href="' + "article/figure/image?size=original&id=info:doi/" + img_ref + '" title="original image"><span class="btn">TIFF</span>'
        /* + '<span class="size">' + convertToBytes(this.sizeTiff)  '</span>'*/
        +'</a></div>'
        + '</div>';

      //combine the markup & add to the slide
      slide.append($fig_div);
      slide.append(staging);
      txt_less.append(fig_title);
      txt_more.append(view_less);
      txt_more.append(fig_title);

      if (text_description !== null) {
        chk_desc = text_description.slice(0,3);
        if (chk_desc !== 'doi') {
          txt_more.append('<p>' + text_description + '</p>');
          txt_less.append('<p>' + text_description + '</p>');
        }
      }

      txt_less.append(view_more);
      txt_more.append(doip);
      txt.append(txt_less);
      txt.append(txt_more);
      datacon.append(txt);
      datacon.append(show_context);
      datacon.append(download_btns);
      slide.append(datacon);

      $FV.slides_el.append(slide);

      $thmb.on('click', function () {
        FVChangeSlide($(this));
        FVArrowKeys($(this));
      });

    });

    // figures controls in control bar
    $('<span class="fig-btn thmb-btn"><i class="icn"></i> All Figures</span>').on('click',function() {
      $FV.figs_pane.toggleClass('thmbs-vis');
      $FV.thmbs_vis = $FV.thmbs_vis ? false : true;
      FVThumbPos($FV.thumbs.active);
    }).appendTo($FV.controls_el);

    $FV.nxt = $('<span class="fig-btn next invisible"><i class="icn"></i> Next</span>');
    $FV.nxt.appendTo($FV.controls_el);

    $FV.prv = $('<span class="fig-btn prev invisible"><i class="icn"></i> Previous</span>');
    $FV.prv.appendTo($FV.controls_el);

    $FV.nxt.on('click',function() {
      FVChangeSlide($FV.thumbs.active.next());
    });
    $FV.prv.on('click',function() {
      FVChangeSlide($FV.thumbs.active.prev());
    });

    $FV.thumbs_el.append($FV.thumbs_cont);
    $FV.slides = $FV.slides_el.find('div.slide'); // all slides
    $FV.figs = $FV.slides_el.find('div.lb-figure'); // all figures
    $FV.thumbs = $FV.thumbs_el.find('div.thmb'); // all thumbnails
    $FV.thumbs.active = null; // used to track active thumb & figure

    $FV.loading = $('<div class="loading-bar"></div>').appendTo($FV.controls_el);
    $FV.zoom = $('<div id="fv-zoom" />');
    $FV.zoom.min = $('<div id="fv-zoom-min" />').appendTo($FV.zoom);
    $FV.zoom.sldr = $('<div id="fv-zoom-sldr" />').appendTo($FV.zoom);
    $FV.zoom.max = $('<div id="fv-zoom-max" />').appendTo($FV.zoom);

    $FV.controls_el.append($FV.zoom);
    $FV.slides_el.append($FV.controls_el);

    // thumbnail close button
    thmb_close = $('<span class="btn-thmb-close" title="close" />');
    $FV.thumbs_el.prepend(thmb_close);
    $FV.figs_pane.append($FV.slides_el);
    $FV.figs_pane.append($FV.thumbs_el);
    //  $FV.figs_pane.append($FV.staging_el); Two div.stagings are built; not sure this one is necessary
    $('#lightbox-content').append($FV.figs_pane);

    $(thmb_close).on('click',function() {
      $FV.figs_pane.toggleClass('thmbs-vis');
      $FV.thmbs_vis = $FV.thmbs_vis ? false : true;
    });
    $('.datacon').find('a.show_context').on('click', function(){
      FVClose();
    });
    if ($.support.touchEvents) {
      var th;
      $FV.slides_el.swipe({
        swipeLeft:function(event, direction, distance, duration, fingerCount) {
          if ($FV.thumbs.active.next().length) {
            th = $FV.thumbs.active.next();
            FVChangeSlide(th);
          }
        },
        swipeRight:function(event, direction, distance, duration, fingerCount) {
          if ($FV.thumbs.active.prev().length) {
            th = $FV.thumbs.active.prev();
            FVChangeSlide(th);
          }
        },
        tap:function(event, target) {
          target.click();
        },
        threshold:25
      });
    }

  }; //end FVBuildFigs

  // display figure slides functionality
  FVChangeSlide = function($thmb) {
    var fig_div, fig_img, i, this_sld;

    if (typeof(ga) !== 'undefined'){
      ga('send', 'event', 'Lightbox', 'Slide Changed', '');
    }

    /*fig_img.css({
     'marginLeft' : 0,
     'marginTop' : 0,
     'height' : fig_div.data('img_sized_h'),
     'width' : fig_div.data('img_sized_w')
     });*/

    if ($FV.thumbs.active !== null) {
      $FV.thumbs.active.removeClass('active');
      fig_div = $FV.figs_array[$FV.thumbs.index($FV.thumbs.active)];
      fig_img = fig_div.find('img');

      if (fig_img.hasClass('ui-draggable')) {
        FVDragStop(fig_div, fig_div.find('img'));
      }
    }
    $FV.thumbs.active = $thmb;
    $FV.thumbs.active.addClass('active');

    $FV.slides.hide();

    i = $FV.thumbs.index($thmb);
    this_sld = $FV.slides.eq(i);
    this_sld.show();
    FigController(i);
    FVFigDescription(this_sld);

    $FV.thumbs.active.next().length ? $FV.nxt.removeClass('invisible') : $FV.nxt.addClass('invisible');
    $FV.thumbs.active.prev().length ? $FV.prv.removeClass('invisible') : $FV.prv.addClass('invisible');

    if ($FV.thmbs_vis) {
      FVThumbPos($thmb);
    }
  };

  FVArrowKeys = function() {

    // arrow keys operate the next / previous slide
    // keydown is unbound in FVClose() so up/down works on web page
    $win.on('keydown', function (e) {
      // left / up arrow keys
      if (e.which == 37 || e.which == 38) {
        if ($FV.thumbs.active.prev().length) {
          var sl = $FV.thumbs.active.prev();
          FVChangeSlide(sl);
        }
        return false;
      }
      // right / down arrow keys
      if (e.which == 39 || e.which == 40) {
        if ($FV.thumbs.active.next().length) {

          var sl = $FV.thumbs.active.next();
          FVChangeSlide(sl);
        }
        return false;
      }
    });
  };

  // figure description expand/reduce controls
  // called in FVChangeSlide
  FVFigDescription = function(sld) {

    var $btn_less, $content, truncate, desc_link;
    $btn_less = sld.find('div.less');
    $content = sld.find('div.text-less');

    truncate = function() {
      //If called on the same element twice, ignore second call
      if($content.data('ellipsis_appended') != 'true') {
        $content.dotdotdot({after: "span.more", ellipsis: ""});
        $content.find('span.more').click(function() {
          $FV.slides_el.addClass('txt-expand');
          $FV.txt_expanded = true;
        });
        $content.data('ellipsis_appended','true');
      }
    };

    // check display of descriptions expanded or not
    if (!$FV.txt_expanded) { // landed on slide and descriptions are hidden.
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

    //close lightbox if link in description is clicked on
    desc_link = sld.find('.fig_title').next();

    $(desc_link).find('a').on('click',function(){
      FVClose();
    });
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


// this function checks the status of figure image building/resizing, and directs to appropriate step
  FigController = function(i) {
    $FV.loading.show();
    $FV.zoom.hide();
    var this_fig = $FV.figs_array[i];
    var $img =  this_fig.find('img');
    var img_state = this_fig.data('state');
    // state of figure
    // 0 - no image loaded
    // 1 - medium image loading
    // 2 - medium image loaded, not visible, not yet resized
    // 3 - medium image visible & resized, large image in process of loading
    // 4 - medium image visible, large image loaded in hidden staging div
    // 5 - large image visible
    switch(img_state) {
      case 0:
        FVLoadMedImg(i);
        break;
      case 1:
        break;
      case 2:
        FVSizeImgToFit(this_fig, false);
        // $img.removeClass('invisible');
        this_fig.data('state', 3);
        FVLoadLargeImg(i);
        break;
      case 3:
        // waiting on large image to load
        break;
      case 4:
        FVSwitchImg($FV.figs_array[i]);
        $FV.loading.hide();
        this_fig.data('state', 5);
        break;
      case 5:
        $FV.loading.hide();
        FVSizeImgToFit(this_fig);
        if (this_fig.hasClass('zoom')) {
          return FVZoomControls(this_fig);
        }
        break;
    }
  };

// build medium image, when loaded - size to fit, call load large image function
  FVLoadMedImg = function(i) {
    var $fig_div, src_med, txt, img_med;

    $fig_div  = $FV.figs_array[i];
    src_med = $fig_div.data('img-src');
    txt = $fig_div.data('img-txt');
    img_med = $('<img src="' + src_med + '" title="' + txt + '" alt="' + txt + '" class="med invisible"/>');

    $fig_div.append(img_med); // add medium image (set to hidden in css)
    $fig_div.data('state', 1);

    $fig_div.imagesLoaded(function() {

      $fig_div.data('state', 2);

      if (i == $FV.thumbs.index($FV.thumbs.active) && $FV.hasClass('figs')) { // true if this slide is still visible
        FVSizeImgToFit($fig_div, false);
        $fig_div.find('img').removeClass('invisible');
        $fig_div.data('state', 3);
        FVLoadLargeImg(i);
      }
    });
  };

// load large images in div.staging
  FVLoadLargeImg = function(i) {
    var $fig_div, src_lg, img_txt, stage, img_lg;

    $fig_div = $FV.figs_array[i];

    src_lg = $fig_div.data('img-lg-src');
    img_txt = $fig_div.data('img-txt');
    stage = $fig_div.next('.staging');

    img_lg = '<img src="' + src_lg + '" title="' + img_txt + '" alt="' + img_txt + '" class="lg invisible"/>';

    stage.append(img_lg); // load large img into 'staging' div & append to fig_div when it is finished loading
    stage.imagesLoaded(function() {
      $fig_div.data('state', 4);

      if (i == $FV.thumbs.index($FV.thumbs.active) && $FV.hasClass('figs')) { // true if this slide is still visible
        FVSwitchImg($fig_div);
        $FV.loading.hide();
        $fig_div.data('state', 5);
      }
    });

  };

// size images to fit in div.lb-figure element
  FVSizeImgToFit = function ($fig_div, down_only) {
    var sizeAndCenter, fig_img, fig_div_h, fig_div_w, i_h, i_w, resized_i_w, resized_i_h;

    $fig_div.imagesLoaded(function(){

      fig_img = $fig_div.find('img');
      fig_div_h = $fig_div.height();
      fig_div_w = $fig_div.width();
      i_h = fig_img.height();
      i_w = fig_img.width();
      var fig_div_aspect_ratio = fig_div_w / fig_div_h;
      var img_aspect_ratio = i_w / i_h;
      // sizes image to fit, scaling up or down, and centering
      sizeAndCenter = function() {
        // compare aspect ratios of parent and image

        if (fig_div_aspect_ratio > img_aspect_ratio) { // fig div is wider than high, img is higher than wide
          //make the image fill parent, calculate the left margin
          fig_img.css('height', fig_div_h);
          //get the new width
          resized_i_w = fig_div_h * img_aspect_ratio;
          //calculate and set the left margin for centering
          fig_img.css({
            'marginLeft': Math.round((fig_div_w - resized_i_w) / 2),
            'marginTop': 0
          });
        } else {  // img is way wider than high

          if (i_w !== fig_div_w && i_w < fig_div_w) {
            fig_img.css({
              'height': Math.round(fig_div_w / img_aspect_ratio),
              'width': Math.round(fig_div_h * img_aspect_ratio)
            });
            //resized_i_w = fig_img.width();
            resized_i_h = fig_img.height();
            fig_img.css({
              'marginTop': Math.round((fig_div_h - resized_i_h) / 2),
              'marginLeft': 0
            });
          } else { }
        }
      };

      if (down_only) {// this is a large image and we don't want to scale up.
        if (fig_div_h > $fig_div.data('img_l_h') && fig_div_w > $fig_div.data('img_l_w')) { // native size smaller than viewport

          fig_img.css({
            'marginTop' : Math.round((fig_div_h - i_h) / 2),
            'marginLeft' : Math.round((fig_div_w - i_w) / 2)
          });

          $fig_div.removeClass('zoom'); // too small to zoom

        } else {
          sizeAndCenter();
          $fig_div.addClass('zoom');
        }
      } else {
        sizeAndCenter();
      }
    });
  };


// switch medium image with large image

  FVSwitchImg = function($fig_div) {
    var img_m, img_m_h, img_m_w, img_l, img_l_h, img_l_w, drag_bx;

    img_m = $fig_div.find('img');
    img_m_h = img_m.height();
    img_m_w = img_m.width();
    img_l = $fig_div.next().find('img.lg');

    // move large image into figure div (image hidden by css)
    $fig_div.append(img_l);
    $fig_div.imagesLoaded(function(){

      img_l_h = img_l.get(0).naturalHeight;
      img_l_w = img_l.get(0).naturalWidth;

      $fig_div.data({
        'img_l_w' : img_l_w,
        'img_l_h' : img_l_h,
        'img_m_w' : img_m_w,
        'img_m_h' : img_m_h
      });
      if (img_l_h < img_m_h) { // large image smaller than resized medium image
        img_l.css({
          'marginTop' : Math.round(($fig_div.height() - img_l_h) / 2),
          'marginLeft' : Math.round(($fig_div.width() - img_l_w) / 2)
        }); // center
      } else {// set large image dimensions and position to that of the medium image
        img_l.css({
          'height' : img_m_h,
          'marginTop' : img_m.css('marginTop'),
          'marginLeft' : img_m.css('marginLeft')
        });
        $fig_div.addClass('zoom'); // zoomable & draggable
      }

      $fig_div.html(img_l.removeClass('invisible'));

      drag_bx = $('<div class="drag-bx" />'); // insert drag containment element

      $fig_div.wrapInner(drag_bx);

      if ($fig_div.hasClass('zoom')) {

        FVZoomControls($fig_div);
      }
    });

  };

  /*zoom figure, initiate dragging
   this is called following either a large image that is bigger than the slide div has finished loading
   OR navigating to a slide whose large image is bigger than the slide and has already loaded.*/

  FVZoomControls = function($fig_div) {
    var $img, fig_div_h, fig_div_w, real_h, real_w, img_aspect_ratio, img_mt, img_ml, resize_h, drag;

    $img = $fig_div.find('img');
    fig_div_h = $fig_div.height();
    fig_div_w = $fig_div.width();
    real_h = $fig_div.data('img_l_h'); // native height of image
    real_w = $fig_div.data('img_l_w'); // native width of image
    img_mt = parseInt($img.css('marginTop')); // top margin of sized to fit image
    img_ml = parseInt($img.css('marginLeft')); // left margin of sized to fit image
    img_aspect_ratio = real_w/real_h; //
    resize_h = $img.height(); // height of sized to fit image
    $FV.zoom.show();
    drag = false; // dragging not enabled
    var $drgbx = $fig_div.find('div.drag-bx');

    $FV.zoom.sldr.slider({
      min: resize_h,
      max: real_h,
      value: resize_h,
      slide: function(e, ui) {
        imgResize(ui.value, img_aspect_ratio);
      },
      stop: function(e, ui) {
        if (!drag) {
          // enable dragging
          FVDragInit($fig_div, $img);
        }
        if (ui.value === resize_h) { // slider is at minimum value
          // kill drag
          FVDragStop($fig_div, $img);
          drag = false;
        } else {
          //FVSizeDragBox($fig_div, $img);
          drag = true;
        }
      }
    });

    // max(+) button
    $FV.zoom.max.on('click', function() {
      var curr_height = $FV.zoom.sldr.slider("value");
      var new_height = resize_h + ((real_h - resize_h) / 4) * Math.ceil((curr_height - resize_h) * 4 / (real_h - resize_h) + 0.1);
      new_height = Math.min(Math.ceil(new_height), real_h);

      $FV.zoom.sldr.slider({
        'value': new_height
      });

      imgResize(new_height, img_aspect_ratio);
      FVDragInit($fig_div, $img);
      // it is better to set dragbox at same time as image is set, ie in imgResize
      // also, I don't think the drag box needs to be sized
      // FVSizeDragBox($fig_div, $img, new_width);
      drag = true;
      new_height = null;
      //curr_height = null;
    });

    // min(-) button
    $FV.zoom.min.on('click', function() {

      if (!drag) {
        return false;
      }
      var curr_height = $FV.zoom.sldr.slider("value");
      var new_height = resize_h + (real_h - resize_h) / 4 * Math.floor((curr_height - resize_h) * 4 / (real_h - resize_h) - 0.1);
      new_height = Math.max(Math.floor(new_height), resize_h);

      $FV.zoom.sldr.slider('value', new_height );

      if (new_height <= resize_h) {
        $img.css({
          'height': new_height,
          'marginTop': img_mt,
          'marginLeft': img_ml
        });
        FVDragStop($fig_div, $img);
        drag = false;
      }
      else {
        imgResize(new_height, img_aspect_ratio);
      }
      new_height = null;

    });

    var imgResize = function(new_height, aspect_ratio) {
      var new_width = new_height * aspect_ratio;
      var mar_top = Math.round((fig_div_h - new_height) / 2);
      var mar_left = Math.round((fig_div_w - new_width) / 2);
      var $drgbx = $fig_div.find('div.drag-bx');

      if (fig_div_w >= new_width) {
        $img.css({
          'height': new_height,
          'marginTop': img_mt - Math.round((new_height - resize_h) / 2),
          'marginLeft': img_ml - Math.round((new_height - resize_h) / 2 * (real_w / real_h))
        });
        /* $drgbx.css({top: (img_mt - Math.round((new_height - resize_h) / 2)) * -1 ,
         left: (img_ml - Math.round((new_height - resize_h) / 2 * (real_w / real_h)) * -1)
         });*/
      } else {
        $img.css({
          'height': new_height,
          'marginTop': mar_top,
          'marginLeft' : mar_left
        });
        /*$drgbx.css({
         top: mar_top,
         left: mar_left * -1
         });*/
      }

    };

    $fig_div.on('DOMMouseScroll mousewheel', function (event) {
      var new_value = $FV.zoom.sldr.slider("value");
      if (event.originalEvent.wheelDeltaY < 0) {
        new_value = Math.min(new_value + 20, real_h);
        imgResize(new_value, img_aspect_ratio);
        $FV.zoom.sldr.slider('value', new_value );
      }
      else if (event.originalEvent.wheelDeltaY > 0) {
        new_value = Math.max(new_value - 20, resize_h);
        imgResize(new_value, img_aspect_ratio);
        $FV.zoom.sldr.slider('value', new_value );
      }
    });
  };

  FVDragInit = function($fig_div, $img) {
    $img.draggable({
      // containment: 'parent',
      stop: function(e, ui) {
        // FVSizeDragBox() sets top/left values to match margins of figure
        // following dragging these are no longer in sync, top/left have changed
        // storing the difference between values, (if no dragging value is 0)
        // when a figure is resized the margins are updated and then the drag box is resized
        // to calculate dimensions/position of resized drag bax, the difference in values prior to figure resize is required

        $fig_div.data({
          'off-top': Math.abs(parseInt($img.css('marginTop'))) - parseInt($img.css('top')),
          'off-left': Math.abs(parseInt($img.css('marginLeft'))) - parseInt($img.css('left'))
        });
      }
    });
  };

  FVDragStop = function($fig_div, $img) {
    $img.draggable('destroy');
    // reset
    $img.css({'top' : 0, 'left' : 0});
   // $fig_div.find('div.drag-bx').removeAttr('style');
    $fig_div.data({
      'off-top': 0,
      'off-left': 0
    });
  };

// Size & position div to contain figure dragging
// adds top/left declarations to image so that image remains in same position following containing div sizing/positioning

  FVSizeDragBox = function($fig_div, $img, img_w) {
    var $drgbx, fig_h, fig_w, img_h, /*img_w,*/ img_mt, img_ml, img_tp, img_lt;

    $drgbx = $fig_div.find('div.drag-bx');
    fig_h = $fig_div.height();
    fig_w = $fig_div.width();
    img_h = $img.height();
    img_w = $img.width();
    img_mt = parseInt($img.css('marginTop'));
    img_ml = parseInt($img.css('marginLeft'));
    img_tp = isNaN(parseInt($img.css('top'))) ? 0 : parseInt($img.css('top'));
    img_lt = isNaN(parseInt($img.css('left'))) ? 0 : parseInt($img.css('left'));

    if (fig_h > img_h) {
      $drgbx.css({
        'top' : img_mt * -1,
        'height': fig_h + img_mt
      });
      $img.css({
        'top' : img_mt - $fig_div.data('off-top')
      });
    } else {
      $drgbx.css({
        'top' :  (img_h - fig_h + img_mt) * -1,
        'height': ((img_h * 2) - fig_h) + img_mt
      });
      $img.css({
        'top' : img_h - fig_h + img_mt - $fig_div.data('off-top')
      });
    }
    if (fig_w > img_w) {
      $drgbx.css({
        'left' : img_ml * -1,
        'width': fig_w + img_ml
      });
      $img.css({
        'left' : img_ml - $fig_div.data('off-left')
      });
    } else {
      $drgbx.css({
        'left' : (img_w - fig_w + img_ml) * -1,
        'width': img_w //((img_w * 2) - fig_w) + img_ml
      });
      $img.css({
        'left' : img_w - fig_w + img_ml - $fig_div.data('off-left')
      });
    }

  };

  var FVClose = function() {

    //re-enable scrolling
    $('body').css('overflow','auto').off('touchmove');
    //reset the foundation tabs
    $('.fv-nav').find('li').removeClass('active');

    //unbind the resizing and the arrow key bindings
    $(window).off('resize.modal keydown');

    //will record the timeStamp for when the modal is closed
    if(typeof event !== 'undefined') {
      close_time = event.timeStamp;
    }

    $FVPending = false;
    $FV.empty();
    $('#article-lightbox').foundation('reveal', 'close');
  };

  $(document).on('keydown', function (e) {
    //27 = escape key
    if (e.which == 27) {
      FVClose();
    }
  });

  function trimIt (trimItem) {
    if (typeof String.prototype.trim !== 'function') {
      String.prototype.trim = function () {
        return this.replace(/^\s+|\s+$/g, '');
      }
    }  else {
      return trimItem.trim();
    }
  }

  function lightboxOpeners() {

    // carousel images on article page
    // this triggers a click on the corresponding lightbox thumbnail
    var $fig_carousel = $('#figure-carousel').find('div.carousel-item');
    if ($fig_carousel.length) {

      $fig_carousel.on('click', function (e) {

        get_ref = $(this).data('doi');

        get_doi = get_ref.slice(0, -5);

        lightbox.FigViewerInit(get_doi, get_ref, 'figs');

        e.preventDefault();

      });
    }

    // article inline images
    // this triggers a click on the corresponding lightbox thumbnail
    var $fig_inline = $('#artText').find('div.img-box');
    if ($fig_inline.length) {

      $fig_inline.on('click', function (e) {

        var $href = $(this).find('a');

        get_ref = $href.data('uri'); //image reference

        get_doi = $href.data('doi');

        lightbox.FigViewerInit(get_doi, get_ref, 'figs');

        e.preventDefault();
      });
    }

    // 'figure' item in article floating nav
    var $nav_figs = $('#nav-figures').find('a');
    if ($nav_figs.length) {
      $nav_figs.on('click', function (e) {

        var doi = $nav_figs.data('doi');

        lightbox.FigViewerInit(doi, null, 'figs');

        e.preventDefault();
      });
    }
    /*
     Sitewide links: hidden for initial article page release

     // links on external pages: figure search results
     var $fig_results = $('#fig-search-results, .article-block .actions, #subject-list-view .actions');
     if ($fig_results.length) {
     $fig_results.find('a.figures').on('click', function (e) {

     var doi = $(this).data('doi');

     lightbox.FigViewerInit(doi, null, 'abst', true);

     e.preventDefault();
     });
     }

     // links on external pages: figure link in the toc
     var $toc_block_links = $('#toc-block div.links');
     if ($toc_block_links.length) {
     $toc_block_links.find('a.figures').on('click', function () {
     var doi = $(this).data('doi');
     FigViewerInit(doi, null, 'figs', true);
     });

     $toc_block_links.find('a.abstract').on('click', function () {
     var doi = $(this).data('doi');
     lightbox.FigViewerInit(doi, null, 'abst', true);
     });
     }

     */


  }
  lightboxOpeners();

})(jQuery);