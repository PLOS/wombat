/**
 *
 * DEPENDENCY:  resource/js/components/truncate_elem
 *              resource/js/components/show_onscroll
 */

(function ($) {

  var s, parse_xml_date, float_header, is_author_list, check_authors_truncation, subject_flags, subject_areas;

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


  subject_flags = function(){
    $(".taxo-flag").on("click", function(){
      $(this).next().css('visibility','visible');
     // if ( $(".taxo-tooltip:visible") ){console.log('yes')}
    });

    $(".taxo-tooltip button").on("click", function(e){
        var flagValue = $(this).val();
        var articleDoi = $("meta[name='dc.identifier']").attr('content');
        articleJournal = articleDoi.replace(/[/.]/g,"");
   /*   var showing = $('.taxo-confirm').attr("display"); //console.log(showing);
      if ( showing === "block" ) {console.log('yes thanks')}*/
      // if localStorage is present, use that
      if (('localStorage' in window) && window.localStorage !== null) {

        // easy object property API
        localStorage[articleJournal] = flagValue;

      } else {

        // without sessionStorage we'll have to use a far-future cookie
        //   with document.cookie's awkward API :(
        var date = new Date();
        date.setTime(date.getTime()+(365*24*60*60*1000));
        var expires = date.toGMTString();
        var cookiestr = "'"+articleJournal +"="+ flagValue+
          ' expires='+expires+'; path=/';
        document.cookie = cookiestr;
      }
       $('.taxo-confirm').css('display','block');
       $('.taxo-explain').css('display','none');
    });

   /* $('html').on('click', function(){
       $('.taxo-tooltip').css('visibility','hidden');
    });*/
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

    // initialize toggle for author list view more
    plos_toggle.init();

    // initialize tooltip_hover for everything
    tooltip_hover.init();

    var init_subject_truncation = subject_areas();
    var init_subject_flags = subject_flags();
  });

}(jQuery));

