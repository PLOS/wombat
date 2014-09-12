
(function ($) {

  var floatHeader, floater,
  //parse xml date into legible date
    rawDate = document.getElementById("rawPubDate").value,
    articlePubDate = dateParse(rawDate, true);

  $( document ).ready(function() {

    $("#artPubDate").append(articlePubDate);

    floater = $("#floatTitleTop");

    showFloater = function() {
      var top = $(window).scrollTop(),
        topLimit = 420;

      if ( top > topLimit ) {
        $(floater).addClass('topVisible');
        floatHeader();

      } else if ( top < topLimit ) {
        $(floater).removeClass('topVisible');
      }

    };
    floatHeader = function(){
      var floaterList, floaterListEl, floaterListHeight, toTruncate, listEnding;

      floaterList = $("#floatAuthorList");
      //if the floating header has been deleted, don't run overflown()
      if (floaterList.length > 0) {
        toTruncate = $(floaterList).overflown();

        if (toTruncate === true) {
          floaterListEl = document.getElementById("floatAuthorList");
          floaterListHeight = floaterListEl.offsetTop;
          listEnding = "<li>&hellip;</li>";

          $(floaterList).children().each(function () {
            if (this.offsetTop > floaterListHeight) {
              $(this).addClass("remove");
            }
          });
          $(".remove").remove();
          $(floaterList).append(listEnding);
        }
      }
    };
    if ( $(floater).length > 0 ) {
      $(window).on('scroll', function () {
        showFloater();
      });
    }

    $(floater).find('.logo-close').on('click', function () {
      $(floater).remove();
      floater = null;
    });

    // initialize toggle for author list view more
    toggle.init();
    // initialize tooltip for author info
    tooltip.init();
  });

  $.fn.overflown = function(){
    var e = this[0];
    return e.scrollHeight > e.clientHeight;
  }

}(jQuery));

