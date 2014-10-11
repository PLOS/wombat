
(function ($) {

    /**
     * Subject area feedback mechanism
     */
    var handleFlagClick = function (event) {
        var categoryTerm = $(event.target).data("categoryname");
        var articleDoi = $(event.target).data("articledoi");
        var action = $(event.target).hasClass("flagged") ? "remove" : "add";

        $.ajax({
            type: 'POST',
            url: siteUrlPrefix + 'taxonomy/flag/' + action,
            data: { 'categoryTerm': categoryTerm, 'articleDoi': articleDoi },
            dataType: 'json',
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(errorThrown);
            },
            success: function (data) {
                action == "remove" ? $(event.target).removeClass("flagged") : $(event.target).addClass("flagged");
            }
        });
    };

    $('span.taxo-flag').on('click', handleFlagClick);

})(jQuery);