


;(function ($) {
  var s;
  tooltip_component = {

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

      var $self = $(this),
          parent_width = $self.parents(s.tooltip_container).innerWidth(),
          parent_width_offset = parent_width * .75,
          this_position = $self.position(),
          $target = $(event.target),
          $display_tooltip = $self.children('div');

    // remove tooltip class from sibling elements
      $(s.tooltip_trigger).not(this)
        .removeClass(s.tooltip_class)
        .children('div').fadeOut('fast');

    // check if the target is tooltip close button OR parents
      if(!$target.parents(s.tooltip_target).length || $target.is(s.tooltip_close)){
         $self.toggleClass(s.tooltip_class);
         //using fadeToggle breaks in IE8 so detection is needed. if IE8 just rely on the css
         if (Modernizr.borderradius) {
            $display_tooltip.fadeToggle('fast');
          }
       }
    /// adds the position class on the tooltip
      if (this_position.left >= parent_width_offset) {
        $self.find(s.tooltip_target).addClass('pos-right');
      }
    /// closes all; Binding is for the click event: if no one clicks on the trigger it will never fire
      $(document).on('click.closeOutside', function(event) {
        if (!$(event.target).closest(s.tooltip_trigger).length) {
          $self.removeClass(s.tooltip_class);
          $display_tooltip.fadeOut('fast');
          $(document).off("click.closeOutside");
        }
      });
    });
  }
};
})(jQuery);