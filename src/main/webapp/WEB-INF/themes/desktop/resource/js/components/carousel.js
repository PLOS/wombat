/**
 * Created by pgrinbaum on 5/21/14.
 * using jcarousel
 */
  carousel = {

    settings: {
      container:       '.jcarousel',
      current_locator: '[data-js=carousel-current-item]',
      index_locator:   'span[data-js=carousel-total-index]',
      controls:        '.carousel-control',
      control_next:    '.jcarousel-next',
      control_prev:    '.jcarousel-prev'
    },

    init:    function () {
      // kick things off
      this.bind_actions();

    },
    numbers: function () {
      var s=this.settings;

      $(s.container)
        // get index
        .on('jcarousel:create',function (event, carousel) {
          var total_index = $(this).jcarousel('items').size();
          $(this).next(s.controls).find(s.index_locator).html(total_index);
          $(this).next(s.controls).find(s.current_locator).html('1');
        }).
        // change number
        on('jcarousel:animateend', function (event, carousel) {
          var current_item = $(this).jcarousel('visible').index();
          var current_item_readable = (current_item + 1);
          $(this).next(s.controls).find(s.current_locator).html(current_item_readable);
        })
         /// initialise jcarousel
        .jcarousel({wrap: 'both'});
    },

    next_prev: function () {
      var s=this.settings;

      $(s.control_prev).jcarouselControl({
        target: '-=1'
      });

      $(s.control_next).jcarouselControl({
        target: '+=1'
      });
    },

    bind_actions: function () {
      this.numbers();
      this.next_prev();
    }

  };


