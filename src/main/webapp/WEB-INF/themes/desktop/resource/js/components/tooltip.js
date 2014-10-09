


;(function ($) {
  var s;
plos_tooltip = {

  settings: {
    tooltip_trigger: '[data-js-tooltip=tooltip_trigger]',
    tooltip_target:  '[data-js-tooltip=tooltip_target]',
    tooltip_close:   '[data-js-tooltip=tooltip_close]',
    tooltip_container: '[data-js-tooltip=tooltip_container]',
    tooltip_class: 'active',
    tooltip_container_class: 'tooltip-container-active'
  },

  init: function () {
   // this.settings = $.extend(this.settings, options);
    this.tooltip();
  },
  tooltip: function () {

    s = this.settings;
    $(s.tooltip_trigger).on('click', function (event) {
      var parent_width = $(this).parents(s.tooltip_container).innerWidth(),
          parent_width_offset = parent_width * .75,
          this_position = $(this).position(),
          $target = $(event.target),
          $self = $(this);

    // remove tooltip class
     $(s.tooltip_trigger).not(this).removeClass(s.tooltip_class);

   //   check if the target is tooltip close OR parents
      if(!$target.parents(s.tooltip_target).length | $target.is(s.tooltip_close)){
        $self.toggleClass(s.tooltip_class);
      }

    /// ads the position class on the tooltip
      if (this_position.left >= parent_width_offset) {
        $(this).find(s.tooltip_target).addClass('pos-right');
      }
    /// closes all i am binding this for the click event if no one clicks on the trigger it will never fire
      $(document).on('click.closeOutside', function(event) {
        if (!$(event.target).closest(s.tooltip_trigger).length) {
          $self.removeClass(s.tooltip_class);
          $(document).off("click.closeOutside");

        }
      });
    });
  }
};
})(jQuery);