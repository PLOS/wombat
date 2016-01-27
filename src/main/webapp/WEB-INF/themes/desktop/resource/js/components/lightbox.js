/**
 *
 * DEPENDENCIES:  resource/js/vendor/underscore
 *                resource/js/vendor/jquery
 *                resource/js/vendor/jquery.panzoom
 *                resource/js/vendor/jquery.dotdotdot
 *                resource/js/vendor/foundation
 *
 */
var FigureLightbox = {};
(function($) {

  FigureLightbox = {
    lbContainerSelector:     '#figure-lightbox-container',

    /* internal selectors */
    lbSelector:               '#figure-lightbox',
    lbTemplateSelector:       '#figure-lightbox-template',
    contextTemplateSelector:  '#image-context-template',
    lbCloseButtonSelector:    '#figure-lightbox .lb-close',
    zoomRangeSelector:        '#figure-lightbox .range-slider',
    $panZoomEl:               null,
    imgData:                  null,


    /* internal config variables */
    imgPath:        WombatConfig.figurePath || 'IMG_PATH_NOT_LOADED'
  };

  FigureLightbox.insertLightboxTemplate = function () {
    var articleData = this.fetchArticleData();

    var lbTemplate = _.template($(this.lbTemplateSelector).html());
    $(this.lbContainerSelector).append(lbTemplate(articleData));
  };

  FigureLightbox.fetchArticleData = function () {
    // @TODO: Do not parse article. Fetch data via an ajax call
    var $mainContainer = $(document).find('main');
    return {
      doi: this.imgData.doi,
      strippedDoi: this.imgData.strippedDoi,

      articleTitle: $mainContainer.find('#artTitle').text(),
      authorList: $mainContainer.find('.author-name').text(),
      figureList: this.imgList
    };
  };

  FigureLightbox.fetchImageData = function () {
    return {
      doi: this.imgData.doi,
      strippedDoi: this.imgData.strippedDoi,

      title: this.imgData.imgElement.find('.figcaption').text(),
      description: this.imgData.imgElement.find('.caption_target').next().html()
    };
  };


  FigureLightbox.bindBehavior = function () {
    var that = this;
    // Escape key destroys and closes lightbox
    $(document).on('keyup.figure-lightbox', function(e) {
      if (e.keyCode === 27) {
        that.close();
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
          if (!$figList.is(':visible')) { // If not is visible show it
            $figList.show();
            var tmpPos = $figList.position();
            $figList.css({right: tmpPos.left - screen.width});
          }
          var end = $figList.position();
          // Get the static position
          $figList.animate({ // Animate toggle to the right
            right: end.left - screen.width
          });
        }).end()

        // Bind mousewheel in figure list. Prevent image zooming
        .find('#figures-list').on('mousewheel', function(e) {
          e.stopPropagation();
        }).end()

        // Bind show in context button
        .find('.show-context').on('click', function () {
          that.close();
        }).end()

        // Bind next figure button
        .find('.next-fig-btn').on('click', function () {
          return that.nextImage();
        }).end()

        // Bind next figure button
        .find('.prev-fig-btn').on('click', function () {
          return that.prevImage();
        }).end()

        .find('#view-more').on('click', function () {
          $('#view-more-wrapper').hide();
        }).end()

        .on('image-switch', function (e, data) {
          var buttons = $(that.lbSelector).find('.fig-btn').show();
          if (data.index === 0) {
            buttons.filter('.prev-fig-btn').hide(); // Hide prev button
          } else if (data.index === (that.imgList.length - 1)) {
            buttons.filter('.next-fig-btn').hide(); // Hide next button
          }

          $(that.lbSelector).find('#view-more, #view-less').on('click', function () {
            $('#image-context').toggleClass('full-display')
                .children(':not(.full-display-show)').toggle();
            $('#view-more-wrapper').slideToggle();
            $('#view-less-wrapper').slideToggle('slow');

          });
        });
  };

  FigureLightbox.nextImage = function () {
    var newIndex = this.getCurrentImageIndex() + 1;
    var nextImg = this.imgList[newIndex];
    if (!nextImg) {
      return false;
    }
    this.switchImage(nextImg.getAttribute('data-doi'));
  };

  FigureLightbox.prevImage = function () {
    var newIndex = this.getCurrentImageIndex() - 1;
    var prevImg = this.imgList[newIndex];
    if (!prevImg) {
      return false;
    }
    this.switchImage(prevImg.getAttribute('data-doi'));
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

  FigureLightbox.switchImage = function (imgDoi) {
    this.imgData = {
      doi: imgDoi
    };
    this.imgData.strippedDoi = this.imgData.doi.replace(/^info:doi\//, '');
    var currentIndex = this.getCurrentImageIndex();
    this.imgData.imgElement = $(this.imgList[currentIndex]);
    // Get data to populate image context
    var imageData = this.fetchImageData();
    var templateFunctions = {
      showInContext: this.showInContext
    };
    var templateData = $.extend(imageData, templateFunctions);
    var lbTemplate = _.template($(this.contextTemplateSelector).html());
    // Remove actual img context
    $(this.lbSelector + ' #image-context').children().remove().end()
        // Append new img context
        .append(lbTemplate(templateData));
    this.renderImg(this.imgData.doi);
    $(this.lbSelector + ' #view-more-wrapper').dotdotdot({after: '#view-more'});

    $(this.lbContainerSelector).trigger('image-switch', {index: currentIndex, element: this.imgData.imgElement});
  };

  FigureLightbox.isInited = function () {
    return $(this.lbContainerSelector).data('is-inited');
  };

  FigureLightbox.loadImage = function (lbContainer, imgDoi, cb) {
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
    var $image = $(this.lbSelector).find('img.main-lightbox-image').attr('src', this.buildImgUrl(imgDoi));
    this.panZoom($image);

    // Reinitialize sliders
    $(document).foundation('slider', 'reflow');
  };


  FigureLightbox.close = function () {
    this.destroy();
    $(this.lbSelector).foundation('reveal', 'close');
  };

  FigureLightbox.showInContext = function (imgDoi) {
    imgDoi = imgDoi.split('/');
    imgDoi = imgDoi[1].slice(8);
    imgDoi = imgDoi.replace(/\./g,'-');
    return '#' + imgDoi;
  };

  FigureLightbox.panZoom = function ($image) {
    var that = this;
    this.$panZoomEl = $image.panzoom();

    /* Bind panzoom and slider to mutually control each other */
    this.bindPanZoomToSlider();
    this.bindSliderToPanZoom();

    this.$panZoomEl.parent().on('mousewheel.focal', function(e) {
      e.preventDefault();
      var delta = e.delta || e.originalEvent.wheelDelta;
      var zoomOut = delta ? delta < 0 : e.originalEvent.deltaY > 0;
      that.$panZoomEl.panzoom('zoom', zoomOut, {
        increment: 0.05,
        animate: false,
        focal: e
      });
    });
  };

  FigureLightbox.bindPanZoomToSlider = function () {
    var that = this;
    $(this.zoomRangeSelector).off('change.fndtn.slider').on('change.fndtn.slider', function(){
      var panzoomInstance = that.$panZoomEl.panzoom('instance');
      var matrix = panzoomInstance.getMatrix();
      matrix[0] = matrix[3] = this.dataset.slider;
      panzoomInstance.setMatrix(matrix);
    });
  };
  FigureLightbox.bindSliderToPanZoom = function () {
    var that = this;
    this.$panZoomEl.on('panzoomzoom', function(e, panzoom, scale) {
      $(that.zoomRangeSelector).foundation('slider', 'set_value', scale);
      // Bug in foundation unbinds after set_value. Workaround: rebind everytime
      that.bindPanZoomToSlider();
    });
  };


  FigureLightbox.buildImgUrl = function (imgDoi, options) {
    var defaultOptions = {
      path: this.imgPath,
      size: 'large'
    };
    options = _.extend(defaultOptions, options);
    return options.path + '?size=' + options.size + '&id=' + imgDoi;
  };

  FigureLightbox.destroy = function () {
      // @TODO: Check when to destroy modal with images
/*    $(this.lbContainerSelector)
      // Unbind close button
        .find(this.lbCloseButtonSelector).off('click').end()
      // Unbind buttons to change images
        .find('.change-img').off('click').end()
      // Unbind button to show all images
        .find('.all-fig-btn').off('click').end()
        .off('image-switch');*/
  };

  })(jQuery);