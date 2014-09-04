/**
 * Created by pgrinbaum on 8/22/14.
 */
toggle = {

  settings: {
    toggle_trigger: '[data-js=toggle_trigger]',
    toggle_target: '[data-js=toggle_target]',
    toggle_hidden: '[data-initial=hide]',
    toggle_add: '[data-js=toggle_add]',
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
      $(this).siblings(s.toggle_trigger).andSelf().toggle(s.speed); //TODO: don't repeat myself so much here.
      $(this).siblings(s.toggle_target).toggle(s.speed);
      $(this).siblings(s.toggle_add).toggle(s.speed);
    })
  }

};




