/**
 *
 * DEPENDENCY:  resource/js/components/truncate_elem
 *              resource/js/components/show_onscroll
 */

(function ($) {

  var s, float_header, is_author_list, spin_target, spin_opts, almspinner;

  float_header = {

    settings : {
      floater : $('#floatTitleTop'),
      hidden_div : 'topVisible',
      scroll_trigger : 420,
      width_trigger : 960,
      div_exists : 1
    },

    init : function () {
      s = this.settings;
      this.scroll_it();
      this.close_floater();
      this.get_width();
      this.check_positions();
    },

    check_div : function () {
      s.div_exists = s.floater.length;
      return s.div_exists;
    },

    check_positions : function () {
      var trigger_header = $(window).scrollTop();
      var new_width = $(window).width();
      if (trigger_header > s.scroll_trigger && new_width > s.width_trigger) {
        return s.floater.addClass(s.hidden_div);
      }
    },

    scroll_it :  function () {
      if (this.check_div() > 0) {
          return $(window).on('scroll', function () {
            var new_width = $(window).width();
            if (new_width > s.width_trigger) {
            show_onscroll(s.floater, s.hidden_div, s.scroll_trigger);
            }
          });
      }
    },

    get_width : function(){
      if (this.check_div() > 0) {
        $(window).resize(function () {
          var new_width = $(window).width();
          var trigger_header = $(window).scrollTop();
          if (new_width < 960) {
            $(s.floater).removeClass(s.hidden_div);
          } else if (trigger_header > s.scroll_trigger && new_width > s.width_trigger) {
              $(s.floater).addClass(s.hidden_div);
          }
        });
      }
    },

    close_floater : function () {
      s.floater.find('.logo-close').on('click', function () {
        s.floater.remove();
      });
    }
  };

 $( document ).ready(function() {

    $('.preventDefault').on('click', function (e) {
      e.preventDefault();

    });

    is_author_list = document.getElementById('floatAuthorList');
    if ( is_author_list != null) {
      truncate_elem.remove_overflowed('#floatAuthorList');

      // initialize tooltip for author info
      plos_tooltip.init();
    }

    float_header.init();

    if ($.fn.twitter ) {
      var doi = $('meta[name=citation_doi]').attr('content');
      var twitter = new $.fn.twitter();
      twitter.getSidebarTweets(doi);
    }
    if ($.fn.signposts ) {
      var doi = $('meta[name=citation_doi]').attr('content');
      var signposts = new $.fn.signposts();
      signposts.getSignpostData(doi);
    }
    // initialize toggle for author list view more
    plos_toggle.init();

    // initialize tooltip_hover for everything
    tooltip_hover.init();

    spin_opts = {
      width: 1,
      zIndex: 5000,
      top: '40%', // Top position relative to parent
      left: '50%' // Left position relative to parent
    };
    spin_target = document.getElementById('loadingMetrics');
    almspinner = new Spinner(spin_opts).spin(spin_target);

  });

}(jQuery));
