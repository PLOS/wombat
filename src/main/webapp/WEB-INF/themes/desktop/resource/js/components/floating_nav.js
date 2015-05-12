(function ($) {
  var $win = $(window);
  $.fn.floatingNav = function (options) {
    var defaults = {
      margin:               90,
      margin_bottom:               35,
      page_width:           960,
      scroll_speed: 500,
      content:              '',
      ul_class:             'nav-page',
      section_anchor:       'a[data-toc]',
      section_anchor_attr:  'data-toc',
      class_active:         'active',
      footer:               '#pageftr',
      alternate_bottom_div: '#banner-ftr',
      link_selector:        'a.scroll'
    };

    var $floating_nav_menu = $(this);
    var opts = $.extend(defaults, options);
    var isAutoScrolling = false;

    $('body').on('click', 'ul.' + opts.ul_class + ' a', AutoScroll);

    function AutoScroll(event) {
      //window.history.pushState is not on all browsers
      if (window.history.pushState) {
        window.history.pushState({}, document.title, event.target.href);
      } else { }

      event.preventDefault();
      isAutoScrolling = true;
      // suppress  expansion/collapse of any nested list items during animation-triggered
      // traversal of the nav
      $('ul.' + opts.ul_class + ' li:not(.' + opts.class_active + ') ul').hide();

      $('html,body').animate(
        {scrollTop: $('#' + this.hash.substring(1)).offset().top}, opts.scroll_speed,
        function () {
          isAutoScrolling = false;
          showActiveSublist();
        }
      );
    }

    function showActiveSublist() {
      $('ul.' + opts.ul_class + ' li.' + opts.class_active + ' ul').show();
    }

    return this.each(
        function () {

          var $this = $(this);
          var $ftr_top = $(opts.footer).offset().top;
          var $el_h = $this.innerHeight();
          var $links = $this.find(opts.link_selector);
          var bnr_h = 0;
          var win_top = 0;

          if ($(opts.alternativeBottomDiv).length) {
            bnr_h = $(opts.alternativeBottomDiv).innerHeight();
          }

          var hilite = function () {
            win_top = $win.scrollTop();
            opts.content.find(opts.section_anchor).each(
                function () {
                  var $this_a = $(this);
                  var parentTag = $this_a.parent().prop('tagName');
                  if (win_top > ($this_a.offset().top - opts.margin) && parentTag != 'H4') {

                    var $this_a_ref = $this_a.attr(opts.section_anchor_attr);
                    var $closest_li = $this.find('a[href="#' + $this_a_ref + '"]').closest('li');

                    $links.closest('li').removeClass(opts.class_active);
                    $closest_li.addClass(opts.class_active).parents('li').addClass(opts.class_active);
                  }
                }
            );
          };

          var positionUpdated = function () {

            if (!isAutoScrolling) {
              showActiveSublist();
            }

            win_top = $win.scrollTop();
            $ftr_top = $(opts.footer).offset().top;
            $el_h = $floating_nav_menu.innerHeight();
            var article_top = opts.content.offset().top;
            //the top of the element is out of the viewport
            var el_view_out = (win_top > (article_top - opts.margin));
            //the viewport is tall enough-
            var view_height = (($el_h + opts.margin + bnr_h) < $win.height());
            //the element is not overlapping the footer
            var el_overlap = (win_top < ($ftr_top - ($el_h + opts.margin)));
            //the viewport is wide enough
            var view_width = ($win.width() >= opts.page_width);

            if (view_height && view_width) {

              if (el_view_out && el_overlap) {
                $this.css({'position': 'fixed', 'top': opts.margin + 'px'});
                hilite();
              } else if (win_top > ($ftr_top - ($el_h + opts.margin))) {

                //Adjust the position here a bit to stop the footer from being overlapped
                var tt = $ftr_top - win_top - $el_h;
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
            positionUpdated();
            $win.scroll(positionUpdated);
            $win.resize(positionUpdated);
          } else {
            // it is a touch screen only use hilite
            hilite();
            $win.scroll(hilite);
          }
        }
    );
  };
})(jQuery);