
/*doc
 ---
 title: truncate elements
 name: truncate_elem
 category: js widgets
 ---

 ---

 pass the container element for what needs truncating

 used on article.js for floating header authors and on figviewer.js for header authors
 truncate_elem.remove_overflowed('#floatAuthorList');

*/
;(function ($) {
  truncate_elem = {

   //TODO: add options for the trunc ending
    remove_overflowed : function (to_truncate) {
      var elem_list_height;
      var trunc_ending = "<li>&hellip;</li>";
      var elem_list = $(to_truncate);

      elem_list_height = $(elem_list[0]).offset().top;
      elem_list.children().each(function () {

        if ($(this).offset().top > elem_list_height ) {
          return $(this).addClass("remove");
        }
      });

      $(".remove").remove();

      $(elem_list).append(trunc_ending);

    }

  }

})(jQuery);

