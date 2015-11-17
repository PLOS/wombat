(function ($) {

  $( document ).ready(function() {

    var categoryDisplayNameMap = { "News": "News Media Coverage", "Blog": "Blog Coverage", "Other": "Related Resources" };

    //Create the HTML block for the media coverage
    var createReferencesHTML = function(categorizedResults) {
      var html = $('#media-coverage-data');
      var keys = Object.keys(categorizedResults);
      for(var a = 0; a < keys.length; a++) {
        var category = keys[a];
        var categoryDisplay = categoryDisplayNameMap[category];

        if(categorizedResults[category].length > 0) {
          $(html).append($('<h3></h3>').html(categoryDisplay));
          var list = $('<ul></ul>');
          for(var b = 0; b < categorizedResults[category].length; b++) {
            var curReference = categorizedResults[category][b];
            list.append(createReferenceLI(curReference, category));
          }

          $(html).append(list);
        }
      }

      return html;
    };

    function mediaReferenceSuccess (result) {
      //Put results into buckets

      //assuming here the request was properly formatted to only get media information
      var mediaSource = result.sources[0];
      //alert(mediaSource.name);

      //alert(Object.keys(mediaSource));


      // don't display anything if there isn't any data
      if (mediaSource.events.length > 0) {
        var categorizedResults = categorizeReferences(mediaSource.events);

        //Build up the UI
        var docFragment = createReferencesHTML(categorizedResults);

        docFragment.show("blind", 500);
      }
    }

    function mediaReferenceFailure(result) {
      // don't display anything if there is an error

      console.error(result);
    }

    //Put the ALM mediaTracker response array into buckets matching their types
    var categorizeReferences = function(response) {
      //The names of these buckets may have to be changed to reflect API changes
      var result = { "News": [], "Blog": [], "Other": [] };
      var typeGroupOther = result["Other"];

      for(var a = 0; a < response.length; a++) {
        var cur = response[a].event;
        var typeGroup = result[cur.type];

        if(typeof typeGroup === 'undefined') {
          typeGroupOther.push(cur);
        } else {
          typeGroup.push(cur);
        }
      }

      return result;
    };

    //Create the LI block for one referral
    var createReferenceLI = function(curReference, category) {
      var publication = "Unknown"
      if(curReference.publication.length > 0) {
        publication = curReference.publication;
      }

      var title = "Unknown";
      if(curReference.title.length > 0) {
        title = curReference.title;
      }

      var publication_date = "Unknown";
      if(curReference.published_on.length > 0) {
        var dateParts = /^(\d{4})-(\d{2})-(\d{2})T(.*)Z$/.exec(curReference.published_on);
        publication_date = $.datepicker.formatDate('dd M yy', new Date(dateParts[1], dateParts[2] - 1, dateParts[3]));
      }

      var htmlContent = '<b>' + publication + '</b>: "<a href="' + curReference.referral + '">' + title +
        '</a>"&nbsp;&nbsp;' + publication_date;

      if (category == 'Other') {
        htmlContent = '<b>' + publication + '</b>: "<a href="' + curReference.referral + '">' + title + '</a>"';
      }

      var liItem = $('<li></li>').html(htmlContent);

      return liItem;
    };

    var doi = $('#figshare-related').data('doi');
    $.fn.getMediaReferences(doi, mediaReferenceSuccess, mediaReferenceFailure);

    $("#media-coverage-modal").on('click','.button.primary', function (e) {
      $("#media-coverage-form :input + span.form-error, #mcform-captcha + span.form-error").text("");

      var data = {
        uri: "info:doi/"  + $('meta[name=citation_doi]').attr("content"),
        name: $('#mcform-name').val(),
        email: $('#mcform-email').val(),
        link: $('#mcform-link').val(),
        comment: $('#mcform-comment').val(),
        recaptcha_challenge_field: $('#recaptcha_challenge_field').val(),
        recaptcha_response_field: $('#recaptcha_response_field').val()
      };

      sendRequest("/article/mediaCoverageSubmit.action", data);
    });

    function sendRequest(url, data) {
      $.ajax(url, {
        type: "post",
        dataType:"json",
        data: data,
        dataFilter:function (data, type) {
          // Remove block comment from around JSON, if present
          return data.replace(/(^\s*\/\*\s*)|(\s*\*\/\s*$)/g, '');
        },
        success:function (data, textStatus, jqXHR) {
          // check to see if there was any error
          if (data.actionErrors && data.actionErrors.length > 0) {
            var fieldErrorKeys = $.map(data.fieldErrors, function (value, key) {
              return key;
            });

            if (fieldErrorKeys.length > 0) {
              // check for an error message for each field
              if (data.fieldErrors.link) {
                $("#mcform-link").next().text(data.fieldErrors.link);
              }

              if (data.fieldErrors.name) {
                $("#mcform-name").next().text(data.fieldErrors.name);
              }

              if (data.fieldErrors.email) {
                $("#mcform-email").next().text(data.fieldErrors.email);
              }

              if (data.fieldErrors.captcha) {
                $("#mcform-captcha").next().text(data.fieldErrors.captcha);
                Recaptcha.reload();
              }

            } else {
              // display the error message and close the form
              $('#media-coverage-form').hide();
              $('#media-coverage-failure').show();

              setTimeout(function() {
                $("#media-coverage-modal").dialog("close");
              }, 3000);
            }

          } else {
            // display success message and close the form
            $('#media-coverage-form').hide();
            $('#media-coverage-success').show();

            setTimeout(function() {
              $("#media-coverage-modal").dialog("close");
            }, 1500);

          }
        },
        error:function (jqXHR, textStatus, errorThrown) {
          // display the error message and close the form
          $('#media-coverage-form').hide();
          $('#media-coverage-failure').show();

          setTimeout(function() {
            $("#media-coverage-modal").dialog("close");
          }, 3000);
        }
      });
    }

    $(document.body).on('click',"#media-coverage-form-link", function (e) {
      $("#media-coverage-form :input + span.form-error, #mcform-captcha + span.form-error").text("");

      showRecaptcha('mcform-captcha');

      $('#media-coverage-success').hide();
      $('#media-coverage-failure').hide();

      $('#media-coverage-form').show();
      // clear input field values
      $('#media-coverage-form :input').not("#mcform-name, #mcform-email").val('');

      $("#media-coverage-modal").dialog({ autoOpen: false, modal: true, resizable: false, minWidth: 600, dialogClass: 'default-modal', title: 'Submit a link to media coverage of this article' });
      $("#media-coverage-modal").dialog("open");
    });

    $("#media-coverage-modal").on('click','.button.cancel', function (e) {
      $("#media-coverage-modal").dialog("close");
    });

    function showRecaptcha(element) {
      Recaptcha.create($('#reCaptcha-info').val(), element, {
        theme: "white",
        callback: Recaptcha.focus_response_field});
    }

  });

}(jQuery));
