(function ($) {
  var subject_areas, openTermTooltip, handleFlagClick;

  subject_areas = (function () {
    var categoryTerm, checkStorage;

    $('#subjectList li').each(
        function () {

          var iconWidth = $('.taxo-flag').outerWidth(true);
          var columnWidth = $(this).width() - iconWidth;
          var columnPadding = 5 * 2;
          var truncationWidth = (columnWidth - columnPadding);
          var truncTerm = $(this).find('.taxo-term');
          var targetSpan = $(truncTerm).next();
          var getWidth = $(truncTerm).width();
          /* //apply width via js if truncation is needed because the css width needs to be auto otherwise */

          if (getWidth > truncationWidth) {
            var term_truncate = $(truncTerm).css('width', columnWidth + 'px');
          }

          /*//check in localstorage if any thing has already been flagged*/
          categoryTerm = $(targetSpan).next().data('categoryname');
          checkStorage = localStorage[categoryTerm];
          if (checkStorage !== undefined) {
            var term_flagged = $(targetSpan).addClass('flagged');
          }

          return {term_flagged: term_flagged, term_truncate: term_truncate};
        }
    );
  })();

  openTermTooltip = function () {
    var targetSpan, toolContainer;
    targetSpan = this;
    toolContainer = $(targetSpan).next();

    /*// show the tooltip*/
    $(toolContainer).addClass('activate');

    /*// close other open tooltips*/
    if ($(toolContainer).parent().siblings().children('div').hasClass('activate')) {
      $(toolContainer).parent().siblings().children('div').removeClass('activate');
      window.clearTimeout(closeIt);
    }

    /*// close tooltip if click outside of it*/
    $(document).on('click.closeOutside', function (event) {
      if ($(event.target).closest('.taxo-flag').length !== 1
        && $(event.target).closest('.taxo-tooltip').length !== 1) {
        $(toolContainer).removeClass('activate');
        $(document).off("click.closeOutside");
      }
    });

  };
  $('.taxo-flag').on('click', openTermTooltip);

  handleFlagClick = (function () {
    /*// handle the yes/no clicks, show confirmation, then close tooltip */
    $('.taxo-tooltip button').on(
        'click', function () {
          /* // get the data & containers needed*/
          var targetSpan, categoryTerm, action, toolContainer, closeIt, flagButton, article_doi;
          flagButton = this;
          toolContainer = $(flagButton).parent().parent();
          categoryTerm = $(toolContainer).data("categoryname");
          targetSpan = $(toolContainer).prev();
          article_doi = $("meta[name='dc.identifier']").attr('content');
          action = $(flagButton).data("action");

          $.ajax(
              {
                type:     'POST',
                url:      siteUrlPrefix + 'taxonomy/flag/' + action,
                data:     {'categoryTerm': categoryTerm, 'articleDoi': article_doi},
                error:    function (errorThrown) {
                  $('#subjectErrors').append(errorThrown);
                },
                success:  function () {
                  action === "remove" ? $(targetSpan).removeClass("flagged") : $(targetSpan).addClass("flagged");
                }
              }
          );

          /* // add/remove from localStorage */
          action === "add" ? localStorage.setItem(categoryTerm, article_doi) : localStorage.removeItem(categoryTerm, article_doi);

          /*//hide p with buttons, show p with confirmation*/
          $(flagButton).parent().toggle().next().toggle();

          /* //hide tooltip, set p with buttons back to block*/
          closeIt = window.setTimeout(
              function () {
                $(toolContainer).removeClass('activate');
                $(toolContainer).children('.taxo-explain').toggle();
                $(toolContainer).children('.taxo-confirm').toggle();
                $('.taxo-flag').on('click', handleFlagClick);
              }, 1000
          );

          var runTimeout = closeIt;
        }
    );
  })();

  /* Load twitter */
  if ($.fn.twitter ) {
    var doi = $('meta[name=citation_doi]').attr('content');
    var twitter = new $.fn.twitter();
    twitter.getSidebarTweets(doi);
  }
})(jQuery);
















