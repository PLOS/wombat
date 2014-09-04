/**
 * Created by ddowell on 8/14/14.
 */

(function ($) {

  var rawDate = document.getElementById("rawPubDate").value,
  articlePubDate = dateParse(rawDate, true);

  $( document ).ready(function() {
   //create the  floating title div
    var floater = $(".titleFloater").clone().appendTo( document.body).css("top","-100px");
    $(floater).find("h1").removeAttr('id');

    $(window).on('scroll', function (){
      //if floater has been removed, no need to run function
      if (floater) {
          onscroll();
      }
    });

  $("#artPubDate").append(articlePubDate);

//show title & authors on floating header if window scrolls to tabs area
    onscroll = function() {
      var top = $(window).scrollTop(),
          topLimit = 420;

        if ( top > topLimit ) {
          $(floater).addClass('topVisible').css('top',0);

          $(floater).find('.logo-close').on('click', function () {
            $(floater).remove();
          });

        } else if ( top < topLimit ) {
          $(floater).removeClass('topVisible').css('top','-100px');
        }
    };
    // initialize toggle for author list view more
    toggle.init();
  });
}(jQuery));

