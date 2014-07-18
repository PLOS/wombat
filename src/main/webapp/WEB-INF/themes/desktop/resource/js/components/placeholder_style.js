/**
 * Created by pgrinbaum.
 * Adds and removes classes based on whether an element is focused or not currently used for search
 */

  placeholder_style = {

    settings: {
      input:             '#navsearch input#search',
      placeholder_class: 'placeholder',
      parent:            'form'
    },

    init: function () {
      var s = this.settings;
      $(s.input).focusin(function () {
        $(this).parents(s.parent).addClass(s.placeholder_class);
      });
      $(s.input).focusout(function () {
        $(this).parents(s.parent).removeClass(s.placeholder_class);
      });
    }

  };





