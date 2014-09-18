var tooltip_hover;
tooltip_hover = {

  settings: {
  tooltip_hover_trigger: '[data-js-tooltip-hover=trigger]',
  tooltip_hover_target:  '[data-js-tooltip-hover=target]',
  tooltip_hover_hidden: '[data-initial=hide]',
  tooltip_class: 'active',
  speed:           0
},


  init: function(options) {
    // kick things off
    this.settings = $.extend(this.settings, options);
    var s = this.settings;
    this.action();
  },

  action: function() {
    var s = this.settings;
    $(s.tooltip_hover_trigger).hover( function () {
         $(this).toggleClass(s.tooltip_class);
    });
  }
};


