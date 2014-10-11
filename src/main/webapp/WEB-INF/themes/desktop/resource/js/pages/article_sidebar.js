/**
 * Subject area feedback mechanism
 */
var handleFlagClick = function(event) {
    var categoryName = this.dataset("categoryname");
    var articleDoi = this.dataset("articledoi");
    var action = $(event.target).hasClass("flagged") ? "remove" : "add";

    $.ajax({
        type: 'POST',
        url:'/taxonomy/flag/' + action,
        data: { 'categoryName': categoryName, 'articleDoi': articleDoi },
        dataType:'json',
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
        },
        success:function (data) {
            if (action == "remove") {
                $(event.target).removeClass("flagged");
            else
                $(event.target).addClass("flagged");
            }
        }
    });
};

$('#subject-areas-container span.taxo-flag').on('click', handleFlagClick());
