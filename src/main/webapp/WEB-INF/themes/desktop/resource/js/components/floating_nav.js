
(function ($) {
  var $win = $(window);
  $.fn.floatingNav = function (options) {
    var defaults = {
      margin:               90,
      content:              '',
      ulClass:              'nav-page',
      sectionAnchor:        'a[data-toc]',
      sectionAnchorAttr:    'data-toc',
      classActive:          'active',
      footer:               '#pageftr',
      alternateBottomDiv:   '#banner-ftr',
      linkSelector:         'a.scroll'

    };
    var opts = $.extend(defaults, options);

    $('body').on(
        'click', 'ul.' + opts.ulClass + ' a', function (event) {

          //window.history.pushState is not on all browsers
          if (window.history.pushState) {
            window.history.pushState({}, document.title, event.target.href);
          } else { }

          event.preventDefault();
          // suppress distracting expansion/collapse of any nested list items during animation-triggered
          // traversal of the nav
          $('ul.' + opts.ulClass + ' li:not(.' + opts.classActive + ') ul').addClass('suppress_expansion');

          $('html,body').animate(
              {scrollTop: $('#' + this.hash.substring(1)).offset().top}, 500, 
              function () {
                $('ul.' + opts.ulClass + ' li ul').removeClass('suppress_expansion');
              }
          );
        }
    );
    return this.each(
        function () {

          var $this = $(this);
          var $ftr_top = $(opts.footer).offset().top;
          var $el_h = $this.innerHeight();
          var $links = $this.find(opts.linkSelector);
          var bnr_h = 0;
          var win_top = 0;

          if ($(opts.alternativeBottomDiv).length) {
            bnr_h = $(opts.alternativeBottomDiv).innerHeight();
          }

          var hilite = function () {
            win_top = $win.scrollTop();
            opts.content.find(opts.sectionAnchor).each(
                function () {
                  var $this_a = $(this);
                  if (win_top > ($this_a.offset().top - opts.margin)) {
                    
                    var $this_a_ref = $this_a.attr(opts.sectionAnchorAttr);
                    var $closest_li = $this.find('a[href="#' + $this_a_ref + '"]').closest('li');
                    
                    $links.closest('li').removeClass(opts.classActive);
                    $closest_li.addClass(opts.classActive);
                    $closest_li.parents('li').addClass(opts.classActive);

                  } else { }
                }
            );

          };

          var positionEl = function () {

            win_top = $win.scrollTop();
            $ftr_top = $(opts.footer).offset().top;
            var article_top = opts.content.offset().top;
            //the top of the element is out of the viewport
            var el_view_out = (win_top > (article_top - opts.margin));
            //the viewport is tall enough-
            var view_height = (($el_h + opts.margin + bnr_h) < $win.height());
            //the element is not overlapping the footer
            var el_overlap = (win_top < ($ftr_top - ($el_h + opts.margin)));
            //the viewport is wide enough
            var view_width = ($win.width() >= 960);

            if (view_height && view_width) {

              if (el_view_out && el_overlap) {
                $this.css({'position': 'fixed', 'top': opts.margin + 'px'});
                hilite();
              } else if (win_top > ($ftr_top - ($el_h + opts.margin))) {

                //Adjust the position here a bit to stop the footer from being overlapped
                var tt = $ftr_top - win_top - $el_h - opts.margin + 35;
                hilite();
                $this.css({'position': 'fixed', 'top': tt + 'px'});
              } else {
                //We're above the article
                $this.css({'position': 'static'});
              }
            } else {
              $this.css({'position': 'static'});
              hilite();
            }
          };

          if ($('html.no-touch').length === 1) { // it is not a touch screen
            positionEl();
            $win.scroll(positionEl);
            $win.resize(positionEl);
          } else {
            // it is a touch screen only use hilite
            hilite();
            $win.scroll(hilite);
          }

        }
    );
  };
})(jQuery);