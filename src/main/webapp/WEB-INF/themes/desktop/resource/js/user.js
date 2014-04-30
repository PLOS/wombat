
$(function () {

  var indexes = {
    profile:0,
    journalAlerts:1,
    savedSearchAlerts:2
  };

  var activeIndex = indexes[$("#user-forms").attr('active')] || 0;

  //TODO: Remove one day
  //Support for OLD URLs:
  //user/secure/editProfile.action?tabId=preferences
  //user/secure/editProfile.action?tabId=alerts$
  //user/secure/editProfile.action?tabId=savedSearchAlerts
  tabParam = getParameterByName("tabId");
  if(tabParam) {
    if(tabParam == "preferences") {
      activeIndex = 0;
    }

    if(tabParam == "alerts") {
      activeIndex = 1;
    }

    if(tabParam == "savedSearchAlerts") {
      activeIndex = 2;
    }
  }//End block to delete

  /*
  * Functions for the taxonomy browser
  **/
  var getJournalSubjectsFormValue = function(journal) {
    var results = [];

    $("input[name=journalSubjectFilters\\[\\'" + journal + "\\'\\]]").each(function(index, node) {
        results.push($(node).val())
      }
    );

    //console.log("Subject List from form:" + results);

    return results;
  };

  var addJournalSubjectsFormValue = function(journal, subject) {
    var form = $("form[name=userAlerts]");

    //console.log("Appending subject:" + subject);

    form.append($("<input type=\"hidden\" name=\"journalSubjectFilters['" + journal + "']\" value=\"" + subject + "\">"));
  };

  var selectSubject = function(subject) {
    var list = $('div.subjectsSelected ol');

    selectedSubjects.push(subject);

    findAndDisableSubject(subject);
    addJournalSubjectsFormValue(journal, subject);

    if($("div.noSubjectsSelected").is(":visible")) {
      $("div.noSubjectsSelected").slideUp({ complete: function() {
        $('div.subjectsSelected').slideDown();
      }});
    }

    var newNode = $("<li class=\"taxonomyNode\" style=\"display:none;\"><div class=\"filter-item\">" + subject + "&nbsp;<img src=\"/images/btn.close.png\"></div></li>");

    newNode.find("img").click(function(event) {
      removeSubject($.trim($(event.target).parent().text()));
    });

    list.append(newNode);

    newNode.slideDown();

    setSubjectSelectedState();
  };

  var removeAllSubjects = function() {
    //console.log("Selected subject list: " + selectedSubjects);

    var selectedSubjectsTemp = selectedSubjects;
    for(var i = 0; i < selectedSubjectsTemp.length; i++) {
      //console.log("Removing subject: " + selectedSubjectsTemp[i]);
      removeSubject(selectedSubjectsTemp[i]);
    }
  };

  var removeSubject = function(subject) {
    var list = $('div.subjectsSelected ol');

    //console.log("Subject List: " + selectedSubjects);

    selectedSubjects = $(selectedSubjects).filter(function(value) {
      return ($.trim(subject) != $.trim(value));
    });

    //console.log("Subject List: " + selectedSubjects);
    list.find("div").filter(function() {
      return ($.trim($(this).text()) == subject);
    }).parent().slideUp({ complete: function() {
      $(this).remove();
    }});

    findAndEnableSubject(subject);

    if(selectedSubjects.length == 0) {
      $("div.subjectsSelected").slideUp({ complete: function() {
        $('div.noSubjectsSelected').slideDown();
      }});
    }

    var form = $("form[name=userAlerts]");

    var selector = "input[name=\"journalSubjectFilters['" + journal + "']\"][value=\"" + subject + "\"]";
    //console.log(selector);
    form.find(selector).remove();
  };

  /**
   * If the user selects a subject without first making other form selections
   * Auto selected appropriate items
   */
  var setSubjectSelectedState = function() {
    $("#subjectSome_" + journal).attr('checked','true');
    $("form[name=userAlerts] input[name=weeklyAlerts][value=" + journal + "]").attr('checked', 'true');
  };

  /**
   * If the user selects a subject without first making other form selections
   * Auto selected appropriate items
   */
  var setSubjectUnSelectedState = function() {
    $("#subjectSome_" + journal).removeAttr('checked');
    $("#subjectAll_" + journal).removeAttr('checked');
    $("form[name=userAlerts] input[name=weeklyAlerts][value=" + journal + "]").removeAttr('checked');
    resetInitialSubjectList();
    removeAllSubjects();
  };

  var toggleSubjectSelector = function(eventObj, journal) {
    console.log('toggleSubjectSelector:' + $(eventObj.target).attr('class'));

    var selector = $("li.subjectAreaSelector[journal=" + journal + "]");
    //If the current clicked node is a child, find the parent instead
    var parentLI = $(eventObj.target).hasClass("filtered")?$(eventObj.target):$(eventObj.target).parents("li.filtered");
    var chkBox = parentLI.find("input[name=weeklyAlerts]");

    //If user clicked on the checkbox or the checkbox's label
    //force the state of the rest of the form to comply

    if(selector.is(":visible")) {
      //$(chkBox).attr("checked", false);
      $(selector).slideUp();
      $(parentLI)
        .find("span.alertToggle")
        .removeClass("alertToggleOn")
        .addClass("alertToggleOff");
      parentLI.removeClass("toggleOn");
    } else {
      $(chkBox).attr("checked", true);
      parentLI.addClass("toggleOn");
      $(selector).slideDown();
      $(parentLI)
        .find("span.alertToggle")
        .removeClass("alertToggleOff")
        .addClass("alertToggleOn");
    }

    eventObj.stopPropagation();
    eventObj.preventDefault();
  };

  var getTaxonomyTreeText = function(node) {
    var parent = node.parents(".taxonomyNode");

    if(parent.length > 0) {
      return(getTaxonomyTreeText($(parent[0])) + '/' + node.attr('data-key'));
    }

    return node.attr('data-key');
  };

  /* Find matching subjects in the tree and mark them selected */
  var findAndDisableSubject = function(subject) {
    $("#subjectAreaSelector span").filter(function() {
        return ($(this).text() == subject);
      }
    ).addClass("checked")
     .unbind('click');
  };

  /* Find matching subjects in the tree and enable them to be selected */
  var findAndEnableSubject = function(subject) {
    $("#subjectAreaSelector span").filter(function() {
      return ($(this).text() == subject);
     }
    ).removeClass("checked")
     .click(function(event) {
       selectSubject($(event.target).text());
      });
  };

  var createTaxonomyNodes = function(rootNode, response) {
    $.each(response.categories, function(key, val) {
      var img = "";

      if(val.length > 0) {
        img = "<image class=\"expand\" src=\"/images/transparent.gif\"/>";
      } else {
        img = "<image src=\"/images/transparent.gif\"/>";
      }

      var node = $("<li class=\"taxonomyNode\" data-key=\"" + key + "\">" + img + "<span>" + key + "</span><ol></ol></li>")
        .attr("data-value", key);

      $(rootNode).append(node);

      node.find("span").each(function() {
        var selected = false;

        for (var i = 0; i < selectedSubjects.length; i++) {
          if(selectedSubjects[i] == $(this).text()) {
            selected = true;
          }
        }

        //If the subject is already selected, don't set up events
        if(selected) {
          $(this).addClass("checked");
        } else {
          $(this).click(function(event) {
            selectSubject($(event.target).parents("li").attr("data-key"));
          });
        }
      });

      if(val.length > 0) {
        $(node).find('img').click(function(event) {
          //Already expanded state, remove children nodes
          if($(event.target).hasClass("expanded")) {
            $(node).find('ol').children().remove();
            $(event.target).removeClass("expanded");
            $(event.target).addClass("expand");
          } else {
            $(event.target).removeClass("expand");
            $(event.target).addClass("expanded");

            //Grab taxonomy data
            curTree = getTaxonomyTreeText(node);

            $.get("/taxonomy/json/" + curTree, $(this).serialize())
              .done(function(response) {
                createTaxonomyNodes($(node).find('ol'), response);
              })
              .fail(function(response) {
                displaySystemError($('form[name=userAlerts]'), response);
                console.log(response);
              });
          }
        });
      }
    });
  }

  var createTaxonomyNodesFromMap = function(rootNode, filter, map) {
    $.each(map, function(key, val) {
      console.log(key);
      var hasChildren = !$.isEmptyObject(val.children);
      var img = "<image " + (hasChildren?"class=\"expanded-nopointer\" ":"") + "src=\"/images/transparent.gif\"/>";
      var node = $("<li class=\"taxonomyNode\" data-key=\"" + key + "\">" + img + "<span>" +
        key.replace(new RegExp("(" + filter + ")", "gi"), "<b>$1</b>")
         + "</span><ol></ol></li>")
        .attr("data-value", key);

      $(rootNode).append(node);

      node.find("span").each(function() {
        var selected = false;

        selectedSubjects = getJournalSubjectsFormValue(journal);

        for (var i = 0; i < selectedSubjects.length; i++) {
          if(selectedSubjects[i] == $(this).text()) {
            selected = true;
          }
        }

        //If the subject is already selected, don't set up events
        if(selected) {
          $(this).addClass("checked");
        } else {
          $(this).click(function(event) {
            selectSubject($(event.target).parents("li").attr("data-key"));
          });
        }

        createTaxonomyNodesFromMap(node.find('ol'), filter, val.children);
      });
    });
  }

  //Grab base taxonomy data
  var resetInitialSubjectList = function() {
    $('#subjectAreaSelector').children().remove();
    $.get("/taxonomy/json", $(this).serialize())
    .done(function(response) {
      createTaxonomyNodes($('#subjectAreaSelector'), response);
    })
    .fail(function(response) {
      displaySystemError($('form[name=userAlerts]'), response);
      console.log(response);
    });
  };

  $(".subjectSearchInput[type='text']").autocomplete({
    select: function(event, ul) {
      $(".subjectSearchInput[type='text']").val(ul.item.value);
      return false;
    },

    focus: function(event, ul) {
      //Don't update the text of the box until a selection is made
      return false;
    },

    source: function(entry, response) {
      var termsHost = $('meta[name=termsHost]').attr("content");

      //make the request to solr
      $.jsonp({
        url: termsHost,
        context: document.body,
        timeout: 10000,
        callbackParameter: "json.wrf",
        data:{
          "terms": "true",
          "terms.fl" : "subject_facet",
          "terms.regex" : ".*" + entry.term + ".*",
          "terms.limit" : 25,
          "terms.sort" : "index",
          "terms.regex.flag" : "case_insensitive",
          "wt": "json"
        },
        success: function (data) {
          var options = [];
          //Every other element is what we want
          for(var i = 0; i < data.terms.subject_facet.length; i = i + 2) {
            options.push({
              label:data.terms.subject_facet[i].replace(new RegExp("(" + entry.term + ")", "gi"), "<b>$1</b>"),
              type: "html",
              value:data.terms.subject_facet[i]
            });
          }
          response(options);
        },
        error: function (xOptions, textStatus) {
          console.log(textStatus);
        }
      });
    }
  });

  var searchTaxonomy = function(filter) {
    if(filter.length == 0) {
      $('#subjectAreaSelector').children().remove();
      getInitialSubjectList();
      return;
    }

    if(filter.length < 3) {
      var node = $("<li><span class=\"required temp\">You must specify a term at least three letters long</span></li>");
      $('#subjectAreaSelector').children().remove();
      $('#subjectAreaSelector').append(node);
    } else {
      $.get("/taxonomy/json?filter=" + filter)
        .done(function(response) {
          $('#subjectAreaSelector').children().remove();
          if(response.map == null) {
            response.status = 1;
            response.statusText = 'Map is null';

            displaySystemError($('form[name=userAlerts]'), response);
          } else {
            if($.isEmptyObject(response.map)) {
              var node = $("<li><span class=\"required temp\">The term you specified did not return any matches</span></li>");
              $('#subjectAreaSelector').children().remove();
              $('#subjectAreaSelector').append(node);
            } else {
              createTaxonomyNodesFromMap($('#subjectAreaSelector'), filter, response.map.children);
            }
          }
        })
        .fail(function(response) {
          displaySystemError($('form[name=userAlerts]'), response);
          console.log(response);
        });
    }
  };

  /* There is partial support here for multiple journals using the selector for the future */
  var journal = $("li.subjectAreaSelector").attr("journal");

  selectedSubjects = getJournalSubjectsFormValue(journal);

  //Bind to UI events
  $("li div.filter-item img").click(function(event) {
    removeSubject($.trim($(event.target).parent().text()));
  });

  $("#subjectAll_" + journal).click(function(eventObj) {
    if(eventObj.target.checked) {
      //resetInitialSubjectList();
      //removeAllSubjects();

      $("form[name=userAlerts] input[name=weeklyAlerts][value=" + journal + "]").prop('checked', 'true');
    }
  });

  $("#subjectSome_" + journal).click(function (eventObj) {
    setSubjectSelectedState();
  });

  $("#alert-form ol > li.filtered li.alerts-weekly label").click(function(eventObj) {
    var selector = $("li.subjectAreaSelector[journal=" + journal + "]");

    if(!selector.is(":visible")) {
      $("#alert-form ol > li.filtered input[name=weeklyAlerts]").click();
    }

    eventObj.stopPropagation();
  });

  $("#alert-form ol > li.filtered input[name=weeklyAlerts]").click(function(eventObj) {
    //Kind of kludge, but this forces the event to be handled at the parent level
    var selector = $("li.subjectAreaSelector[journal=" + journal + "]");
    if(!selector.is(":visible")) {
      $("#alert-form ol > li.filtered").click();
    }
  });

  $("#alert-form ol > li.filtered").click(function (eventObj) {
    toggleSubjectSelector(eventObj, journal);
  });

  $(":input[name='searchSubject_btn']").click(function(eventObj) {
    var filter = $(".subjectSearchInput[type='text']").val();

    if($.trim(filter).length > 0) {
      searchTaxonomy(filter);
    } else {
      resetInitialSubjectList();
    }
  });

  if($(".subjectSearchInput[type='text']").val()) {
    $("div.clearFilter").css("display", "block");
  }

  $(".subjectSearchInput[type='text']").keydown(function(eventObj) {
    //If user pressed enter, don't submit for form, just do the taxonomy search
    setSubjectSelectedState();

    if(eventObj.which == 13) {
      eventObj.preventDefault();
      eventObj.stopPropagation();
      if ($(eventObj.target).val()) {
        $(".subjectSearchInput[type='text']").autocomplete("close");
        searchTaxonomy($(eventObj.target).val());
      }
    }
  });

  $(".subjectSearchInput[type='text']").keyup(function(eventObj) {
    if ($(eventObj.target).val()) {
      $("div.clearFilter").css("display", "block");
    } else {
      $("div.clearFilter").css("display", "none");
    }
  });

  $("div.clearFilter").click(function(eventObj) {
    $(".subjectSearchInput[type='text']").val("");
    $("div.clearFilter").css("display", "none");
    $(".subjectSearchInput[type='text']").focus();
    $('#subjectAreaSelector').children().remove();
    resetInitialSubjectList();
  });

  if($('#subjectAreaSelector').length) {
    resetInitialSubjectList();

    //Don't bubble up the event
    $("li.alerts-weekly input").click(function (eventObj) {
      eventObj.stopPropagation();
    });
  }

  /*
   * End functions for the taxonomy browser
   *
   **/

  //setup tabs
  $("#user-forms").tabs();

  var $panes = $(this).find('div.tab-pane');
  var $tab_nav = $(this).find('div.tab-nav');
  var $tab_lis = $tab_nav.find('li');

  $tab_lis.removeClass('active');
  $panes.hide();

  $tab_lis.eq(activeIndex).addClass('active');
  $panes.eq(activeIndex).show();

  //checkboxes on the alerts form
  $("#checkAllWeekly").change(function () {
    $("li.alerts-weekly input").not(":first")
      .attr("checked", $(this).is(":checked"));
  });

  $("#checkAllMonthly").click(function () {
    $("li.alerts-monthly input").not(":first")
      .attr("checked", $(this).is(":checked"));
  });

  //checkboxes on the search alerts form
  $("#checkAllWeeklySavedSearch").change(function () {
    $("li.search-alerts-weekly input").not(":first")
        .attr("checked", $(this).is(":checked"));
  });

  $("#checkAllMonthlySavedSearch").click(function () {
    $("li.search-alerts-monthly input").not(":first")
        .attr("checked", $(this).is(":checked"));
  });

  $("#checkAllDeleteSavedSearch").click(function () {
    $("li.search-alerts-delete input").not(":first")
        .attr("checked", $(this).is(":checked"));
  });

  var confirmedSaved = function() {
    var confirmBox = $("#save-confirm");

    //Set the center alignment padding + border see css style
    var popMargTop = ($(confirmBox).height() + 24) / 2;
    var popMargLeft = ($(confirmBox).width() + 24) / 2;

    $(confirmBox).css({
      'margin-top' : -popMargTop,
      'margin-left' : -popMargLeft
    });

    //Fade in the Popup
    $(confirmBox).fadeIn(500)
      .delay(1000)
      .fadeOut(1000);
  };

  //Remove error messages before adding new ones
  var cleanMesssages = function() {
    $("span.required.temp").each(function(index, val) {
      $(val).slideUp();
      setTimeout(function() { $(val).remove() }, 500);
    });

    $("div.required.temp").each(function(index, val) {
      $(val).slideUp();
      setTimeout(function() { $(val).remove() }, 500);
    });

    $("li").removeClass("form-error");
  }

  var validateProfileResponse = function(formObj, response) {
    var formBtn = $(formObj).find(":input[name='formSubmit']");

    if(!$.isEmptyObject(response.fieldErrors)) {
      var message = $('<span class="required temp">Please correct the errors above.</span>');

      formBtn.after(message);

      $.each(response.fieldErrors, function(index, value) {
        $(formObj).find(":input[name='" + index + "']").each(function(formIndex, element) {
          //Append class to parent LI
          $(element).parent().addClass("form-error");
          $(element).after(" <span class='required temp'>" + response.fieldErrors[index] + "</span>");
        });
      });
    } else {
      return true;
    }
  };

  var validateAlertsResponse = function(formObj, response) {
    //This action can only return one error
    if(response.actionErrors.length > 0) {
      //Display a message at the bottom of the form
      $(formObj).find(":input[name='formSubmit']:eq(1)").each(function(index, val) {
        var message = $('<span style=\"display:none\" class="required temp">Please correct the error listed above.</span>');
        $(val).after(message);
        message.slideDown();
      });

      if($(formObj).find("div.noSubjectsSelected").is(":visible")) {
        $(formObj).find("div.noSubjectsSelected:eq(0)").each(function(index, val) {
          var message = $('<div  style=\"display:none\" class="required temp">' + response.actionErrors[0] + '</div>');
          $(val).after(message);
          message.slideDown();
        });
      } else {
        $(formObj).find(":input[name='formSubmit']:eq(0)").each(function(index, val) {
          var message = $('<div  style=\"display:none\" class="required temp">' + response.actionErrors[0] + '</div>');
          $(val).before(message);
          message.slideDown();
        });
      }
    } else {
      return true;
    }
  };

  var displaySystemError = function(formObj, response) {
    var message = "System error.  Code: " + response.status + " (" + response.statusText + ")";
    var formBtn = $(formObj).find(":input[name='formSubmit']");

    if($(formObj).find("span.required.temp").size() == 0) {
      var errorP = $('<span class="required temp"/>');

      errorP.text(message);
      $(formBtn).after(errorP);
    } else {
      $(formBtn).find("span.required.temp").text(message);
    }
  };

  //Make the forms submit via ajax
  $('form[name=userForm]').submit(function(event) {
    event.preventDefault();

    $.post("/user/secure/saveProfileJSON.action", $(this).serialize())
      .done(function(response) {
        cleanMesssages();
        if(validateProfileResponse($('form[name=userForm]'), response)) {
          confirmedSaved();
        }
      })
      .fail(function(response) {
        displaySystemError($('form[name=userForm]'), response);
        console.log(response);
      });
  });

  $('form[name=userAlerts]').submit(function(event) {
    event.preventDefault();

    $.post("/user/secure/saveUserAlertsJSON.action", $(this).serialize())
      .done(function(response) {
        cleanMesssages();
        if(validateAlertsResponse($('form[name=userAlerts]'), response)) {
          confirmedSaved();

          if($("#subjectAll_" + journal).is(':checked')) {
            removeAllSubjects();
          };
        }
      })
      .fail(function(response) {
        displaySystemError($('form[name=userAlerts]'), response);
        console.log(response);
      });
  });

  $('form[name=userSearchAlerts]').submit(function(event) {
    event.preventDefault();

    $.post("/user/secure/saveSearchAlertsJSON.action", $(this).serialize())
      .done(function(json) {
        //There is no form to validate
        $.each(json.deleteAlerts, function(index, value) {
          $("#saID" + value).slideUp(400, function(target) {
            $("#saID" + value).remove();

            if($(".saID").size() == 0) {
              $("#saOL").slideUp();
              $("#sa_none_defined").slideDown();
            }
          });
        });
        confirmedSaved();
      })
      .fail(function(response) {
        displaySystemError($('form[name=userSearchAlerts]'), response);
        console.log(response);
      });
  });
});