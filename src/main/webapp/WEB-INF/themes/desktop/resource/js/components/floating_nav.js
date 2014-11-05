;
(function ($) {
  var $win = $(window);
  $.fn.floatingNav = function (options) {
    defaults = {
      margin:             90,
      sections:           '',
      parentContainer:    '.article-content',
      sectionAnchor:      'a[data-toc]',
      sectionAnchorAttr:  'data-toc',
      classActive:        'active',
      footer:             '#pageftr',
      alternateBottomDiv: '#banner-ftr',
      linkSelector:       'a.scroll',
      alternativeBottomDiv: '#banner-ftr'

    };
    var options = $.extend(defaults, options);
    return this.each(function () {

      var $this = $(this),
          ftr_top = $(options.footer).offset().top,
          el_h = $this.innerHeight(),
          bnr_h = 0,
          win_top = 0,
          links = $this.find(options.linkSelector);

      if ($(options.alternativeBottomDiv).length) {
        bnr_h = $(options.alternativeBottomDiv).innerHeight();
      }

      var hilite = function () {
        win_top = $win.scrollTop();
        (options.sections).each(function () {
          this_sec = $(this);
          if (win_top > (this_sec.offset().top - options.margin)) {
            var this_sec_ref = this_sec.find(options.sectionAnchor).attr(options.sectionAnchorAttr);
            links.closest('li').removeClass(options.classActive);
            $this.find('a[href="#' + this_sec_ref + '"]').closest('li').addClass(options.classActive);
          } else { }
        });
      }

      var positionEl = function () {

        win_top = $win.scrollTop();
        ftr_top = $(options.footer).offset().top;

        var article_top = $(options.parentContainer).offset().top,
            el_view_out = (win_top > (article_top - options.margin)),  //the top of the element is out of the viewport
            view_height = ((el_h + options.margin + bnr_h) < $win.height()), //the viewport is tall enough-
            el_overlap = (win_top < (ftr_top - (el_h + options.margin))), //the element is not overlapping the footer
            view_width = ($win.width() >= 960); //the viewport is wide enough
        if (view_height && view_width ) {

        if (el_view_out && el_overlap) {

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
        } else {
          $this.css({ 'position': 'static'});
          hilite();
        }
      }

      if ($('html.no-touch').length === 1){ // it is not a touch screen
        positionEl();
        $win.scroll(positionEl);
        $win.resize(positionEl);
      } else {
      // it is a touch screen only use hilite
        hilite();
        $win.scroll(hilite);
      }


    });
  };
})(jQuery);