/**
 * Created by ddowell on 9/15/14.
 */

  /*function to show / hide something based on y-axis
   location the page in a browser window.
   Animation can be achieved with css3 */
;(function ($) {
  show_onscroll = function (hidden_div, added_class, show_location) {

    var tiptop = $(window).scrollTop();

    if (tiptop > show_location) {
      $(hidden_div).addClass(added_class);

    } else
      if (tiptop < show_location) {
        $(hidden_div).removeClass(added_class);
      }
  };
})(jQuery);