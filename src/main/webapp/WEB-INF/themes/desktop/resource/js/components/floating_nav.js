(function ($) {
  var $win = $(window);
  $.fn.floatingNav = function (options) {
    var defaults = {
      margin: 90,
      margin_bottom: 35,
      page_width: 960,
      scroll_speed: 500,
      content: '',
      ul_class: 'nav-page',
      section_anchor: 'a[data-toc]',
      section_anchor_attr: 'data-toc',
      class_active: 'active',
      footer: '#pageftr',
      alternate_bottom_div: '#banner-ftr',
      link_selector: 'a.scroll',
      ignored_sections: ''
    };

    var $floating_nav_menu = $(this);
    var win_top = $win.scrollTop();
    var opts = $.extend(defaults, options);
    var isAutoScrolling = false;

    $('body').on('click', 'ul.' + opts.ul_class + ' a', AutoScroll);

    function AutoScroll(event) {
      //window.history.pushState is not on all browsers
      if (window.history.pushState) {
        window.history.pushState({}, document.title, event.target.href);
      }

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

    function hiliteLink(link) {
      var $links = $floating_nav_menu.find(opts.link_selector);
      var parentTag = link.parent().prop('tagName');
      var isIgnoredSection = parentTag !== "" && opts.ignored_sections.indexOf(parentTag) != -1;
      if (win_top > (link.offset().top - opts.margin) && !isIgnoredSection) {

        var $linkRef = link.attr(opts.section_anchor_attr);
        var $closest_li = $floating_nav_menu.find('a[href="#' + $linkRef + '"]').closest('li');

        $links.closest('li').removeClass(opts.class_active);
        $closest_li.addClass(opts.class_active).parents('li').addClass(opts.class_active);
      }
    }

    return this.each(
      function () {

        var hilite = function () {
          opts.content.find(opts.section_anchor).each(function() {
            hiliteLink($(this))
          });
        };

        var positionUpdated = function () {

          if (!isAutoScrolling) {
            showActiveSublist();
          }

          win_top = $win.scrollTop();
          var $ftr_top = $(opts.footer).offset().top;
          var $el_h = $floating_nav_menu.innerHeight();
          var article_top = opts.content.offset().top;

          //the top of the element is out of the viewport
          var el_view_out = (win_top > (article_top - opts.margin));
          //the element is not overlapping the footer
          var el_overlap = (win_top < ($ftr_top - ($el_h + opts.margin)));
          //the viewport is wide enough
          var view_width = ($win.width() >= opts.page_width);

          if (view_width) {
            if (el_view_out && el_overlap) {
              $floating_nav_menu.css({'position': 'fixed', 'top': opts.margin + 'px'});
              hilite();
            } else if (win_top > ($ftr_top - ($el_h + opts.margin))) {

              //Adjust the position here a bit to stop the footer from being overlapped
              var tt = $ftr_top - win_top - $el_h;
              hilite();
              $floating_nav_menu.css({'position': 'fixed', 'top': tt + 'px'});
            } else {
              //We're above the article
              $floating_nav_menu.css({'position': 'static'});
            }
          } else {
            $floating_nav_menu.css({'position': 'static'});
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