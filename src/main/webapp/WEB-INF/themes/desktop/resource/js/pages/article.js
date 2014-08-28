/**
 * Created by ddowell on 8/14/14.
 */

(function ($) {

  function UpdateTableHeaders() {
    $(".persist-area").each(function() {

      var el             = $(this),
        offset         = el.offset(),
        scrollTop      = $(window).scrollTop(),
        floatingHeader = $(".floatingHeader", this)

      if ((scrollTop > offset.top) && (scrollTop < offset.top + el.height())) {
        floatingHeader.css({
          "visibility": "visible"
        });
      } else {
        floatingHeader.css({
          "visibility": "hidden"
        });
      };
    });
  }

  var rawDate = document.getElementById("rawPubDate").value;
  var articlePubDate = dateParse(rawDate, true);

  $( document ).ready(function() {
    $("#artPubDate").append(articlePubDate);



  var clonedHeaderRow;

  $(".persist-area").each(function() {
    clonedHeaderRow = $(".persist-header", this);
    clonedHeaderRow
      .before(clonedHeaderRow.clone())
      .css("width", clonedHeaderRow.width())
      .addClass("floatingHeader");

  });

  $(window)
    .scroll(UpdateTableHeaders)
    .trigger("scroll");
  });

}(jQuery));
