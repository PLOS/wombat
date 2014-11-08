(function ($) {

  /**
   * Find the figure carousel (existing markup) and populate its links.
   */
  function buildFigureCarouselLinks($carouselSection) {
    $carouselSection.find('.carousel-item').each(function (index, carouselItem) {
      var $carouselItem = $(carouselItem);
      var figureDoi = $carouselItem.attr('data-doi');
      figureDoi = figureDoi.replace(/^info:doi\//, ''); // TODO: Fix in service layer
      var $inlineFigure = $('.figure[data-doi="' + figureDoi + '"]');
      $carouselItem.find('a').attr('href', '#' + $inlineFigure.attr('id'));
    });
  }

  /**
   * Move the figure carousel from its static position (defined in FreeMarker templates) to its desired position (which
   * is midway through post-transformation HTML).
   */
  function moveFigureCarouselSection($carouselSection) {
    var $new_position = $('.articleinfo');
    if ($new_position.length) {
      $carouselSection.detach();
      $carouselSection.insertBefore($new_position);
    }
  }

  $.fn.buildFigureCarousel = function (options) {
    var defaults = {
      speed: 500,
      access: false,
      autoplay: false,
      delay: 10000,
      defaultpaddingbottom: 10
    };
    options = $.extend(defaults, options);
    var $this = $(this);

    buildFigureCarouselLinks($this);
    moveFigureCarouselSection($this);

    var $wrapper = $this.find('.carousel-wrapper');
    var $slider = $wrapper.find('.slider');

    // Find all items in the slider. (Call this after altering the slider to refresh.)
    function getItems() {
      return $slider.find('.carousel-item');
    }

    var $items = getItems();
    var itemWidth = $items.eq(0).outerWidth();
    var visibleSize = Math.ceil($wrapper.innerWidth() / itemWidth);
    var pageCount = Math.ceil($items.length / visibleSize);

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

    var currentPage = 0;

    function goToPage(page) {
      var pageDifference = page - currentPage;
      if (pageDifference == 0) return;
      var pixelDifference = itemWidth * visibleSize * pageDifference;

      $wrapper.filter(':not(:animated)').animate(
          {scrollLeft: '+=' + pixelDifference},
          options.speed,

          // When animation is complete
          function () {
            // If at the end or beginning (one of the cloned pages), reposition to the original page it was cloned from for infinite effect
            if (page < 0) {
              $wrapper.scrollLeft(itemWidth * visibleSize * pageCount);
              currentPage = pageCount - 1;
            } else if (page >= pageCount) {
              $wrapper.scrollLeft(itemWidth * visibleSize);
              currentPage = 0;
            } else {
              currentPage = page;
            }

            var $pageButtons = $this.find('.carousel-page-buttons .index');
            $pageButtons.removeClass('active');
            $pageButtons.eq(currentPage).addClass('active');
          });
    }

    // Wire controls
    var $control = $this.find('.carousel-control');
    $control.find('.button.previous').click(function () {
      goToPage(currentPage - 1);
    });
    $control.find('.button.next').click(function () {
      goToPage(currentPage + 1);
    });
    if ($items.length > visibleSize) {
      function createPageButton(pageNumber) {
        //for IE8 you have to put the closing span AND the &nbsp
        var $pageButton = $('<span class="index">&nbsp;</span>');

        $pageButton.click(function () {
          goToPage(pageNumber);
        });
        return $pageButton;
      }

      var $pageButtons = $this.find('.carousel-page-buttons');
      for (var i = 0; i < pageCount; i++) {
        var $pageButton = createPageButton(i);
        if (i == 0) {
          $pageButton.addClass('active');
        }
        $pageButtons.append($pageButton);
      }
    }

    // TODO: Implement autoplay, touch events
  };

})(jQuery);
