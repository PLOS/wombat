/**
 * Created by pgrinbaum on 8/22/14.
 */
toggle = {

  settings: {
    toggle_trigger: '[data-js=toggle_trigger]',
    toggle_target: '[data-js=toggle_target] .close',
    toggle_hidden: '[data-initial=hide]',

    speed: 0
  },

  init:    function () {
    // kick things off
    this.toggle();
  },

  toggle: function () {
    var s=this.settings;
    $(s.toggle_hidden).hide();
    $(s.toggle_trigger).on('click', function(){
      $(this).siblings(s.toggle_trigger).andSelf().toggle(s.speed);
      $(this).siblings(s.toggle_target).toggle(s.speed);
    })
  }

};

tooltip = {

  settings: {
    tooltip_trigger: '[data-js=tooltip_trigger]',
    tooltip_target: '[data-js=tooltip_target]',
    tooltip_hidden: '[data-initial=hide]',
    tooltip_event: 'click',
    tooltip_adjust: true,
    speed: 0
  },

  init:  function (options) {
    // kick things off
    this.settings = $.extend(this.settings, options);
    var s=this.settings;
    this.tooltip();
    this.checkwidth();
  },

  tooltip: function () {
    var s=this.settings;
    $(s.tooltip_hidden).hide();

  //  $(s.tooltip_hidden).hide();
    $(s.tooltip_trigger).on('click', function(){
      $(this).toggleClass('active').nextAll(s.tooltip_target).toggle(s.speed);
    });
  },
  checkwidth: function () {
    var s=this.settings;

    var thing= $(s.toggle_target).parent().innerWidth();
    alert(thing);

    //  $(s.tooltip_hidden).hide();
    $(s.tooltip_trigger).on(s.tooltip_event, function(){
      var pos = $(this).position();
      $(this).parent().toggleClass('active');
    });
  }
};




