;
(function ($) {
  var $win = $(window);
  $.fn.floatingNav = function (options) {
    defaults = {
      margin:   90,
      sections: ''
    };
    var options = $.extend(defaults, options);
    return this.each(function () {

      var $this = $(this),
          ftr_top = $('#pageftr').offset().top,
          el_top = $this.offset().top,
          el_h = $this.innerHeight(),
          bnr_h = 0;
      if ($('#banner-ftr').length) {
        bnr_h = $('#banner-ftr').innerHeight();
      }
      var win_top = 0;
      var links = $this.find('a.scroll');


      var hilite = function () {
        (options.sections).each(function () {
          this_sec = $(this);
          if (win_top > (this_sec.offset().top - options.margin)) {
            console.log('this');
            var this_sec_ref = this_sec.find('a[data-toc]').attr('data-toc');
            links.closest('li').removeClass('active');
            $this.find('a[href="#' + this_sec_ref + '"]').closest('li').addClass('active');
          } else { }
        });
      }

      var positionEl = function () {

        win_top = $win.scrollTop();
        article_top = $('.article-container').offset().top;    /// this is the ticket
        ftr_top = $('#pageftr').offset().top;
        var el_top_now = $this.offset().top;    /// do not use

        console.log("el_top : " + el_top);
        console.log("el_top_now : " + el_top_now);
        console.log("win_top : " + win_top);
        console.log("win_top final : " + (el_top_now - options.margin));
        console.log("$win.height() : " + $win.height());
        console.log("$this.position.top() : " + $this.position().top);
        console.log("$('article-container').position().top: " + $('.article-container').offset().top);
        var el_view_out = (win_top > (article_top - options.margin)),  //the top of the element is out of the viewport
            view_height = ((el_h + options.margin + bnr_h) < $win.height()), //the viewport is tall enough-
            el_overlap = (win_top < (ftr_top - (el_h + options.margin))), //the element is not overlapping the footer
            view_width = ($win.width() >= 960); //the viewport is wide enough

        console.log(el_view_out);

        if (el_view_out && view_height && el_overlap && view_width) {

          $this.css({ 'position': 'fixed', 'top': options.margin + 'px' });
          hilite();
        } else if (win_top > (ftr_top - (el_h + options.margin))) {
          //Adjust the position here a bit to stop the footer from being overlapped
          var tt = ftr_top - win_top - el_h - options.margin + 35;
          hilite();
          $this.css({ 'position': 'fixed', 'top': tt + 'px' });
        } else {
          //We're above the article
          $this.css({ 'position': 'static'});
        }
      }

      positionEl();
      $win.scroll(positionEl);
      $win.resize(positionEl);
    });
  };
})(jQuery);