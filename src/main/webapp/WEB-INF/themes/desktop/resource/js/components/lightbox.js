
var FigureLightbox = {};
(function($) {

  FigureLightbox = {
    lbSelector:     '#article-lightbox',

    /* internal config variables */
    imgPath:        WombatConfig.figurePath || 'IMG_PATH_NOT_LOADED'
  };

  FigureLightbox.init = function (lbContainer, cb) {
    this.lbSelector = lbContainer;

    cb();
  };

  FigureLightbox.loadImage = function (imgDoi) {
    $(this.lbSelector)
        .foundation('reveal', 'open')
        .find('img').attr('src', this.buildImgUrl(imgDoi));
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