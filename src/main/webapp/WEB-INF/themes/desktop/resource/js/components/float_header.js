/**
 * Created by ddowell on 9/16/14.
 * DEPENDENCY: resource/js/components/show_onscroll
 */
var s, float_header = {   // TODO: make extensible so floater, hidden_div, & scroll_trigger can be set as options
  settings: {
    floater : $("#floatTitleTop"), // TODO: use data-js-floater as selector
    hidden_div : "topVisible",
    scroll_trigger : 420,
    div_exists : 1
  },

  init: function () {
    s = this.settings;
    this.scroll_stuff();
    this.close_floater();
  },

  check_div : function () {
    s.div_exists = s.floater.length;
    return s.div_exists;
  },

  scroll_stuff :  function () {
    if (this.check_div() > 0) {

      return $(window).on('scroll', function () {
        //show_onscroll is in resource/js/components/
        var show_header = show_onscroll(s.floater, s.hidden_div, s.scroll_trigger);

      });
    }
  },

  close_floater : function () {
    s.floater.find('.logo-close').on('click', function () {
      s.floater.remove();
    });
  }
};
