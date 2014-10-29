(function ($) {

  /**
   * Find the figure carousel (existing markup) and populate its links.
   */
  $.fn.buildFigureCarousel = function () {
    $('.carousel-item').each(function (index, carouselItem) {
      var $carouselItem = $(carouselItem);
      var figureDoi = $carouselItem.attr('data-doi');
      figureDoi = figureDoi.replace(/^info:doi\//, ''); // TODO: Fix in service layer
      var $inlineFigure = $(document).find('.figure[data-doi="' + figureDoi + '"]');
      $carouselItem.find('a').attr('href', '#' + $inlineFigure.attr('id'));
    });
  };

  /**
   * Move the figure carousel from its static position (defined in FreeMarker templates) to its desired position (which
   * is midway through post-transformation HTML).
   */
  $.fn.moveFigureCarousel = function () {
    var $carousel = $('#figure-carousel');
    var $new_position = $('.articleinfo');
    if ($new_position.length) {
      $carousel.detach();
      $carousel.insertBefore($new_position);
    }
  }

})(jQuery);
