/**
 * Created by pgrinbaum on 8/12/14.
 */

//  sets hover state and visibility
//

(function ($) {

  $.fn.menu_drop = function (action, options) {
    options = $.extend({}, defaults, options);

    var defaults = {
      child: 'ul.nav-elements >li>ul'
    };

    if (action === "show") {
      this.addClass("hover");
      $(options.child, this).show();
      return this;
    }

    if (action === "hide") {
      this.removeClass("hover");
      $(options.child, this).hide();
      return this;
    }

  };

}(jQuery));