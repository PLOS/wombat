(function ($) {
  $.fn.buildFigureCarousel = function () {
    $('.carousel-item').each(function (index, carouselItem) {
      var $carouselItem = $(carouselItem);
      var figureDoi = $carouselItem.attr('data-doi');
      figureDoi = figureDoi.replace(/^info:doi\//, ''); // TODO: Fix in service layer
      var $inlineFigure = $(document).find('.figure[data-doi="' + figureDoi + '"]');
      $carouselItem.attr('href', '#' + $inlineFigure.attr('id'));
    });
  };
})(jQuery);
