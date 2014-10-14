
(function ($) {
  var subject_areas, handleFlagClick;
  article_doi = $("meta[name='dc.identifier']").attr('content');

  subject_areas = function() {
    var truncTerm, targetSpan, getWidth, categoryTerm, checkStorage;
    //apply truncation via js because the css width
    // needs to be auto when truncation isn't necessary
    $("#subjectList li").each(function () {
      truncTerm = $(this).find('.taxo-term');
      targetSpan = $(truncTerm).next();
      getWidth = $(truncTerm).width();
      if (getWidth > 135) {
        return $(truncTerm).css('width', '140px');
      }
      //check in localstorage if any thing has already been flagged
      categoryTerm = $(targetSpan).data("categoryname");
      checkStorage = localStorage[categoryTerm];
      if (checkStorage !== undefined) {
        return $(targetSpan).addClass('flagged');
      }
    });
  };

 handleFlagClick = function () {
   var targetSpan, toolContainer, categoryTerm, flagButton, action;
   targetSpan = this;
   categoryTerm = $(targetSpan).data("categoryname");
   action = $(targetSpan).hasClass("flagged") ? "remove" : "add";
   toolContainer = $(targetSpan).next();
   toolContainer.css('visibility', 'visible');

   $(toolContainer).find('button').on('click', function(){
     flagButton = this;
     action = $(flagButton).data("action");

     $.ajax({
       type: 'POST',
       url: siteUrlPrefix + 'taxonomy/flag/' + action,
       data: { 'categoryTerm': categoryTerm, 'articleDoi': article_doi },
       dataType: 'json',
       error: function (jqXHR, textStatus, errorThrown) {
       console.log(errorThrown);
       },
       success: function (data) {
         action == "remove" ? $(targetSpan).removeClass("flagged") : $(targetSpan).addClass("flagged");
       }
     });
     action == "add" ? localStorage.setItem(categoryTerm, article_doi) : localStorage.removeItem(categoryTerm, article_doi);

     //hide p with buttons, show p with confirmation
     $(flagButton).parent().toggle().next().toggle();

     //hide tooltip, reverse the toggles of above
     setTimeout(function (){
       $(toolContainer).css('visibility','hidden');
       $(flagButton).parent().toggle().next().toggle();
     }, 1000);
   });
 };

 $('.taxo-flag').on('click', handleFlagClick);
  var init_subject_truncation = subject_areas();
})(jQuery);
















