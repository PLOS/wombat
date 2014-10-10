/**
 *
 * DEPENDENCY:  resource/js/components/truncate_elem
 *              resource/js/components/show_onscroll
 */

(function ($) {

 var s, parse_xml_date, float_header, is_author_list, check_authors_truncation, subject_areas,
twitter_module, t;

   parse_xml_date = {
    settings: {
      raw_date : document.getElementById("rawPubDate").value
    },

    init : function () {
      s = this.settings;
      this.article_pub_date();
    },

    article_pub_date : function () {
      var article_pub_date = dateParse(s.raw_date, true);
      $("#artPubDate").append(article_pub_date);
    }
  };

   float_header = {

    settings: {
      floater : $("#floatTitleTop"),
      hidden_div : "topVisible",
      scroll_trigger : 420,
      div_exists : 1
    },

    init: function () {
      s = this.settings;
      this.scroll_it();
      this.close_floater();
    },

    check_div : function () {
      s.div_exists = s.floater.length;
      return s.div_exists;
    },

    scroll_it :  function () {
      if (this.check_div() > 0) {
        return $(window).on('scroll', function () {
          //show_onscroll is in resource/js/components/
          show_onscroll(s.floater, s.hidden_div, s.scroll_trigger);
        });
      }
    },

    close_floater : function () {
      s.floater.find('.logo-close').on('click', function () {
        s.floater.remove();
      });
    }
  };
  /*twitter_module = {
    settings: {
      plos_twitter: null,
      doi: $("meta[name='dc.identifier']").attr("content")
    },

    init: function() {
     t = this.settings;

      plos_twitter = twitter();
      plos_twitter.displayTwitterCites(doi);
    }
  };*/
  check_authors_truncation = {

    settings : {
      toTruncate : $("#floatAuthorList")
    },

    init : function () {
      s = this.settings;
      this.run_it();
    },

    run_it : function () {

      if (this.overflown() === true) {
        // truncate_elem is in resource/js/components
        return truncate_elem.init();
      }
    },

    overflown : function(){
      var e = s.toTruncate[0];
      return e.scrollHeight > e.clientHeight;
    }
  };

subject_areas = function() {
  //apply truncation via js because the css width needs to be auto
  $("#subjectList li").each(function () {
    var truncTerm = $(this).find('.taxo-term');
    var getWidth = $(truncTerm).width();
    if (getWidth > 135) {
      return $(truncTerm).css('width', '140px');
    }
  });
}

  $( document ).ready(function() {
    if ($.fn.twitter && !$("#twitter-alm-timeline div.tweet-header").is(":visible")) {
      var doi = $('meta[name=citation_doi]').attr("content");
      var twitter = new $.fn.twitter();
      twitter.displayTweetsArticleSidebar(doi);
    }
    $(".preventDefault").on('click', function (e) {
      e.preventDefault();

    });

    parse_xml_date.init();

    is_author_list = document.getElementById("floatAuthorList");
    if ( is_author_list != null) {
      check_authors_truncation.init();
      // initialize tooltip for author info
      plos_tooltip.init();
    }

    var init_subject_truncation = subject_areas();

    float_header.init();
    twitter_module.init();

    // initialize toggle for author list view more
    plos_toggle.init();
    // initialize tooltip for author info
    plos_tooltip.init();
    // initialize tooltip_hover for everything
    tooltip_hover.init();
  });

}(jQuery));

