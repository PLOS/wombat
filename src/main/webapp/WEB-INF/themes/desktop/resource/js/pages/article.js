/**
 * Created by ddowell on 8/14/14.
 * DEPENDENCY:  resource/js/components/truncate_elem
 *              resource/js/components/show_onscroll
 */

(function ($) {

 var s, parse_xml_date, float_header, check_authors_truncation;
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
          var show_header = show_onscroll(s.floater, s.hidden_div, s.scroll_trigger);
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
   $(".dloads-container").on('click', 'a', function () {
     this.preventDefault();
   });
   parse_xml_date.init();
   check_authors_truncation.init();
   float_header.init();

   // initialize toggle for author list view more
   toggle.init();
   // initialize tooltip for author info
   tooltip.init();
   // initialize tooltip_hover for everything
   tooltip_hover.init();
   // initialize the xml & citations dropdown
   var downloads_menu = menuAimInit();
 });
}(jQuery));

