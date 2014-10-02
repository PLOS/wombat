/**
 * Created by pgrinbaum on 8/22/14.
 */
;(function ($) {
  var s;
  plos_toggle = {

    settings: {
      toggle_trigger: '[data-js-toggle=toggle_trigger]',
      toggle_target: '[data-js-toggle=toggle_target]',
      toggle_add: '[data-js-toggle=toggle_add]',
      speed: 0
    },

    init: function () {

      this.toggle();

    },

    toggle: function () {
      s = this.settings;
      $(s.toggle_trigger).on('click', function () {

        $(this).siblings(s.toggle_trigger).andSelf().toggle(s.speed); //TODO: don't repeat myself so much here.
        $(this).siblings(s.toggle_target).toggle(s.speed); //TODO rewrite to not use jQuery
        $(this).siblings(s.toggle_add).toggle(s.speed);
      });
    }

  };


})(jQuery);