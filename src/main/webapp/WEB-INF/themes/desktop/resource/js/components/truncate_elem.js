;(function ($) {
var s;
  truncate_elem = { // TODO: make extensible so elem_list & trunc_ending can be set as options
  settings: {
    elem_list : $("#floatAuthorList"), // TODO: use data-js-floater as selector
    //using hard number for now. getting the offsetTop from elem_list isn't working cuz either/both separate file / object literal format
    elem_list_height : null,
    trunc_ending : "<li>&hellip;</li>"
  },

  init: function () {
    s = this.settings;
    this.remove_overflowed();
    this.add_ellips();
  },

  remove_overflowed : function () {
    s.elem_list_height = s.elem_list[0].offsetTop;
    s.elem_list.children().each(function () {
      if (this.offsetTop > s.elem_list_height ) {
        return $(this).addClass("remove");
      }
    });
    $(".remove").remove();
  },

  add_ellips: function () {
    $(s.elem_list).append(s.trunc_ending);
  }
}

})(jQuery);