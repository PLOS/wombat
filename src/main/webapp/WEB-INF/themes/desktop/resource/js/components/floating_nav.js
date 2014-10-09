;
(function ($) {
  var $win = $(window);
  $.fn.floatingNav = function (options) {
    defaults = {
      margin:   90,
      sections: '',
      parentContainer: '.article-container',
      sectionAnchor: 'a[data-toc]',
      sectionAnchorAttr : 'data-toc',
      classActive: 'active',
      footer: '#pageftr',
      alternateBottomDiv: '#banner-ftr',
      linkSelector: 'a.scroll'

    };
    var options = $.extend(defaults, options);
    return this.each(function () {

      var $this = $(this),
          ftr_top = $(options.footer).offset().top,
          el_top = $this.offset().top,
          el_h = $this.innerHeight(),
          bnr_h = 0,
       win_top = 0,
      links = $this.find(options.linkSelector);

      if ($(options.alternativeBottomDiv).length) {
        bnr_h = $(options.alternativeBottomDiv).innerHeight();
      }



      var hilite = function () {
        (options.sections).each(function () {
          this_sec = $(this);
          if (win_top > (this_sec.offset().top - options.margin)) {
            console.log('hilite');
            var this_sec_ref = this_sec.find(options.sectionAnchor).attr(options.sectionAnchorAttr);
            links.closest('li').removeClass(options.classActive);
            $this.find('a[href="#' + this_sec_ref + '"]').closest('li').addClass(options.classActive);
          } else { }
        });
      }

      var positionEl = function () {

        win_top = $win.scrollTop();
        article_top = $(options.parentContainer).offset().top;
        ftr_top = $(options.footer).offset().top;

        var el_view_out = (win_top > (article_top - options.margin)),  //the top of the element is out of the viewport
            view_height = ((el_h + options.margin + bnr_h) < $win.height()), //the viewport is tall enough-
            el_overlap = (win_top < (ftr_top - (el_h + options.margin))), //the element is not overlapping the footer
            view_width = ($win.width() >= 960); //the viewport is wide enough

        if (el_view_out && view_height && el_overlap && view_width) {

          $this.css({ 'position': 'fixed', 'top': options.margin + 'px' });
          hilite();
          console.log('test1')

        } else if (win_top > (ftr_top - (el_h + options.margin))) {
          //Adjust the position here a bit to stop the footer from being overlapped
          console.log('test2')

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