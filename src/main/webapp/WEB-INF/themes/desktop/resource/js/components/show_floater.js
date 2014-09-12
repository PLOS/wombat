(function ($) {
  /*function to show / hide a top header based on y-axis
  location the page in a browser window. one way of using is
  to use it with the $(window).on('scroll', function () {...}  */

  var show_floater = function(hidden_floater, added_class, show_location){

    var top = $(window).scrollTop();

    if ( top > show_location ) {
      $(hidden_floater).addClass(added_class);
      floatHeader();

    } else if ( top < show_location ) {
      $(hidden_floater).removeClass(added_class);
    }
  };
}(jQuery));/**
 * Created by ddowell on 9/12/14.
 */
