/**
 *  Scrolls to the jquery selector it is called on.
 * example implementation:
  scrollLocation = '#' + this.hash.substring(1);
 $(scrollLocation).myScrollTo({
        callback: function () {
          showActiveSublist();
        },
      });

 options:
 scrollSpeed: 500 -  the speed at which one scrolls to the object
 changeUrl: false,- if true changes the location hash to the object id
 stopDefault: true - prevents the default behaviour of the bound event.
                    change to false if you the default event behaviour - IF USING you have to pass back the event into the function like so:
 $(scrollLocation).scrollTo(event,{

 *
 */

(function (factory) {
  if (typeof define === 'function' && define.amd) {
    // AMD. Register as an anonymous module.
    define(['jquery'], factory);

  } else if (typeof module === 'object' && module.exports) {
    // Node/CommonJS
    module.exports=factory(require('jquery'));
  } else {
    // Browser globals
    factory(jQuery);
  }
}
(function ($) {
  $.fn.scrollTo=function (event) {

    var scrollLocationId=this.attr('id');

    var options = $.extend({
      callback: function () {
      },
      scrollSpeed: 500,
      changeUrl: false,
      stopDefault: true
    }, arguments[0] || {});

    if (options.stopDefault) {
      event.preventDefault(event);
    }
    $('html,body').stop().animate(
        {
          scrollTop: this.offset().top
        },
        options.scrollSpeed,
        function () {
          if (options.changeUrl) {
            window.location.hash=scrollLocationId;
          }
        }
    );
    options.callback.call(this);
    return this;
  };

}));

