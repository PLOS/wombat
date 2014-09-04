tooltip_hover = {

  settings: {
    tooltip_hover_trigger: '[data-js=tooltip_hover_trigger]',
    tooltip_hover_target:  '[data-js=tooltip_hover_target]',
    tooltip_hover_hidden:  '[data-initial=hide]',
    tooltip_container: '[data-js=tooltip_container]',
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

  tooltip: function () {
    var s = this.settings;

    $(s.tooltip_trigger).on('hover', function () {

    });

  },
  remove_all: function(){
    var s = this.settings;
    $(s.tooltip_trigger).removeClass(s.tooltip_class);
  },

  click_elsewhere: function () {
    var s = this.settings;
    $(s.tooltip_trigger).blur(s.remove_all);

  }
};
