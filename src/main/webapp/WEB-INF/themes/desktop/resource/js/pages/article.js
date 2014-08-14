/**
 * Created by ddowell on 8/14/14.
 */

(function ($) {

  var rawDate = document.getElementById("rawPubDate").value;
  var articlePubDate = dateParse(rawDate, true);

  $( document ).ready(function() {
    $("#artPubDate").append(articlePubDate);
  });

}(jQuery));