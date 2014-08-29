/**
 * Created by pgrinbaum on 8/22/14.
 */
toggle = {

  settings: {
    toggle_trigger: '[data-js=toggle_trigger]',
    toggle_target: '[data-js=toggle_target]',
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
    tooltip_close: '[data-js=tooltip_close]',
    tooltip_event: 'click',
    tooltip_adjust: true,
    speed: 0
  },

  init:  function (options) {
    // kick things off
    this.settings = $.extend(this.settings, options);
    var s = this.settings;
    this.tooltip();
    this.checkwidth();
  },

  tooltip: function () {
    var s = this.settings;
  //  $(s.tooltip_hidden).hide();

  //  $(s.tooltip_hidden).hide();
    $(s.tooltip_trigger).on('click', function(){

      var parent_width = $(this).parents('.author-list').innerWidth(),
          parent_width_offset = parent_width * .75,
          pos = $(this).position();
      console.log(pos.left);
      console.log(parent_width_offset);
      $(s.tooltip_trigger).not(this).removeClass('active');

      $(this).toggleClass('active');
      if(pos.left >= parent_width_offset) { $(this).addClass('posRight');}

    });
//    $(s.tooltip_close).on('click', function(){
//      $(s.tooltip_trigger).removeClass('active');
//    });



  },
  checkwidth: function () {
//    var s=this.settings;
//    //  $(s.tooltip_hidden).hide();
//    $(s.tooltip_trigger).on(s.tooltip_event, function(){
//      $(this).parent().toggleClass('active');
//    });
  }
};




