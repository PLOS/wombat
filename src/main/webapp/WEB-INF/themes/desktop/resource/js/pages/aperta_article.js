
$(document).ready(function () {

    // Capture articleinfo and contributions
    var articleinfo = $("#articleText .articleinfo");
    articleinfo.remove();

    var contributions = $("#articleText .contributions");
    contributions.remove();

    // Create section collapse.
    var items = $("#articleText > *");
    items.each(function(index, item) {
        var a = $(item).find('a');
        var h2 = $(item).find("h2");
        if (!$(item).hasClass("toc-section") || !a.attr("title")) {
            // remove items without toc-section and title.
            //$(item).css("display", "none");
            $(item).remove();
        } else {
            // Move the section headers in an outside h2.
            $(item).addClass("section-box");
            if (index > 0) {
                $(item).addClass("hide");
            }
            item.id = "sectionBox" + index;

            h2.addClass("section-toggle");

            h2.attr("href", "#" + item.id);
            h2.html('<span>' + h2.html() + '</span>');
            h2.insertBefore($(item));
        }
    });

    // Create authors collapse. Move to after abstract.
    var h2 = $('<h2 class="section-toggle" href="#authors"><span>Authors</span></h2>');
    h2.insertAfter($("#articleText > *:nth-child(2)")); // insert as third child
    $("#authors").addClass("section-box");
    $("#authors").insertAfter(h2);

    // Put articleinfo as Disclosures after authors.
    h2 = $('<h2 class="section-toggle" href="#disclosures"><span>Disclosures</span></h2>');
    h2.insertAfter($("#articleText > *:nth-child(4)")); // insert as fifth child
    articleinfo.attr("id", "disclosures");
    articleinfo.addClass("section-box hide");
    articleinfo.insertAfter(h2);

    // Sanitize supporting information.
    $("#articleText .figshare_widget").remove();
    $("#articleText .supplementary-material").each(function(index, item) {
        var a = $(item).find(".siTitle > a");
        var text = a.text().trim().replace(/\s*\.$/, "");
        var type = $(item).find(".postSiDOI").text().trim();
        var match = type.match(/^.*?(\w+)\)$/);
        if (match) {
            type = match[1].toLowerCase();
        }
        var filename = a.attr("href").split("/").pop() + "." + type;
        a.text(text + " (" + filename + ")");
        $(item).html("");
        $(item).append('<span class="filetype ' + type + '"></span>');
        $(item).append(a);
    });

});
