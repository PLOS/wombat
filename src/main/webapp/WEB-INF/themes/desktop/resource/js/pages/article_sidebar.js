
(function ($) {
  var subject_areas, handleFlagClick;

  subject_areas = (function () {

   var truncTerm, targetSpan, getWidth, categoryTerm, checkStorage;
   $("#subjectList li").each(function () {
     /* //apply width via js if truncation is needed because the css width needs to be auto otherwise*/
     truncTerm = $(this).find('.taxo-term');
     targetSpan = $(truncTerm).next();
     getWidth = $(truncTerm).width();
     if (getWidth > 134) {
       return $(truncTerm).css('width', '140px');
     }

     /*//check in localstorage if any thing has already been flagged*/
     categoryTerm = $(targetSpan).data("categoryname");
     checkStorage = localStorage[categoryTerm];
     if (checkStorage !== undefined) {
       return $(targetSpan).addClass('flagged');
     }
   });
  })();

  handleFlagClick = function () {

   /* // get the data & containers needed*/
    var targetSpan, categoryTerm, action, toolContainer, closeIt, flagButton, article_doi;
    targetSpan = this;
    categoryTerm = $(targetSpan).data("categoryname");
    action = $(targetSpan).hasClass("flagged") ? "remove" : "add";
    toolContainer = $(targetSpan).next();
    article_doi = $("meta[name='dc.identifier']").attr('content');

    /*// show the tooltip*/
    $(toolContainer).addClass('activate');

    /*// close other open tooltips*/
    if ( $(toolContainer).parent().siblings().children('div').hasClass('activate') ){
      $(toolContainer).parent().siblings().children('div').removeClass('activate');
      window.clearTimeout(closeIt);
    }

    /*// close tooltip if click outside of it*/
    $(document).on('click.closeOutside', function (event) {
      if ($(event.target).closest('.taxo-flag').length === 1 || $(event.target).closest('.taxo-tooltip').length === 1) {
        // do nothing
      } else {
        $(toolContainer).removeClass('activate');
        $(document).off("click.closeOutside");
      }
    });

    /*// handle the yes/no clicks, show confirmation, then close tooltip */
    $(toolContainer).find('button').on('click', function () {
      flagButton = this;
      action = $(flagButton).data("action");

      $.ajax({
        type: 'POST',
        url: siteUrlPrefix + 'taxonomy/flag/' + action,
        data: { 'categoryTerm': categoryTerm, 'articleDoi': article_doi },
        dataType: 'json',
        error: function (jqXHR, textStatus, errorThrown) {
          //console.log(errorThrown);
          $('#subjectErrors').append(errorThrown);
        },
        success: function () {
          action === "remove" ? $(targetSpan).removeClass("flagged") : $(targetSpan).addClass("flagged");
        }
      });
      action === "add" ? localStorage.setItem(categoryTerm, article_doi) : localStorage.removeItem(categoryTerm, article_doi);

      /*//hide p with buttons, show p with confirmation*/
      $(flagButton).parent().toggle().next().toggle();

     /* //hide tooltip, set p with buttons back to block*/
      closeIt = window.setTimeout(function () {
          $(toolContainer).removeClass('activate');
          $(toolContainer).children('.taxo-explain').toggle();
          $(toolContainer).children('.taxo-confirm').toggle();
        }, 1000);

      var runTimeout = closeIt;

    });
  };

  $('.taxo-flag').on('click', handleFlagClick);

})(jQuery);
















