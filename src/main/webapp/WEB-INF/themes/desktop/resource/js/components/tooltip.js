


/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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