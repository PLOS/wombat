(function ($) {
  $.fn.buildNav = function (options) {
    var defaults = {
      content: '',
      margin: 70
    };
    var options = $.extend(defaults, options);
    return this.each(function () {
      var $this = $(this),
          $new_ul = $('<ul class="nav-page" />'),
          $anchors = (options.content).find('a[data-toc]');
      if ($anchors.length > 0) {
        $anchors.each(function () {
          var this_a = $(this),
            title = this_a.attr('title'),
            target = this_a.attr('data-toc'),
            itemClass = this_a.attr('id');

          $('<li><a href="#' + target + '" class="scroll">' + title + '</a></li>').addClass(itemClass).appendTo($new_ul);
        });
        $new_ul.find('li').eq(0).addClass('active');

        $new_ul.prependTo($this);
        $this.on("click", "a.scroll", function (event) {
          var link = $(this);

          //window.history.pushState is not on all browsers
          if (window.history.pushState) {
            window.history.pushState({}, document.title, event.target.href);
          } else {  }

          event.preventDefault();
          $('html,body').animate({scrollTop: $('[name="' + this.hash.substring(1) + '"]').offset().top - options.margin}, 500, function () {
            // see spec
            // window.location.hash = link.attr('href');
          });
        });
      };

    });
  };
})(jQuery);
