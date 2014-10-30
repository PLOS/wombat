/**
 *
 * DEPENDENCY:  resource/js/components/truncate_elem
 *              resource/js/components/show_onscroll
 */

(function ($) {

  var s, parse_xml_date, float_header, is_author_list, check_authors_truncation;

   parse_xml_date = {
    settings : {
      raw_date : document.getElementById("rawPubDate").value
    },

    init : function () {
      s = this.settings;
      this.article_pub_date();
    },

    article_pub_date : function () {
      var article_pub_date = dateParse(s.raw_date, true, false, "en-US");
      $("#artPubDate").append(article_pub_date);
    }
  };

  float_header = {

    settings : {
      floater : $("#floatTitleTop"),
      hidden_div : "topVisible",
      scroll_trigger : 420,
      div_exists : 1
    },

    init : function () {
      s = this.settings;
      this.scroll_it();
      this.close_floater();
      this.get_width();
    },

    check_div : function () {
      s.div_exists = s.floater.length;
      return s.div_exists;
    },

    scroll_it :  function () {
      if (this.check_div() > 0) {
        if ($(window).width() > 960) {
          return $(window).on('scroll', function () {
            //show_onscroll is in resource/js/components/
            show_onscroll(s.floater, s.hidden_div, s.scroll_trigger);
          });
        }
      }
    },
    get_width : function(){
      if (this.check_div() > 0) {
        $(window).resize(function () {
          var new_width = $(window).width();
          if (new_width < 960) {
            $(s.floater).removeClass(s.hidden_div);
          } else if (new_width > 960) {
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

  $( document ).ready(function() {

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


    float_header.init();

    if ($.fn.twitter ) {
      var doi = $('meta[name=citation_doi]').attr("content");
      var twitter = new $.fn.twitter();
      twitter.getSidebarTweets(doi);

    }

    // initialize toggle for author list view more
    plos_toggle.init();
    // initialize tooltip_hover for everything
    tooltip_hover.init();
   /* function fixAvatar(img) {
      // During the onload event, IE correctly identifies any images that
      // weren’t downloaded as not complete. Others should too. Gecko-based
      // browsers act like NS4 in that they report this incorrectly.
      if (!img.complete) {
        console.log(img);
        return false;
      }

      // However, they do have two very useful properties: naturalWidth and
      // naturalHeight. These give the true size of the image. If it failed
      // to load, either of these should be zero.

      if (typeof img.naturalWidth !== "undefined" && img.naturalWidth === 0) {
        console.log('natwidth no ');
        return false;
      }

      // No other way of checking: assume it’s ok.
      return true;
    }
    var checkImg = $(listappend).find('.imgLoad');
    fixAvatar(checkImg);*/
  });

}(jQuery));



