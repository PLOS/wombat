
//dependencies: jquery.touchswipe.js, foundation tabs, foundation reveal

(function($) {

  var $FV, $FVPending, selected_tab, $win, FigViewerInit, FVBuildHdr, FVBuildAbs, FVBuildRefs, FVDisplayPane, FVBuildFigs, FVSize, FVChangeSlide, FVArrowKeys, FVFigDescripton, FVThumbPos, FVDisplayFig, FVLoadMedImg, FVLoadLargeImg, FVSizeImgToFit, FVSwitchImg, FVFigFunctions, FVDragInit, FVDragStop, FVSizeDragBox, displayModal, get_ref, get_doi;

  $FV = {};
  $FVPending = false;
  selected_tab = $('.tab-title.active').attr('id');
  $win = $(window);

  // FigViewerInit is initiated when user clicks on anything to open the lightbox. Click events are at the bottom of this page.
  // ref=src of specific figure clicked on; if not specific figure, is set to null
  // state=abst, figs, or refs; external_page = true if not on article page
  FigViewerInit = function(doi, ref, pane, external_page) {
    var findActive, rerunMathjax, loadJSON;

    //disable scrolling on web page behind fig viewer
    $('body').css('overflow', 'hidden');
    $('body').on('touchmove', function(e){e.preventDefault()});

    // allow only one instance of FigViewerInit to be pending.
    if ($FVPending) {
      return;
    }
    $FVPending = true;

    //find the container divs in figviewer.ftl
    $FV = $('#fig-viewer');
    $FV.cont = $('#fig-viewer-content');
    $FV.nav = $('.fv-nav');

    findActive = $('.fv-nav').find('li');

    //highlight the active nav item. even though foundation tabs is used for navigation when the lightbox is open,
    // the initial tab needs to be registered manually: this part is not handled by foundation.
    $.each(findActive, function(){
      var activateLi = $(this).hasClass(pane);
      if (activateLi === true){
        $(this).addClass('active');
      }
    });

    $(findActive).on('click', function(){
      FVDisplayPane(this.className);
    });
    if (ref) {
      ref = ref.slice(9);
    }  else {
      ref = null;
    }

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

      var apiurl = siteUrlPrefix + 'article?id=' + doi;
      //var apiurl = '/article/lightbox.action?uri=' + doi;

      $.ajax({
        url:apiurl,
        dataFilter:function (data, type) {
          return data.replace(/(^\/<\*|\*\/$)/g, '');
        },
        dataType:'html',
        error: function (jqXHR, textStatus, errorThrown) {
          //TODO: change this
          console.log(errorThrown);

          $FVPending = false;
        },
        success:function (data) {
          var article_title, authors, auth_list, article_body;

          article_title = $('#artTitle').text();

          authors = $('#author-list').find('.author-name');

          auth_list = $(authors).text();

          FVBuildHdr(article_title, auth_list, doi);

          FVBuildFigs(data, doi);

          // from article tab where references,abstract and metadata exists, no need to fetch
          // them again from the server.
          // if (typeof selected_tab != 'undefined' && selected_tab == 'tabArticle') {
          $FVPending = false;
          article_body = $('#artText');
          FVBuildAbs(doi, $(article_body).find('.abstract'), $(article_body).find('.articleinfo'));

          FVBuildRefs($(article_body).find('.references'));

          displayModal();

          //rerunMathjax();
          //}
          /* else {

           var article, articleUrl = $win.href + '/article?id=' + doi;console.log(articleUrl);
           console.log('fetch full article: ' + articleUrl);
           $.ajax({
           url: articleUrl,
           success: function(fullArticleHtml) {
           $FVPending = false;
           article = $(fullArticleHtml);

           FVBuildAbs(doi, article.find('.article .abstract'), article.find('.article .articleinfo'));
           FVBuildRefs(article.find('ol.references'));
           displayModal();
           //rerunMathjax();
           },
           error: function() {
           $FVPending = false;
           }});
           }*/
        }
      });
    };
    displayModal = function () {

      if(typeof(_gaq) !== 'undefined'){
        _gaq.push(['_trackEvent',"Lightbox", "Display Modal", ""]);
      }

      FVSize();
      FVDisplayPane(pane);

      $FV.removeClass('abst figs refs').addClass(pane);

      if (pane === 'figs') {$('#panel-figs').addClass('active');}
      // debounce resize event
      var resizeDelay;
      $win.on('resize.modal', function() {
        clearTimeout(resizeDelay);
        resizeDelay = setTimeout(function() {
          FVSize();
        }, 100);
      });
    };

    loadJSON();
  };



  FVSize = function () {

    var win_h, frame_h, hdr_h, fig_h, data_h, parts_h;
    //  based on the browser window height
    win_h = $win.height();
    frame_h = 20; //account for the 10 pixel border
    var main_h = win_h - frame_h;
    //Set the height of the fig-viewer container: window height - border size
    $FV.cont.css('height', main_h);

    hdr_h = 46; //height of title & nav header
    data_h = 120; // height of bottom controls
    $('.tabs-content').css('height', main_h - hdr_h);
    $('#panel-figs').css('height', main_h - hdr_h);
    // set the height of the figure divs: window - border -
    parts_h = hdr_h + data_h + frame_h;
    fig_h = win_h - parts_h;     //console.log(fig_h);
    //figure height is window minus frame, header, frame, and data heights
    $FV.figs.css('height', fig_h);
    $FV.thumbs_cont.css('height', fig_h - parseInt($FV.thumbs_el.css('paddingTop')));
    $FV.abst_pane.css('height', win_h - frame_h - hdr_h - 1);

    $FV.refs_pane.css('height', win_h - frame_h - hdr_h);

    if ($FV.thmbs_vis) {
      FVThumbPos($FV.thumbs.active);

    }

  };

  // build header elements
  FVBuildHdr = function(title, authors, articleDoi) {
    var authArray, articleLink, h1, authorList, trimAuth, auth;

    $FV.hdr = $('.fv-title');

    authArray = authors.split(',');

    if ($FV.external_page) {
      articleLink = "http://dx.plos.org/" + articleDoi.replace("info:doi/", "");
      h1 = '<a href="' + articleLink + '">' + title + '</a>';

    } else {

      h1 =  title;
    }

    $('#fvTitle').html(h1);
    authorList = $('#fvAuthors');
    var get_last = authArray.length-1;

    $.each(authArray, function (index, author) {

      trimAuth = trimIt(author);

      if (index !== get_last) {
        auth = $('<span>' + trimAuth + ', </span>');
      } else {
        auth = $('<span>' + trimAuth + ' </span>');
      }

      $(auth).appendTo(authorList);

    });

  };

  // build figures pane
  FVBuildFigs = function(data, doi) {
    var path, showInContext, fig_container, title_txt, image_title, img_ref, $thmb, slide, datacon, txt, txt_less, txt_more, title, toggleLess, context_hash, doip, $fig, staging, dl, context_lnk, text_description;

    $FV.figs_pane = $('<div id="fig-viewer-figs" class="pane" />');
    $FV.thumbs_el = $('<div id="fig-viewer-thmbs" />');
    $FV.thumbs_cont = $('<div id="fig-viewer-thmbs-content" />');
    $FV.controls_el = $('<div id="fig-viewer-controls" />');
    $FV.slides_el = $('<div id="fig-viewer-slides" />');
    $FV.staging_el = $('<div class="staging" />'); // hidden container for loading large images
    $FV.figs_set = [];  // all figures array
    path = siteUrlPrefix+'article/figure/image?size=';

    // /wombat/DesktopPlosPathogens/article/figure/image?size=inline&amp;id=info:doi/10.1371/journal.ppat.1000621.g001
    //http://localhost:8081/wombat/DesktopPlosMedicine/article/figure/image?size=large&id=info:doi/10.1371/journal.pmed.0010019.t001
    showInContext = function (uri) {
      uri = uri.split('/');
      uri = uri[1].slice(8);
      uri = uri.replace(/\./g,'-');
      return '#' + uri;
    };

    fig_container = $('.figure');

    $.each(fig_container, function () {

      //title_txt = ( ? '<strong>' +this.title + ':</strong> ' : '') + this.transformedCaptionTitle;
      // CAPTION text handling is here
      title_txt = $(this).find('.figcaption');
      //the image long description is hard to find in the article-transform.xml. the following method of finding the text might be brittle.
      text_description = $(this).find('.caption_target').next().html();
      image_title = $(title_txt).html();// + ' ' + this.plainCaptionTitle;
      img_ref = $(this).data('doi');
      context_hash = showInContext(img_ref);

      $thmb = $('<div class="thmb"' + ' data-uri="' + img_ref + '"><div class="thmb-wrap"><img src="' + path +'inline&id=info:doi/'+ img_ref + '" alt="' + image_title + '" title="' + image_title + '"></div></div>').on('click', function () {
        FVChangeSlide($(this));
        FVArrowKeys($(this));
      });
      $FV.thumbs_cont.append($thmb);
      slide = $('<div class="slide" />');
      datacon = $('<div class="datacon" />');
      txt = $('<div class="txt" />');
      txt_less = $('<div class="text-less" />');
      txt_more = $('<div class="text-more" />');
      title = '<div class="fig_title">' + image_title + '</div>';
      toggleLess = $('<div class="toggle less" title="view less" />');

      /* save this for later: first version is for article page only
       if ($FV.external_page) { // the image is on another page
       context_hash = '/article/' + $FV.url + context_hash;
       }*/

      doip = '<p class="doi">doi:'+img_ref+'</p>';

      // build div with the references of medium & large versions in data attributes
      $fig = $('<div class="figure" data-img-src="' + path+ 'medium&id=info:doi/' +img_ref  + '" data-img-lg-src="' + path +'large&id=info:doi/' +img_ref+'" data-img-txt="' + image_title + '"></div>');

      $fig.data('state', 0) // track image loading state of figure
        .data('off-top', 0)
        .data('off-left', 0);
      $FV.figs_set.push($fig);

      staging = '<div class="staging" />'; // hidden container for loading large image

      dl = '<div class="download">'
        + '<h3>Download:</h3>'
        + '<div class="item"><a href="' + "article/figure/powerpoint?id=info:doi/" + img_ref + '" title="PowerPoint slide"><span class="btn">PPT</span></a></div>'
        + '<div class="item"><a href="' + "article/figure/image?size=large&id=info:doi/" + img_ref + '" title="large image"><span class="btn">PNG</span><span class="size">' + /*convertToBytes(this.sizeLarge)*/  '</span></a></div>'
        + '<div class="item"><a href="' + "article/figure/image?size=original&id=info:doi/" + img_ref + '" title="original image"><span class="btn">TIFF</span><span class="size">' + /*convertToBytes(this.sizeTiff)*/  '</span></a></div>'
        + '</div>';

      context_lnk = '<a class="btn lnk_context close-reveal-modal" href="' + context_hash + '">Show in Context</a>';

      slide.append($fig);
      slide.append(staging);
      txt_less.append(title);
      txt_more.append(toggleLess);
      txt_more.append(title);

      if (text_description !== null) {
        txt_more.append('<div class="desc">' + text_description + '</div>');
        txt_less.append('<div class="desc">' + text_description + '</div>');
      }

      txt_more.append(doip);
      txt.append(txt_less);
      txt.append(txt_more);
      datacon.append(txt);
      datacon.append(context_lnk);
      datacon.append(dl);
      slide.append(datacon);
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

    // figures controls in control bar
    $('<span class="fig-btn thmb-btn"><i class="icn"></i> All Figures</span>').on('click',function() {
      $FV.figs_pane.toggleClass('thmbs-vis');
      $FV.thmbs_vis = $FV.thmbs_vis ? false : true;
      FVThumbPos($FV.thumbs.active);
    }).appendTo($FV.controls_el);

    $FV.nxt = $('<span class="fig-btn next invisible"><i class="icn"></i> Next</span>');

    $FV.nxt.on('click',function() {
      FVChangeSlide($FV.thumbs.active.next());
    }).appendTo($FV.controls_el);

    $FV.prv = $('<span class="fig-btn prev invisible"><i class="icn"></i> Previous</span>');

    $FV.prv.on('click',function() {
      FVChangeSlide($FV.thumbs.active.prev());
    }).appendTo($FV.controls_el);

    $FV.thumbs_el.append($FV.thumbs_cont);
    $FV.slides = $FV.slides_el.find('div.slide'); // all slides
    $FV.figs = $FV.slides_el.find('div.figure'); // all figures
    $FV.thumbs = $FV.thumbs_el.find('div.thmb'); // all thumbnails
    $FV.thumbs.active = null; // used to track active thumb & figure

    $FV.loading = $('<div class="loading-bar"></div>').appendTo($FV.controls_el);
    $FV.zoom = $('<div id="fv-zoom" />');
    $FV.zoom.min = $('<div id="fv-zoom-min" />').appendTo($FV.zoom);
    $FV.zoom.sldr = $('<div id="fv-zoom-sldr" />').appendTo($FV.zoom);
    $FV.zoom.max = $('<div id="fv-zoom-max" />').appendTo($FV.zoom);
    $FV.controls_el.append($FV.zoom);



    $FV.slides_el.append($FV.controls_el);
    $FV.figs_pane.append($FV.slides_el);
    $FV.figs_pane.append($FV.thumbs_el);
    $FV.figs_pane.append($FV.staging_el);

    $('#panel-figs').append($FV.figs_pane);
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

  };

  // build abstract pane
  FVBuildAbs = function(doi, abstractText, metadata) {
    $FV.abst_pane = $('<div id="fig-viewer-abst" class="pane" />');
    var lnk_pdf, pdf_href, $abst_info, $abst_content = $('<div class="abstract" />');

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

    $('#panel-abst').append($FV.abst_pane);

  };

  // build references pane
  FVBuildRefs = function(references) {
    var refs_content = references.html();

    $FV.refs_pane = $('<div id="fig-viewer-refs" class="pane"/>');
    $FV.refs_pane.append('<h3>References</h3>');
    $FV.refs_pane.append('<ol class="references">'+ refs_content +'</ol>');
    $('#panel-refs').append($FV.refs_pane);
  };

  // add panel name to fig-viewer tag & display figure chosen (if on figs panel)
  FVDisplayPane = function(pane) {
    $FV.removeClass('abst figs refs').addClass(pane);

    if (pane == 'figs') {

      if ($FV.thumbs.active == null) { // no thumb is active so this is the 1st figure displayed

        if ($FV.figs_ref) { // specific figure is requested
          $('#fig-viewer').foundation('reveal', 'open');
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

  // display figure slides functionality
  FVChangeSlide = function($thmb) {

    if(typeof(_gaq) !== 'undefined'){
      _gaq.push(['_trackEvent',"Lightbox", "Slide Changed", ""]);
    }

    if ($FV.thumbs.active !== null) { // not the initial slide

      var old_fig, old_img, i, this_sld;
      $FV.thumbs.active.removeClass('active');
      old_fig = $FV.figs_set[$FV.thumbs.index($FV.thumbs.active)];
      old_img = old_fig.find('img');

      if (old_img.hasClass('ui-draggable')) { // the slide we are leaving had a drag-enabled figure, reset it
        FVDragStop(old_fig, old_fig.find('img'));
      }
    }
    $FV.thumbs.active = $thmb;
    $FV.thumbs.active.addClass('active');

    $FV.slides.hide();

    i = $FV.thumbs.index($thmb);
    this_sld = $FV.slides.eq(i);// console.log(this_sld);

    this_sld.show();
    FVDisplayFig(i);
    FVFigDescripton(this_sld);

    $FV.thumbs.active.next().length ? $FV.nxt.removeClass('invisible') : $FV.nxt.addClass('invisible');
    $FV.thumbs.active.prev().length ? $FV.prv.removeClass('invisible') : $FV.prv.addClass('invisible');
    if ($FV.thmbs_vis) {
      FVThumbPos($thmb);
    }

  };

  FVArrowKeys = function() {
    // arrow keys operate the next / previous slide
    // keydown is unbound in FVClose() so up/down works on web page
    $(this).on('keydown', function (e) {// this = window, .on calls jquery
      // left / up arrow keys
      if (e.which == 37 || e.which == 38) {
        if ($FV.thumbs.active.prev().length) {
          var t = $FV.thumbs.active.prev();
          FVChangeSlide(t);
        }
        return false;
      }
      // right / down arrow keys
      if (e.which == 39 || e.which == 40) {
        if ($FV.thumbs.active.next().length) {

          var t = $FV.thumbs.active.next();
          FVChangeSlide(t);
        }
        return false;
      }
    });
  };

  // figure description expand/reduce controls
  // called in FVChangeSlide
  FVFigDescripton = function(sld) {

    var $btn_less, $content, truncate, desc_link;
    $btn_less = sld.find('div.less');
    $content = sld.find('div.text-less');

    truncate = function() {

      //If called on the same element twice, igntruecond call
      if($content.data('ellipsis_appended') !== true) {

        $content.ellipsis({ ellipsis_text:'<span class="toggle more">... show more</span>' });

        $content.find('span.more').click(function() {
          $FV.slides_el.addClass('expanded');
          $FV.txt_expanded = true;
        });
        $content.data('ellipsis_appended','true');
      }
    };

    // check display of descriptions
    if (!$FV.txt_expanded) { // landed on slide and descriptions are hidden.
      truncate();
      $btn_less.click(function() {
        $FV.slides_el.removeClass('expanded');
        $FV.txt_expanded = false;
      });
    } else { // landed on this slide and descriptions are visible.
      // truncate following description reveal
      $btn_less.click(function() {
        $FV.slides_el.removeClass('expanded');
        $FV.txt_expanded = false;
        truncate();
      });
    }

    //close lightbox if link in description is clicked on
    desc_link = sld.find('.desc').children('a');

    $(desc_link).on('click',function(){
      $('#fig-viewer').foundation('reveal', 'close');
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
  FVDisplayFig = function(i) {
    $FV.loading.show();
    $FV.zoom.hide();
    var this_fig = $FV.figs_set[i];
    var $img = this_fig.find('img');
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
        FVLoadMedImg(i);
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
  FVLoadLargeImg = function(i) {      ///////////////////// PROBLEM MIGHT BE HERE
    var src = $FV.figs_set[i].data('img-lg-src');
    var txt = $FV.figs_set[i].data('img-txt');
    //console.log(src);
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
    var img, el_h, el_w, i_h, i_w;
    img = el.find('img');
    el_h = el.height();
    el_w = el.width();
    i_h = img.height();
    i_w = img.width();
    ///////////////////// PROBLEM MIGHT BE HERE
    //console.log(el_h+ ' ' +el_w+ ' ' +i_h+ ' ' +i_w);
    //console.log(el_w / el_h +' '+ i_w / i_h);
    // sizes image to fit, scaling up or down, and centering
    // setting size with height
    //console.log(Math.round((el_w -  img.width()) / 2));
    //console.log(Math.round(el_w * (i_h / i_w)));
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
    };

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
    var $img_m, img_m_h, $img_l, img_l_h, img_l_w, drag_bx;
    $img_m = $fig.find('img');
    img_m_h = $img_m.height();
    $img_l = $fig.next('div.staging').find('img');
    // move large image into figure div (image hidden by css)
    $fig.append($img_l);
    //img_l_erg = $fig.find($img_l); console.log(img_l_erg);
    img_l_h = $img_l.height();//css('height');
    img_l_w = $img_l.width();
    // store native dimensions
    $fig.data('img_l_w', img_l_w).data('img_l_h', img_l_h);

    if (img_l_h < img_m_h) { // large image smaller than resized medium image
      $img_l.css({'marginTop' : Math.round(($fig.height() - img_l_h) / 2), 'marginLeft' : Math.round(($fig.width() - img_l_w) / 2)}); // center
    } else {
      $img_l.css({'height' : img_m_h, 'marginTop' : $img_m.css('marginTop'), 'marginLeft' : $img_m.css('marginLeft')}); // match dimensions and position of medium image
      $fig.addClass('zoom'); // zoomable & draggable
    }
    $fig.html($img_l.removeClass('invisible')); // replace
    drag_bx = $('<div class="drag-bx" />'); // insert drag containment element
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
    var $img, real_h, real_w, img_mt, img_ml, resize_h, drag, $drgbx;
    $img = $fig.find('img');
    real_h = $fig.data('img_l_h'); // native height of image
    real_w = $fig.data('img_l_w'); // native width of image
    img_mt = parseInt($img.css('marginTop')); // top margin of sized to fit image
    img_ml = parseInt($img.css('marginLeft')); // left margin of sized to fit image
    resize_h = $img.height(); // height of sized to fit image
    drag = false; // dragging not enabled
    $drgbx = $fig.find('div.drag-bx');

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
    $img.css({'top' : 0, 'left' : 0});
    $fig.find('div.drag-bx').removeAttr('style');
    $fig.data('off-top', 0)
      .data('off-left', 0);
  };


// Size & position div to contain figure dragging
// adds top/left declarations to image so that image remains in same position following containing div sizing/positioning
// runs following figure resize

  FVSizeDragBox = function($fig, $img) {
    var $drgbx, fig_h, fig_w, img_h, img_w, img_mt, img_ml, img_tp, img_lt;
    $drgbx = $fig.find('div.drag-bx');
    fig_h = $fig.height();
    fig_w = $fig.width();
    img_h = $img.height();
    img_w = $img.width()
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

    //re-enable scrolling
    $('body').css('overflow','auto').off('touchmove');
    //re-set the foundation tabs
    $('.fv-nav').find('li').removeClass('active');
    //reset the figures content
    $('.tabs-content').find('section').removeClass('active').empty();
    //unbind the resizing and the arrow key bindings
    $win.off('resize.modal keydown');

    /*//will record the timeStamp for when the modal is closed
     if(typeof event !== 'undefined') {
     close_time = event.timeStamp;
     }*/

    $('#fig-viewer').foundation('reveal', 'close');

    $FVPending = false;

  };

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
      //  console.log(element);
      //ellipsis added, no need to keep going
      if(ellipsis_added) {
        break;
      }

      //If this is a text node, work on it, otherwise recurse
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
    ellipsis_text : '&hellip;'
  };

//Why is this bound universally?  That seems strange.
//-Joe
  $(document).bind('keydown', function (e) {
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

  function initMainContainer() {

    // carousel images on article page
    // this triggers a click on the corresponding lightbox thumbnail
    var $fig_carousel = $('#figure-carousel').find('div.carousel-item');
    if ($fig_carousel.length) {
      $lnks = $fig_carousel.find('a');
      $fig_carousel.on('click', function (e) {

        get_ref = $(this).data('doi');

        get_doi = get_ref.slice(0, -5);

        FigViewerInit(get_doi, get_ref, 'figs');

        e.preventDefault();

      });
      // $lnks.append('<div class="expand" />');
    }
    // article inline images
    var $fig_inline = $('#artText').find('div.figure');
    if ($fig_inline.length) {
      $lnks = $fig_inline.find('a');
      $lnks.on('click', function (e) {
        get_ref = $(this).data('uri'); //image reference

        get_doi = $(this).data('doi');

        FigViewerInit(get_doi, get_ref, 'figs');

        e.preventDefault();

      });
      // $lnks.append('<div class="expand" />');
    }

    // 'figure' item in article floating nav
    var $nav_figs = $('#nav-figures').find('a');
    if ($nav_figs.length) {
      $nav_figs.on('click', function (e) {
        $('#fig-viewer').foundation('reveal', 'open');

        var doi = $nav_figs.data('doi');

        FigViewerInit(doi, null, 'figs');

        e.preventDefault();

      });
    }

    // links on external pages: figure search results
    var $fig_results = $('#fig-search-results, .article-block .actions, #subject-list-view .actions');
    if ($fig_results.length) {
      $fig_results.find('a.figures').on('click', function (e) {
        var doi = $(this).data('doi');

        FigViewerInit(doi, null, 'abst', true);
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
        FigViewerInit(doi, null, 'abst', true);
      });
    }

    $('.fv-close').on('click',function(){
      FVClose();
    });

  }
  initMainContainer();

})(jQuery);