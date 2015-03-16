/**
 * Created by pgrinbaum on 8/12/14.
 */

//  sets hover state and visibility
//

(function ($) {

  $.fn.menu_drop = function (action, options) {
    options = $.extend({}, defaults, options);

    var defaults = {
      child: '.dropdown'
    };

    if (action === "show") {
      this.addClass("hover");
      $(options.child, this).css('visibility', 'visible');
      return this;
    }

    if (action === "hide") {
      this.removeClass("hover");
      $(options.child, this).css('visibility', 'hidden');
      return this;
    }

  };

}(jQuery));