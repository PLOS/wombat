
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

  // @TODO: Do not parse article. Fetch data via an ajax call
  FigureLightbox.fetchArticleData = function () {
    var $mainContainer = $(document).find('main');
    return {
      doi: this.imgData.doi,
      title: this.imgData.title,
      description: this.imgData.description,
      articleTitle: $mainContainer.find('#artTitle').text(),
      authorList: $mainContainer.find('.author-name').text(),
      body: $mainContainer.find('#artText'),
      abstractData: $mainContainer.find('.abstract'),
      abstractInfo: $mainContainer.find('.articleinfo')
    };
  };

  FigureLightbox.bindBehavior = function () {
    $(this.lbContainerSelector)
        .find(this.lbCloseButtonSelector).on('click', function () {
          FigureLightbox.close();
        });
  };

  FigureLightbox.loadImage = function (lbContainer, img, cb) {
    this.lbContainerSelector = lbContainer;

    this.imgData = {
      doi: img.doi || '0',
      description: img.description || '',
      title: img.title || ''
    };
    this.insertLightboxTemplate();
    this.bindBehavior();
    $(this.lbSelector)
        .foundation('reveal', 'open');
    var $image = $(this.lbSelector).find('img').attr('src', this.buildImgUrl(this.imgData.doi));
    this.panZoom($image);

    // Reinitialize sliders
    $(document).foundation('slider', 'reflow');

    if (typeof cb === 'function') {
      cb();
    }
  };

  FigureLightbox.close = function () {
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


})(jQuery);