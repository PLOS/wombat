
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
      var elem_list_height, trunc_ending, elem_list, remove_list, get_last;
      trunc_ending = "&hellip;";
      elem_list = $(to_truncate);
      remove_list = [];
      elem_list_height = $(elem_list[0]).offset().top;

      elem_list.children().each(function () {
        if ($(this).offset().top > elem_list_height ) {
          remove_list.push(this);
          return  $(this).addClass("remove");
        }
      });

      get_last = remove_list[0];
      $(get_last).prev().append(trunc_ending);
      $(".remove").css('visibility','hidden');

    }

  }

})(jQuery);

