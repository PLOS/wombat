


;(function ($) {
  var s;
plos_tooltip = {

  settings: {
    tooltip_trigger: '[data-js-tooltip=tooltip_trigger]',
    tooltip_target:  '[data-js-tooltip=tooltip_target]',
    tooltip_hidden:  '[data-initial=hide]',
    tooltip_close:   '[data-js-tooltip=tooltip_close]',
    tooltip_container: '[data-js-tooltip=tooltip_container]',  // the container needs an id TODO add to documentation.
    tooltip_class: 'active',
    tooltip_container_class: 'tooltip-container-active'
  },

  init: function () {
   // this.settings = $.extend(this.settings, options);
    this.tooltip();
  },
  tooltip: function (e) {

    s = this.settings;
    $(s.tooltip_trigger).on('click', function () {
      var parent_width = $(this).parents(s.tooltip_container).innerWidth(),
          parent_width_offset = parent_width * .75,
          this_position = $(this).position();
      // add event listener for clicking outside of body
      document.body.addEventListener('click', boxCloser, false);
    // closes all other tooltips
      $(s.tooltip_trigger).not(this).removeClass(s.tooltip_class);
      // toggles the active class to show and hide
      $(this).toggleClass(s.tooltip_class);
    /// ads the position class on the tooltip
      if (this_position.left >= parent_width_offset) {
        $(this).find(s.tooltip_target).addClass('pos-right');
      }

    });
   // close on click
    $(this).on('click', s.tooltip_close, function () {
      $(this).parent(s.tooltip_trigger).removeClass(s.tooltip_class);
    });
   /// closes the whole box
   // TODO: put outside this function
    function boxCloser(e) {
      var subject = $(s.tooltip_container);
      if(e.target.id != subject.attr('id') && !subject.has(e.target).length)
      {
        document.body.removeEventListener('click', boxCloser, false);
        $(s.tooltip_trigger).removeClass(s.tooltip_class);
      }
    }

  }
};
})(jQuery);