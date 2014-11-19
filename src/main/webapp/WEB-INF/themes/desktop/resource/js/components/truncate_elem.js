
/*doc
 ---
 title: truncate elements
 name: truncate_elem
 category: js widgets
 ---

 ---

 pass two arguments:
 * container element for what needs truncating,
 * what the truncation mark should be
 * used on article.js for floating header authors and on figviewer.js for header authors
 truncate_elem.remove_overflowed('#floatAuthorList', "<li>&hellip;</li>");

*/
;(function ($) {
  truncate_elem = {


    remove_overflowed : function (to_truncate) {
      var elem_list_height;
      var trunc_ending = "<li>&hellip;</li>";
      var elem_list = $(to_truncate);

      elem_list_height = elem_list[0].offsetTop;
      elem_list.children().each(function () {

        if (this.offsetTop > elem_list_height ) {
          return $(this).addClass("remove");
        }
      });

      $(".remove").remove();

      $(elem_list).append(trunc_ending);

    }/*,
    add_ellips: function () {
      $(elem_list).append(trunc_ending);
    }*/


  }

})(jQuery);

