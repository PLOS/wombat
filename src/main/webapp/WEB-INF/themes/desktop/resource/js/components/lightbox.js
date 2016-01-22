
var FigureLightbox = {};
(function($) {

  FigureLightbox = {
    lbContainerSelector:     '#figure-lightbox-container',

    /* internal selectors */
    lbSelector:               '#figure-lightbox',
    lbTemplateSelector:       '#figure-lightbox-template',
    lbCloseButtonSelector:    '.lb-close',
    zoomRangeSelector:        '.range-slider',
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
    var figureInArticle = $mainContainer.find('.figure[data-doi="' + this.imgData.strippedDoi + '"]');

    return {
      doi: this.imgData.doi,
      title: figureInArticle.find('.figcaption').text(),
      description: figureInArticle.find('.caption_target').next().html(),
      articleTitle: $mainContainer.find('#artTitle').text(),
      authorList: $mainContainer.find('.author-name').text(),
      body: $mainContainer.find('#artText'),
      abstractData: $mainContainer.find('.abstract'),
      abstractInfo: $mainContainer.find('.articleinfo'),

      figureList: $mainContainer.find('.lightbox-figure')
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

        .find('#figures-list').on('mousewheel', function(e) {
          e.stopPropagation();
        });
  };

  FigureLightbox.switchImage = function (imgDoi, cb) {
    this.imgData = {
      doi: imgDoi || '0'
    };
    this.imgData.strippedDoi = this.imgData.doi.replace(/^info:doi\//, '');
    var articleData = this.fetchArticleData();
    this.renderImg(this.imgData.doi);
    $(this.lbSelector)
        .find('#figure-title').text(articleData.title).end()
        .find('#figure-description').text(articleData.description);
  };

  FigureLightbox.loadImage = function (lbContainer, imgDoi, cb) {
    this.lbContainerSelector = lbContainer || this.lbContainerSelector;

    this.imgData = {
      doi: imgDoi || '0'
    };
    this.imgData.strippedDoi = this.imgData.doi.replace(/^info:doi\//, '');

    this.insertLightboxTemplate();
    this.bindBehavior();
    $(this.lbSelector)
        .foundation('reveal', 'open');
    this.renderImg(this.imgData.doi);

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
        increment: 0.1,
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
    $(this.lbContainerSelector)
      // Unbind close button
        .find(this.lbCloseButtonSelector).off('click').end()
      // Unbind buttons to change images
        .find('.change-img').off('click').end()
      // Unbind button to show all images
        .find('.all-fig-btn').off('click');
  };

  })(jQuery);