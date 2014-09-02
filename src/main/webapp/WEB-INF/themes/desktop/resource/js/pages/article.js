/**
 * Created by ddowell on 8/14/14.
 */

(function ($) {
  var rawDate, articlePubDate;


  rawDate = document.getElementById("rawPubDate").value;
  articlePubDate = dateParse(rawDate, true);

  $( document ).ready(function() {
    var floater = $(".titleFloater").clone().appendTo( document.body).css("top","-100px");

    //bindEvents();
    //function bindEvents() {
    $(window).scroll( onscroll );
   // }


  $("#artPubDate").append(articlePubDate);

//show title & authors on floating header if window scrolls to tabs area
    onscroll = function() {
      var top = $(window).scrollTop(),
          topLimit = 420;
        //console.log(top);
      if (top < topLimit && !floater.hasClass("topVisible") || top > topLimit && floater.hasClass("topVisible")) return;

        if ( top > topLimit ) {
            $(floater).addClass('topVisible').css('top',0);

        $(floater).find('.logo-close').on('click', function () {

            floater.css('top','-100px');
            $(window).unbind('scroll');
        });

        } else {
            $(floater).removeClass('topVisible').css('top','-100px');
        }
    };

      function onetime(node, type, callback) {

          // create event
          node.addEventListener(type, function(e) {
              // remove event
              e.target.removeEventListener(e.type, arguments.callee);
              // call handler
              return callback(e);
          });

      }
  });
}(jQuery));

