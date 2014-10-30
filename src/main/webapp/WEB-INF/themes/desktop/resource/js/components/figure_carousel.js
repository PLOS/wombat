(function ($) {

  /**
   * Find the figure carousel (existing markup) and populate its links.
   */
  $.fn.buildFigureCarouselLinks = function () {
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
  $.fn.moveFigureCarouselSection = function () {
    var $carousel = $('#figure-carousel-section');
    var $new_position = $('.articleinfo');
    if ($new_position.length) {
      $carousel.detach();
      $carousel.insertBefore($new_position);
    }
  };

  $.fn.buildFigureCarouselSlider = function () {
    var $wrapper = $('.carousel-wrapper');
    var $slider = $wrapper.find('.slider');

    // Find all items in the slider. (Call this after altering the slider to refresh.)
    function getItems() {
      return $slider.find('.carousel-item');
    }

    var $items = getItems();
    var itemWidth = $items.eq(0).outerWidth();
    var visibleSize = Math.ceil($wrapper.innerWidth() / itemWidth);

    // TODO: Special case where $items.length <= visibleSize ?

    // Pad the slider so that the number of items is divisible by visibleSize
    var padding = (visibleSize - ($items.length % visibleSize)) % visibleSize;
    for (var i = 0; i < padding; i++) {
      $slider.append($('<div class="empty carousel-item"/>'));
    }
    $items = getItems();

    // Clone one visible chunk at the beginning and end, to wrap around
    $items.filter(':first').before($items.slice(-visibleSize).clone().addClass('clone'));
    $items.filter(':last').after($items.slice(0, visibleSize).clone().addClass('clone'));
    $items = getItems();
    $wrapper.scrollLeft(itemWidth * visibleSize); // Scroll past the cloned chunk at the beginning

    // TODO: Embed videos?
    // (Legacy impl hard-codes YouTube links here. Might want to extract into child themes.)

    // TODO: Finish implementing
  };

})(jQuery);
