
/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

/**
 *
 * DEPENDENCY: resource/js/components/show_onscroll
 *
 */
(function ($) {
  var $win = $(window);
  var originalTarget = window.location.hash;


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

    function AutoScroll(sectionsMap, section, event) {
      if(event) {
        event.preventDefault();
        event.stopPropagation();
      }
      $floating_nav_menu.addClass('scrolling');

      $('html,body').stop().animate(
        {
          scrollTop: section.topPosition
        },
        500,
        function () {
          $floating_nav_menu.removeClass('scrolling');
          window.location.hash='#'+section.id;
          hiliteLink(sectionsMap);
        }
      );
    };

    function hiliteLink(sectionsMap) {
      if(!$floating_nav_menu.hasClass('scrolling')) {
        var currentSection = _.filter(sectionsMap, function (section) {
          return section.topPosition <= win_top && section.bottomPosition >= win_top;
        })[0];

        if (currentSection) {
          var $closest_li = currentSection.$link.closest('li');
          if ($closest_li.parents('li').hasClass('active')) {
            $floating_nav_menu.addClass('no-hidding');
          }
          else {
            $floating_nav_menu.removeClass('no-hidding');
          }

          var $linkActive = $floating_nav_menu.find('li.' + opts.class_active);
          $linkActive.removeClass(opts.class_active);

          $closest_li.addClass(opts.class_active).parents('li').addClass(opts.class_active);
        }
        else {
          var $linkActive = $floating_nav_menu.find('li.' + opts.class_active);
          $linkActive.removeClass(opts.class_active);
        }
      };

    }

    return this.each(
      function () {
        var sectionsMap = [];
        var createSectionsMap = function () {
          sectionsMap = [];
          var links =  $floating_nav_menu.find(opts.link_selector);
          links.each(function (index) {
            var sectionObj = {};
            var section = opts.content.find($(this).attr('href'));

            var topPosition = section.offset().top;
            var nextEl = opts.content.find(links.eq(index+1).attr('href'));
            var bottomPosition = 0;

            if(nextEl.offset()) {
              bottomPosition = nextEl.offset().top-1;
            }
            else {
              bottomPosition = opts.content.height() + opts.content.offset().top;
              var sectionHeight = bottomPosition-topPosition;
              var windowHeight = $(window).height();
              var spacingHeight = opts.content.find('.final-section-spacing').height();
              if(sectionHeight + spacingHeight < windowHeight) {
                if(!opts.content.find('.final-section-spacing').length) {
                  opts.content.append('<div class="final-section-spacing"></div>')
                }

                var diff = windowHeight - sectionHeight - $('body > footer').innerHeight();
                opts.content.find('.final-section-spacing').css('height', diff+'px');
              }
            }

            var $floatTitleTop = $('#floatTitleTop');

            if($floatTitleTop.length) {
              var floatTitleHeight = $floatTitleTop.height() + 20;
              if(originalTarget != '#'+section.attr(opts.section_anchor_attr)) {
                topPosition = topPosition - floatTitleHeight;
              }

              var nextEl = links.eq(index+1);
              if(originalTarget != nextEl.attr('href')) {
                bottomPosition = bottomPosition - floatTitleHeight;
              }
            }

            sectionObj.id = section.attr(opts.section_anchor_attr);
            sectionObj.$element = section;
            sectionObj.$link = $(this);
            sectionObj.topPosition = topPosition;
            sectionObj.bottomPosition = bottomPosition;

            sectionsMap.push(sectionObj);
          });
        };

        var hilite = function () {
          createSectionsMap();
          hiliteLink(sectionsMap);
        };

        $('body').on('click', 'ul.' + opts.ul_class + ' a', function (e) {
          createSectionsMap();
          var sectionId = $(this).attr('href').substring(1);
          var section = _.findWhere(sectionsMap, { id: sectionId });
          AutoScroll(sectionsMap, section, e);
        });

        var positionUpdated = function () {

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