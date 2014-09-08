tooltip = {

  settings: {
    tooltip_trigger: '[data-js=tooltip_trigger]',
    tooltip_target:  '[data-js=tooltip_target]',
    tooltip_hidden:  '[data-initial=hide]',
    tooltip_close:   '[data-js=tooltip_close]',
    tooltip_event:   'click',
    tooltip_container: '[data-js=tooltip_container]',
    tooltip_adjust:  true,
    tooltip_class: 'active',
    speed:           0
  },

  init: function (options) {
    // kick things off
    this.settings = $.extend(this.settings, options);
    var s = this.settings;
    this.tooltip();
    this.click_elsewhere();

  },

  remove_all: function(){
    var s = this.settings;
  },
  tooltip: function () {
    var s = this.settings;

    $(s.tooltip_trigger).blur(function(){
      $(s.tooltip_trigger).removeClass(s.tooltip_class);
    });

    $(s.tooltip_trigger).on('click', function () {
      var parent_width = $(this).parents(s.tooltip_container).innerWidth(),
          parent_width_offset = parent_width * .75,
          this_position = $(this).position();

      $(s.tooltip_trigger).attr('tabindex','0');
      $(s.tooltip_trigger).not(this).removeClass(s.tooltip_class);
      $(this).toggleClass(s.tooltip_class);

      if (this_position.left >= parent_width_offset) {
        $(this).addClass('posRight');
      }

    });

    $(s.tooltip_close).on('click', function () {
      $(this).parent(s.tooltip_trigger).removeClass('active');
    });

  },


  click_elsewhere: function () {
    var s = this.settings;

  }
};
