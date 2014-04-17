/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Provides functionality for the dynamic academic editor search on solr.
 * (/static/edboard.action).
 */

/**
 * Main function to get the Academic Editors from SOLR.
 * This clears out existing editors and searches for editors,
 * appending them to the main content area by last name.
 * All arguments are optional.  Possible arguments are:
 * {
 *    query : a 'q' parameter passed to solr (e.g. "expertise:Biology" or
 *        "expertise:Biology AND expertise:Chemistry"). Defaults to "*:*"
 *    formatSectionEds: boolean indicating whether to format section editors
 *        differently from academic editors. If true, the function will pull
 *        an image html string from a div with id "section_editor_icon".
 *        Defaults to true
 *    subjectsOnLine: Maximum number of subjects to show under an editor
 *        before the "show all" link.  -1 to show them all.  Defaults to 5
 *    highlight: boolean indicating whether to highlight snippets of results
 *        or not. Defaults to false.
 * }
 */

$.fn.edBoard = function () {
  var solrHost = $('meta[name=searchHost]').attr("content");
  var queryInfo = {};

  this.getEditors = function (args, currentPage) {

    if (typeof(currentPage) === 'undefined') {
      // args (query information) can be undefined or null (and it's ok to save undefined or null)
      // save the query information so it can be used when user pages through the search results
      queryInfo = args;
    } else {
      // user is paging through the search results, use the query information we saved
      args = queryInfo;
    }

    //set the default arguments
    args = $.extend({
      query: "*:*",
      formatSectionEds: true,
      subjectsOnLine: 5,
      highlight: false
    }, args);


    // display the spinner next to the search textbox every time user search and pages through results
    $("#loading .spinner").css("display", "inline");
    $("#loading").css("display", "block");

    if (typeof(currentPage) === 'undefined') {
      // set the default current page
      currentPage = 0;

      //clear out the existing editors
      $("div.editor").each(function (index, obj) {
        $(this).remove();
      });
      $("#all_editors").css("display", "none");

      args["initialLoad"] = true;

    } else {
      args["initialLoad"] = false;
    }

    args["currentPage"] = currentPage;

    //asking solr to do the highlighting is too slow
    //(qtime goes from ~4ms to ~90ms,
    //and you still have to do client side work)
    //so we'll do the highlighting on the client side
    args.queryTerms = [];
    if (args.highlight) {
      $.each(args.query.split(" AND "), function (index, item) {
        var term = item
            .replace(/.*:/, "")
            .replace(/[^a-zA-Z\- ()]/g, "")//filter out characters that break regex
            .replace(/\(/, "\\(")
            .replace(/\)/, "\\)");
        args.queryTerms.push(new RegExp("(" + term + ")", "ig"));
      });
    }

    if (args.formatSectionEds) {
      args.icon_html = $("#section_editor_icon").html();
    }

    var success = function(response) {
      args["data"] = response;
      this.setEditors(args);
    };

    this.getData(args, jQuery.proxy(success, this));
  };

  this.getData = function(args, callBack) {

    //make the request to solr
    $.jsonp({
      url: solrHost,
      context: document.body,
      timeout: 10000,
      callbackParameter: "json.wrf",
      data: {
        q: args.query,
        wt: "json",
        fq: "doc_type:(section_editor OR academic_editor) AND cross_published_journal_key:PLoSONE",
        fl: "doc_type,ae_name,ae_institute,ae_country,ae_subject",
        sort: "ae_last_name asc,ae_name asc",
        rows: 50,
        start: (args.currentPage * 50)
      },
      success: callBack,
      error: function (xOptions, textStatus) {
        console.log(textStatus);
      }
    });
  };

  //Setting the Editor results and making a call to load first page
  this.setEditors = function (json) {

    $(".spinner").fadeOut(1000);

    if (json.initialLoad) {
      $("#all_editors").css("display", "none");
    }

    this.loadCallback(json);

    if (json.initialLoad) {
      $("#all_editors").show("blind", 1000);
    }

  };

  //load callback function
  //defining it here allows us to reference the
  //query terms for highlighting
  this.loadCallback = function (json) {

    var editorsDiv = $("#all_editors");
    editorsDiv.empty();
    editorsDiv.append("</br>")

    var pageSize = 50;
    var numOfEditors = json.data.response.numFound;
    var currentPage = json.currentPage;

    // example: if currentPage is 2 and numOfEditors is 420
    // totalPages is 9 == ceil of 420/50
    // startIndex is 100, endIndex is 149.
    var totalPages = Math.ceil(numOfEditors / pageSize);
    var startIndex = currentPage * pageSize;

    // if last page has less than 50 Editors, use remaining Editors.
    var endIndex = Math.min(numOfEditors, startIndex + pageSize);

    if (numOfEditors > 0) {
      editorsDiv.append("Displaying  " + parseInt(startIndex + 1) + "-" + endIndex +
          " of " + parseInt(numOfEditors) + " Editors.");
    }
    editorsDiv.append("</br>");

    var singlePage = $("<div></div>");
    for (var i = 0; i < json.data.response.docs.length; i++) {

      var editor = json.data.response.docs[i];
      //create a div for the editor
      var entry = $("<div></div>").addClass("editor");

      var nameDiv = $("<div></div>").addClass("name").html("<b>" + editor.ae_name + "</b>");
      entry.append(nameDiv);

      //show an icon if the person is a section editor
      if (json.formatSectionEds && editor.doc_type == "section_editor") {
        nameDiv.prepend($("<div></div>").addClass("icon_holder").html(json.icon_html));
        nameDiv.append($("<span></span>").addClass("section_ed").html(" Section Editor"));
      }

      if (editor.ae_institute) {
        entry.append($("<div></div>")
            .addClass("organization")
            .html(editor.ae_institute.join(", ")));
      }

      if (editor.ae_country) {
        entry.append($("<div></div>")
            .addClass("location")
            .html(editor.ae_country.join(", ")));
      }

      if (editor.ae_subject) {
        //highlight the subjects
        if (json.highlight) {
          $.each(editor.ae_subject, function (index, subject) {
            $.each(json, function (index, term) {
              subject = subject.replace(term, "<span class=\"highlight\">$1</span>");
            });
            editor.ae_subject[index] = subject;
          });
        }

        entry.append($("<div></div>").addClass("expertise")
            .html("Expertise: " + editor.ae_subject.join(", ")));

      }

      entry.append("</br>");
      singlePage.append(entry);
    }
    editorsDiv.append("</br>");
    // Adding pagination component to div
    var paginationTop = this.paging(totalPages, currentPage);
    var paginationBottom = this.paging(totalPages, currentPage);
    editorsDiv.append(paginationTop, singlePage, paginationBottom);

    //unhide the editor sections that were
    //hidden on previous searches
    $(".hidden").removeClass("hidden");

    //hide the sections with no editors
    $(".editor_section").each(function (index, div) {
      var parent = $(".editor_section:eq(" + index + ")");
      var childrens = $(".editor_section:eq(" + index + ") > div.editor");

      if (childrens.length == 0) {
        parent.addClass("hidden");
        $("a[href=\"#" + div.id + "\"]").addClass("hidden");
      }
    });

    //show the loaded data
    $("#loading").fadeOut();
    $(editorsDiv).show("blind", 500);
  }; //end loadCallback


  this.paging = function (totalPages, currentPage) {
    var pagination = $("<div></div>");

    // no pagination if only one page
    if (totalPages > 1) {
      // otherwise adding pagination

      //the logic is same as in templates/journals/PLoSDefault/webapp/search/searchResults.ftl

      /*
       It supports the following use cases and will output the following:
       current page is zero based.
       page number = current page + 1.

       if current page is the start or end
       if current page is 0:
       < 1 2 3  ... 10 >
       if current page is 9:
       < 1 ...8 9 10 >

       if current page is 4:
       (Current page is greater then 2 pages away from start or end)
       < 1 ... 4 5 6 ... 10 >

       if current page is less then 2 pages away from start or end:
       current page is 7:
       < 1 ...7 8 9 10 >
       current page is 2:
       < 1 2 3 4 ... 10 >
       */

      pagination.attr("class", "pagination");

      var ellipsis = '<span>...</span>';
      var prev = '<span class="prev">&lt;</span>';
      var next = '<span class="next">&gt;</span>';

      if (totalPages < 4) {
        // if less than 4 pages, do not put "..."
        // put < with or without link depending on whether this is first page.
        if (currentPage > 0) {
          pagination.append(this.pagingAnchor((currentPage - 1), "&lt;", "prev"));
        }
        else {
          pagination.append(prev);
        }

        // put page number for all pages
        // do not put link for current page.
        for (var pageNumber = 0; pageNumber < totalPages; ++pageNumber) {
          if (pageNumber == currentPage) {
            pagination.append("<strong>" + (currentPage + 1) + "</strong>");
          }
          else {
            pagination.append(this.pagingAnchor(pageNumber, pageNumber + 1));
          }
        }

        // put > at the end with or without link depending on whether it is the last page.
        if (currentPage < (totalPages - 1)) {
          pagination.append(this.pagingAnchor(currentPage + 1, "&gt;", "next"));
        }
        else {
          pagination.append(next);
        }
      } else {
        // if >=4 pages then need to put "..."
        // put < and first page number always.
        // The link is present if this is not the first page.
        if (currentPage > 0) {
          pagination.append(this.pagingAnchor((currentPage - 1), "&lt;", "prev"));
          pagination.append(this.pagingAnchor(0, 1));
        }
        else {
          pagination.append(prev + '<strong>1</strong>');
        }
        // put the first "..." if this is more than 2 pages away from start.
        if (currentPage > 2) {
          pagination.append(ellipsis);
        }
        // put the three page numbers -- one before, current and one after
        for (var pageNumber = Math.min(currentPage, 0); pageNumber <= Math.max(3, currentPage + 2); ++pageNumber) {
          if ((pageNumber > 1 && pageNumber < totalPages && pageNumber > (currentPage - 1)
              || ((pageNumber == (totalPages - 2)) && (pageNumber > (currentPage - 2))))) {
            if ((currentPage + 1) == pageNumber) {
              pagination.append("<strong>" + pageNumber + "</strong>");
            }
            else {
              pagination.append(this.pagingAnchor(pageNumber - 1, pageNumber));
            }
          }
        }
        // if this is more than 2 pages away from last page, put last "..."
        if (currentPage < (totalPages - 3)) {
          pagination.append(ellipsis);
        }
        // put the last page number and >.
        // The link depends of whether this is the last page.
        if (currentPage < (totalPages - 1)) {
          pagination.append(this.pagingAnchor(totalPages - 1, totalPages));
          pagination.append(this.pagingAnchor(currentPage + 1, "&gt;", "next"));
        }
        else {
          pagination.append('<strong>' + totalPages + '</strong>' + next);
        }
      }
    }

    return pagination;
  };

  this.pagingAnchor = function (pageNumber, pagingText, className) {

    var success = function(event) {
      var pageNumber = parseInt($(event.target).text());
      if (isNaN(pageNumber)) {
        pageNumber = parseInt($('.pagination strong:first').text());
        if ($(event.target).text() === '<') {
          pageNumber = pageNumber - 1;
        } else {
          pageNumber = pageNumber + 1;
        }
      }
      this.getEditors(null, pageNumber - 1);
    }

    var anchor = $('<a></a>').attr({
      href: "#",
      title: "(" + (pageNumber + 1) + ")"
    }).html(pagingText).bind("click", jQuery.proxy(success, this))

    if (className) {
      anchor.attr("class", className);
    }
    return anchor;
  };


  /**
   * Initialize an autosuggest search box that queries solr for the
   * subjects starting with the user entered string.  Arguments should
   * be of the form:
   * {
   *   textbox: id text input box
   *   button: id search button for the box
   *   searchFunction: callback to take the value from the search box
   *                    and run a search.  Note that the autocomplete
   *                    supports multiple values, separated by commas
   * }
   */
  this.initializeAutoSuggest = function (args) {
    //the textbox to do auto suggext
    var textbox = $("#" + args.textBox);
    //search button to attach a listener to
    var searchButton = $("#" + args.searchButton);
    //reset button to attach a listener to
    var resetButton = $("#" + args.resetButton);

    //show a shaded suggestion in the box
    var default_value = "People, Areas of Expertise, ...";
    textbox.css("opacity", 0.5);
    textbox.val(default_value);

    textbox.focus(function (eventObj) {
      if ($(this).val() == default_value) {
        $(this).val("");
      }
      $(this).css("opacity", 1);
    });

    textbox.keyup(function (eventObj) {
      if (eventObj.keyCode == 13) {
        //Enter pressed
        //Select first element (if any) as value
        //Simulate filter click
        console.log($(this).data("autocomplete").menu.options);
        if ($(this).data("autocomplete").menu.options) {
          console.log($(this).data("autocomplete").menu.options[0]);
        }
      }
    });

    searchButton.click(function (eventObj) {
      var textbox = $("#" + args.textBox);

      if (textbox.val() == default_value) {
        textbox.val("");
      }

      args.searchFunction(textbox.val());
    });

    $("#" + args.textBox).autocomplete({
      select: function (event, ul) {
        var terms = event.target.value.split(",");

        //Pop last value
        terms.pop();

        if (terms.length > 0) {
          event.target.value = terms.join(", ") + ", " + ul.item.value;
        } else {
          event.target.value = ul.item.value;
        }

        return false;
      },

      focus: function (event, ul) {
        //Don't update the text of the box until a selection is made
        return false;
      },

      source: function (entry, response) {
        var actual_query = [];
        var terms = entry.term.split(",");

        if (terms.length > 0) {
          var prefix = $.trim(terms.pop());

          // once the subject facet and name facet queries complete,
          // invoke the response handler with the list of options.
          var success_handler = function (json_subjects, json_names) {
            var options = [];

            // areas and names total is at most 20
            var areas_count = json_subjects ? json_subjects.facet_counts.facet_fields.ae_subject_facet.length / 2 : 0;
            var names_count = json_names ? json_names.response.docs.length : 0;
            if (areas_count >= 10 && names_count >= 10) {
              areas_count = 10;
              names_count = 10;
            }
            else if (areas_count < 10 && names_count >= 10) {
              names_count = Math.min(names_count, 20 - areas_count);
            }
            else if (areas_count >= 10 && names_count < 10) {
              areas_count = Math.min(areas_count, 20 - names_count);
            }

            //push the subjects
            if (json_subjects && json_subjects.facet_counts.facet_fields.ae_subject_facet) {
              var subjects = json_subjects.facet_counts.facet_fields.ae_subject_facet;

              var subject_title = false;
              $.each(subjects, function (index, subject) {
                //solr returns a list that looks like
                // ["biology",2411, "biophysics",344]

                //Only push terms that haven't been selected.
                if ($.inArray(subject, terms) == -1) {
                  if ((index / 2) < areas_count && index % 2 == 0 && subjects[index + 1] > 0) {
                    if (!subject_title) {
                      subject_title = true;
                      options.push({ label: "<b>Areas of Expertise</b>", type: "html", value: ""});
                    }
                    var label = subject + " (" + subjects[index + 1] + ")";
                    ;
                    if (prefix.length > 0) {
                      label = "<b>" + label.substr(0, prefix.length) + "</b>" + label.substr(prefix.length);
                    }
                    options.push({ label: label, value: subject, type: "html" });
                  }
                }
              });
            }

            //push academic editors name
            if (json_names && json_names.response.docs) {
              var names = json_names.response.docs;

              var prefix_parts = prefix.split(" ");
              var name_title = false;
              if (names.length > 0) {
                $.each(names, function (index, name0) {
                  if (index < names_count) {
                    var people = name0.ae_name;
                    var last_first = null;
                    if (!name0.ae_last_name) {
                      var parts = people.split(" ");
                      var last_first = parts.pop() + ", " + parts.join(" ");
                    }
                    else {
                      // example: people = "Mamta Singh" (11)
                      // ae_last_name = "Singh" (5)
                      // index1 = 6, last_first = "Singh" + ", " + "Mamta"
                      var index1 = people.length - name0.ae_last_name.length;
                      last_first = people.substr(index1) + ", " + people.substr(0, index1 - 1);
                    }

                    //Only push terms that haven't been selected.
                    if ($.inArray('"' + people + '"', terms) == -1) {
                      if (!name_title) {
                        name_title = true;
                        options.push({ label: "<b>People</b>", type: "html", value: ""});
                      }
                      var label = last_first;
                      if (prefix.length > 0) {
                        for (var j = 0; j < prefix_parts.length; ++j) {
                          if (prefix_parts[j]) {
                            var re = RegExp("\\b(" + prefix_parts[j] + ")", "ig");
                            label = label.replace(re, "<b>$1</b>");
                          }
                        }
                      }
                      options.push({ label: label, value: '"' + people + '"', type: "html"});
                    }
                  }
                });
              }
            }

            options.push({ label: '<span style="font-style: italic; color: #808080;">keep typing to see more results...</span>', type: "html", value: ""});

            response(options);
          };

          // all except the last item is queried exactly via name or subject
          $.each(terms, function (index, term) {
            var item = $.trim(term);
            if (item.length > 0) {
              if (item[0] == '"' && item[item.length - 1] == '"') {
                actual_query.push("ae_name:" + item);
              }
              else {
                actual_query.push("(ae_subject:\"" + item + "\" OR ae_name:\"" + item + "\")");
              }
            }
          });

          // actual_query now has list of exact match query for all items except the last.

          // for subject facet query, if no query exists (only one item)
          // then use a wild-card *:* query (q).
          var query = actual_query.slice(0);

          if (query.length == 0) {
            query.push("*:*")
          }

          // subject facet query for 3 items A, B, C looks like
          // q=(A AND B)&facet.prefix=C...

          var data = [
            {name: "wt", value: "json"},
            {name: "q", value: query.join(" AND ")},
            {name: "fq", value: "doc_type:(section_editor OR academic_editor) AND cross_published_journal_key:PLoSONE"},
            {name: "facet", value: true},
            {name: "facet.field", value: "ae_subject_facet"},
            {name: "facet.sort", value: "index"},
            {name: "facet.mincount", value: 1},
            {name: "facet.limit", value: 20},
            {name: "facet.prefix", value: prefix.toLowerCase()}
          ];

          $.jsonp({
            url: solrHost,
            context: document.body,
            timeout: 10000,
            data: data,
            callbackParameter: "json.wrf",
            success: function (json_subjects, textStatus, xOptions) {
              if (prefix.length < 3) {
                success_handler(json_subjects);
                return;
              }

              // for items A, B, C the name query looks like
              // q=(A AND B AND (C OR C*))

              var query = actual_query.slice(0);
              var prefix_parts = prefix.split(" ");
              var last_part = prefix_parts.pop();
              for (var i = 0; i < prefix_parts.length; ++i) {
                if (prefix_parts[i]) {
                  query.push("ae_name_facet:" + prefix_parts[i].toLowerCase());
                }
              }
              query.push("(ae_name_facet:" + last_part.toLowerCase()
                  + "* OR ae_name_facet:" + last_part.toLowerCase() + ")");

              var data = [
                {name: "wt", value: "json"},
                {name: "q", value: query.join(" AND ")},
                {name: "fl", value: "ae_name, ae_last_name"},
                {name: "rows", value: 20},
                {name: "fq", value: "doc_type:(section_editor OR academic_editor) AND cross_published_journal_key:PLoSONE"},
                {name: "sort", value: "ae_last_name asc"},
                {name: "facet", value: false}
              ];

              $.jsonp({
                url: solrHost,
                context: document.body,
                timeout: 10000,
                data: data,
                callbackParameter: "json.wrf",
                success: function (json_names, textStatus, xOptions) {
                  success_handler(json_subjects, json_names);
                },
                error: function (xOptions, error) {
                  console.log(error);
                  success_handler(json_subjects);
                }
              });
            },
            error: function (xOptions, error) {
              console.log(error);
              success_handler();
            }
          });
        }
      }
    });
  };
};

$(function () {

  var edBoard = new $.fn.edBoard();

  edBoard.getEditors();

  edBoard.initializeAutoSuggest(
      {
        textBox: "searchBox",
        searchButton: "searchButton",
        resetButton: "clearFilter",

        searchFunction: function (userString) {
          var query = [];

          // each item is either name or subject.
          // if it is "quoted" it is only a name, otherwise
          // it is either name or subject.
          var items = userString.match(/(\".*?\")|[^,]+/g);
          if (items) {
            $.each(items, function (index, term) {
              var item = $.trim(term);
              if (item.length > 0) {
                if (item[0] == '"' && item[item.length - 1] == '"') {
                  var name = item.substring(1, item.length - 1);
                  query.push("ae_name:\"" + name + "\"");
                }
                else {
                  query.push("(ae_subject:\"" + item + "\" OR ae_name:\"" + item + "\")");
                }
              }
            });
          }

          edBoard.getEditors({
            "query": query.join(" AND "),
            highlight: true
          });
        }
      }
  );

  $("#searchBox").keyup(function (eventObj) {
    setTimeout(function () {
      console.log($("#searchBox").val() + ", " + $("#clearFilter").css("display"));
      if ($("#searchBox").val() && $("#clearFilter").css("display") != "block") {
        $("#clearFilter").css("display", "block");
      }
      else if (!$("#searchBox").val() && $("#clearFilter").css("display") == "block") {
        $("#clearFilter").css("display", "none");
      }
    }, 0);
    return true;
  });

  $("#clearFilter").click(function (eventObj) {
    $("#searchBox").val("");
    $("#clearFilter").css("display", "none");
    edBoard.getEditors();
    $("#searchBox").focus();
  });
});