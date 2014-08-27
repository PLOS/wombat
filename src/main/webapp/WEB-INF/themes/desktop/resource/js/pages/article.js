/**
 * Created by ddowell on 8/14/14.
 */

(function ($) {

  var rawDate = document.getElementById("rawPubDate").value;
  var articlePubDate = dateParse(rawDate, true);

  $( document ).ready(function() {
    $("#artPubDate").append(articlePubDate);
/*
    $(".type-desc").hover(function(){
        $(".type-tooltip").addClass("type-tooltip-hover");  },
      function () {
        $(".type-tooltip").removeClass("type-tooltip-hover");
      }
    });*/

  });

  toggle.init();
}(jQuery));

