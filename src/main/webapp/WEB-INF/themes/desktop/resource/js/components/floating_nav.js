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

      var $this = $(this);
      var ftr_top = $('#pageftr').offset().top;
      var el_top = $this.offset().top;
      var el_h = $this.innerHeight();
      var bnr_h = 0;
      if ($('#banner-ftr').length) {
        bnr_h = $('#banner-ftr').innerHeight();
      }
      var win_top = 0;
      var links = $this.find('a.scroll');

      var hilite = function () {
        (options.sections).each(function () {
          this_sec = $(this);
          if (win_top > (this_sec.offset().top - options.margin)) {

            var this_sec_ref = this_sec.find('a[data-toc]').attr('data-toc');
            links.closest('li').removeClass('active');
            $this.find('a[href="#' + this_sec_ref + '"]').closest('li').addClass('active');
          }
        });
      }

      var positionEl = function () {
        win_top = $win.scrollTop();
        ftr_top = $('#pageftr').offset().top;

        var el_view_out = (win_top > (el_top - options.margin)),  //the top of the element is out of the viewport
            view_height = ((el_h + options.margin + bnr_h) < $win.height()), //the viewport is tall enough-
            el_overlap = (win_top < (ftr_top - (el_h + options.margin))), //the element is not overlapping the footer
            view_width = ($win.width() >= 960); //the viewport is wide enough

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

      var marginFix = function () {
        var lastSection = $('div.article div').last();
        if (lastSection.length > 0) {
          var offset = lastSection.offset().top;
          var docHeight = $(document).height();
          var z = (docHeight - offset) + options.margin;
          if (z < $win.height()) {
            var margin = Math.ceil(($win.height() - z) + options.margin);
            lastSection.css({ 'margin-bottom': margin + 'px'});
          }
        }
      }

      positionEl();
      marginFix();
      $win.scroll(positionEl);
      $win.resize(positionEl);
    });
  };
})(jQuery);