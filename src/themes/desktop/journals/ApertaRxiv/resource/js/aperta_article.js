$(document).ready(function () {

  var doi = $('meta[name="citation_doi"]').attr("content"); // TODO: is there a better way?

  // Capture articleinfo and contributions
  var articleinfo = $("#articleText .articleinfo");
  articleinfo.remove();

  var contributions = $("#articleText .contributions");
  contributions.remove();

  // Create section collapse.
  var items = $("#articleText > *");
  items.each(function (index, item) {
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
      if (!item.id) {
        item.id = "sectionBox" + index;
      }

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

  // Create metrics collapse. Move to after authors.
  h2 = $('<h2 class="section-toggle" href="#metrics"><span>Metrics</span></h2>');
  h2.insertAfter($("#articleText > *:nth-child(4)")); // insert as fifth child
  $("#metrics").addClass("section-box");
  $("#metrics").insertAfter(h2);

  // Put articleinfo as Disclosures after authors.
  h2 = $('<h2 class="section-toggle" href="#disclosures"><span>Disclosures</span></h2>');
  h2.insertAfter($("#articleText > *:nth-child(6)")); // insert as seventh child
  articleinfo.attr("id", "disclosures");
  articleinfo.addClass("section-box hide");
  articleinfo.insertAfter(h2);

  // Sanitize supporting information.
  $("#articleText .figshare_widget").remove();
  $("#articleText .supplementary-material").each(function (index, item) {
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

  $("#share-options li a").click(function (event) {
    // TODO: link is not automatically opened, don't know why. So using window.open.
    window.open(event.currentTarget.getAttribute("href"));
  });

  // show-more authors
  $("#authors #authors-show-more").click(function () {
    $("#authors > .authors-show-more").addClass("hide");
    $("#authors > .on-show-more, #authors > .authors-show-less").removeClass("hide");
  });
  $("#authors #authors-show-less").click(function () {
    $("#authors").collapse("closeAll");
    $("#authors > .authors-show-more").removeClass("hide");
    $("#authors > .on-show-more, #authors > .authors-show-less").addClass("hide");
  });

  window.openORCID = function openORCID() {

    var corresponding_author_orcid_id = $('.author-corresponding').siblings('.author-orcid')
      .find('a').text().split('/').pop();

    var state_json = '{ "doi" : "' + doi + ' " , "orcid_id" : "' +
      corresponding_author_orcid_id + '" }';

    var encoded_state_json = btoa(encodeURIComponent(state_json));

    var redirect_uri = ORCID_REDIRECT_URI + "?state=" + encoded_state_json;

    var request_url = ORCID_HOST + "/oauth/authorize?client_id=" +
      ORCID_CLIENT_ID + "&response_type=code&scope=/authenticate&show_login=true&redirect_uri="
      + redirect_uri;

    var oauthWindow = window.open(request_url, "_blank",
      "toolbar=no, scrollbars=yes, width=500, height=600, top=500, left=500");
  };

  // fetch comments
  function loadCommentsTree(callback) {
    $.get("article/comments?id=" + encodeURIComponent(doi), function (text) {
      $("#commentstree").html(text);

      // top level "Add a Comment" is clicked.
      $("#comments .post_comment > a").click(function (event) {
        event.preventDefault();
        event.stopImmediatePropagation();
        comments.showRespondBox('0', 0, undefined, comments.refreshCallback);

        if (!$("#comments .post_comment > a").hasClass("inactive")) {
          $("#comments .post_comment > a").addClass("inactive");
        }
        return false;
      });

      if (callback) {
        callback();
      }
    });

  }

  $("#articleText").on("open.collapse", function (event) {
    var id = event.$box.attr("id");
    if (id == "comments") {
      loadCommentsTree();
    } else if (id == "metrics") {
      $.fn.updateCounters();
    }
  });
  $("#articleText").on("close.collapse", function (event) {
    if (event.$box.attr("id") == "comments") {
      setTimeout(function () {
        $("#commentstree").html("");
      }, 1000);
    }
  });
  $("#newCommentForm .link.cancel").click(function (event) {
    if ($("#comments .post_comment > a").hasClass("inactive")) {
      $("#comments .post_comment > a").removeClass("inactive");
    }
  });

  // this is global
  var comments = window.comments = new $.fn.comments();
  comments.addresses = {
    listThreadURL: "article/comment",
    submitDiscussionURL: "article/comments/new",
    submitFlagURL: "article/comments/flag",
    submitReplyURL: "article/comments/new"
  };

  comments.refreshCallback = function (data, index) {
    $("#articleText").collapse("close", "#comments");

    loadCommentsTree(function () {
      // update comment thread is moved to top.
      $("#threads").collapse("open", "#replybody-1");
      var dt = null;
      if (index && index.match(/^\d+$/)) {
        dt = $('[data-uri="' + data.createdCommentUri + '"]').parent();
      }
      if (dt && dt.length > 0) {
        setTimeout(function () {
          dt[0].scrollIntoView();
        }, 100);
      } else {
        $("#threads")[0].scrollIntoView();
      }
    });
  };

});
