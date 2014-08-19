/**
 * Created by ddowell on 8/14/14.
 */

(function ($) {

  var rawDate = document.getElementById("rawPubDate").value;
  var articlePubDate = dateParse(rawDate, true);

  $( document ).ready(function() {
    $("#artPubDate").append(articlePubDate);


/*$(document).foundation({
    tab: {
      callback : function (tab) {
        console.log(tab);
      }
    }
  });*/
  /*   $('#myTabs').on('toggled', function (event, tab) {
    console.log(tab);
  });*/  });
}(jQuery));