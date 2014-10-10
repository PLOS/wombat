



var handleFlagClick = function(e) {
  console.log(this);
  var categoryTerm = $(this).data("categoryname"); console.log(categoryTerm);
  /*,
   articleID = $(this).data("articleid"),
   categoryName = $(this).data("categoryname");*/

  console.log('hi');

  /*  $.ajax({
   type: 'POST',
   url:'/taxonomy/flag/json',
   data: { 'categoryID': categoryID, 'articleID': articleID },
   dataType:'json',
   error: function (jqXHR, textStatus, errorThrown) {
   // console.log(errorThrown);

   },
   success:function (data) {
   $(this).unbind('click', handleFlagClick).bind('click', handleDeflagClick).addClass("flagged").attr('title', "Remove inappropriate flag from '" + categoryName + "'");
   }
   });*/
};

var handleDeflagClick = function(event) {
  var categoryID = $(event.target).data("categoryid");
  var articleID = $(event.target).data("articleid");
  var categoryName = $(event.target).data("categoryname");

  $.ajax({
    type: 'POST',
    url:'/taxonomy/deflag/json',
    data: { 'categoryID': categoryID, 'articleID': articleID },
    dataType:'json',
    error: function (jqXHR, textStatus, errorThrown) {
      console.log(errorThrown);
    },
    success:function (data) {
      $(event.target).unbind('click', handleDeflagClick);
      $(event.target).bind('click', handleFlagClick);
      $(event.target).removeClass("flagged");
      $(event.target).attr('title', "Flag '" + categoryName + "' as inappropriate");
    }
  });
};

$('.taxo-flag').on('click', handleFlagClick);
$('.flagged').on('click', handleDeflagClick);

