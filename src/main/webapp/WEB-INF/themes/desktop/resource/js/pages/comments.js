/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

(function ($) {
  $.fn.comments = function () {

    // Constants filled in by FreeMarker and supplied to here from the webpage.
    this.addresses = null;
    this.indentationWidth = null;

    /**
     * Show an element with a user-friendly animation.
     * @param element  the element to show
     */
    function animatedShow(element) {
      element.show("blind", 500);
    }

    /**
     * Hide an element with a user-friendly animation.
     * @param toHide  the JQuery element to hide
     * @param callback  a callback to execute (if not null/false) after finishing the animation
     */
    function animatedHide(toHide, callback) {
      toHide.hide("blind", {direction: "vertical"}, 500, callback);
    }

    /**
     * Return the JQuery element for a reply.
     * @param replyId  the ID of the reply
     * @return {*} the element
     */
    function getReplyElement(replyId) {
      return $('#reply-' + replyId);
    }

    /**
     * Return the JQuery div that holds the replies to a parent comment.
     * @param parentId  the non-null ID of the parent comment
     * @return {*} the reply list div
     */
    function getReplyListFor(parentId) {
      return $('#replies_to-' + parentId);
    }

    /**
     * Produce a clone of a JQuery element with a new "id" attribute.
     * @param selector  a selector that will find the element to clone
     * @param id  the ID to assign to the new clone, or a false/null value for the clone to have no ID
     * @return {*}  the clone
     */
    function cloneWithId(selector, id) {
      var clone = $(selector).clone();
      return (id ? clone.attr('id', id) : clone.removeAttr('id'));
    }

    /**
     * Show a drop-down box beneath a reply to prompt a user action.
     *
     * @param replyId  the reply where the box should appear
     * @param typeToHide  the type of other box to hide before showing this one
     * @param typeToShow  the type of box to show
     * @param closeSelectors  JQuery selectors for everything that should close the box when clicked
     * @param setupCallback  a function (that takes the new box as an argument) to call to finish setting it up
     */
    this.showBox = function (replyId, typeToHide, typeToShow, closeSelectors, setupCallback) {
      var reply = getReplyElement(replyId);
      reply.find('.' + typeToHide + '_container').hide(); // Keep its input in case the user switches back

      // Set up the new HTML object
      var container = reply.find('.' + typeToShow + '_container');
      if (container.data('populated')) {
        // The HTML, and possibly some user input, is already in the container. Just display it.
        animatedShow(container);
        return;
      }
      container.data('populated', true); // so that the HTML won't get overwritten if the button is clicked again

      var box = cloneWithId('#' + typeToShow + '_prototype', null);
      setupCallback(box);

      // How to close the box and clear any input
      var closeFunction = function () {
        container.data('populated', false); // so that the HTML gets rebuilt the next time it's opened
        animatedHide(container, function () {
          box.remove(); // Avoid holding the input in memory until the box is re-opened (it would be overwritten anyway)
        });
      };
      for (var i = 0; i < closeSelectors.length; i++) {
        box.find(closeSelectors[i]).click(closeFunction);
      }

      // Display it
      container.html(box);
      box.show();
      animatedShow(container);


    };

    /**
     * Show the "report a concern" box beneath a reply, clearing the response box first if necessary.
     * @param replyId  the ID of the reply where the box should be shown
     */
    this.showReportBox = function (replyId, callback) {
      var outer = this;
      this.showBox(replyId, 'respond', 'report', ['.btn_cancel', '.close_confirm'],
        function (box) {
          box.find('.btn_submit').click(function () {
            outer.submitReport(replyId,$(this), callback);
          });
        });
    };

    function shiftCaptchaFormTo(replyBox) {
      $('#captchaForm').detach().appendTo(replyBox.find('.captchaContainer')).show();
    }

    /**
     * Show the "respond to this posting" box beneath a reply, clearing the report box first if necessary.
     * @param replyId  the ID of the reply where the box should be shown
     */
    this.showRespondBox = function (replyId, depth, eventElement, callback) {
      var replyElement = getReplyElement(replyId);
      replyElement.data('depth', depth);
      var outer = this;
      var parentTitle = replyElement.find('.response_title').text();
      this.showBox(replyId, 'report', 'respond', ['.btn_cancel'],
        function (box) {
          box.find('.btn_submit').click(function () {
            // Usually the Captcha form will already be here.  In case the user opened a second box and submitted from
            // the first box, shift the Captcha form back so they won't be confused if there is a validation failure.
            shiftCaptchaFormTo(box);

            if (callback) {
              if (replyId == "0") { // TODO: hack for Aperta top level submitDiscussion
                var articleDoi = replyElement.data("uri");
                outer.submitDiscussion(articleDoi, eventElement, replyElement, callback);
              } else {
                outer.submitResponse(replyId, $(this), callback);
              }
            } else {
              outer.submitResponse(replyId, $(this));
            }
          });
          
          box.find('[name="comment_title"]').attr("value", parentTitle ? 'RE: ' + parentTitle : '');
          outer.wireCompetingInterestRadioButtons(box);

          shiftCaptchaFormTo(box);
        });
    };

    /**
     * Under "competing interests", set up the radio buttons to disable the input text area when set to "no".
     * @param replyElement  the reply to set up
     */
    this.wireCompetingInterestRadioButtons = function (replyElement) {
      replyElement.find('input:radio[name="competing"]').change(function (event) {
        var ciIsYes = event.target.value;
        var ciText = replyElement.find('textarea[name="competing_interests"]');
        ciText.attr('disabled', (ciIsYes ? null : 'disabled'));
      });
    };

    /**
     * Send an Ajax request to the server, using parameters appropriate to this page.
     * @param url  the URL to send the Ajax request to
     * @param data  an object to send as the request data
     * @param success  callback for success (per $.ajax)
     */
    function sendAjaxRequest(url, data, success) {
      $.ajax(url, {
        type: "post",
        dataType: "json",
        data: data,
        dataFilter: function (data, type) {
          // Remove block comment from around JSON, if present
          return data.replace(/(^\s*\/\*\s*)|(\s*\*\/\s*$)/g, '');
        },
        success: success,
        error: function (jqXHR, textStatus, errorThrown) {
        },
        complete: function (jqXHR, textStatus) {
        }
      });
    }

    /**
     * Submit a top-level response to an article over Ajax and show the result.
     * @param articleDoi the DOI of the article to which the user is responding
     */
    this.submitDiscussion = function (articleDoi, eventElement, replyElement, callback) {
      if (!replyElement) {
          replyElement = $('.reply');
      }
      var commentData = getCommentData(replyElement);
      commentData.target = articleDoi;

      var listThreadURL = this.addresses.listThreadURL; // make available in the local scope
      var submittedCallback = function (data) {
        if (callback) {
          callback(data, articleDoi);
        } else {
          window.location = listThreadURL + '?id=' + data.createdCommentUri;
        }
      };
      submit(replyElement, $('.error'), this.addresses.submitDiscussionURL, commentData, submittedCallback, eventElement);
    };

    /**
     * Submit the response data from a reply's response box and show the result. Talks to the server over Ajax.
     * @param parentId  the ID of the existing reply, to which the user is responding
     */
    this.submitResponse = function (parentId, eventElement, callback) {
      var replyElement = getReplyElement(parentId);
      var commentData = getCommentData(replyElement);
      commentData.inReplyTo = replyElement.data('uri');

      var outer = this;
      var submittedCallback = function (data) {
        if (callback) {
          callback(data, parentId);
        } else {
          // Reload the page so the user can see their comment
          location.reload(true);
          return; // TODO: Instead insert the comment into the page without a refresh
          var annotationUrl = null; // outer.addresses.getAnnotationURL; TODO: Set up with Ajax endpoint

          // Make a second Ajax request to get the new comment (we need its back-end representation)

          sendAjaxRequest(annotationUrl, {annotationId: data.replyId},
              function (data, textStatus, jqXHR) {
                  // Got the new comment; now add the content to the page
                  outer.putComment(parentId, data.annotationId, data.annotation);
              });
        }
      };
      var errorMsgElement = replyElement.find('.subresponse .error');
      submit(replyElement, errorMsgElement, this.addresses.submitReplyURL, commentData, submittedCallback, eventElement);
    };

    /**
     * Submit a report (flag) from a reply's "report a concern" button and show the result.
     * @param replyId  the ID of the reply being flagged
     */
    this.submitReport = function (replyId, eventElement, callback) {
      var reply = getReplyElement(replyId);
      var data = {
        target: reply.data('uri'),
        reasonCode: reply.find('input:radio[name="reason"]:checked').val(),
        comment: reply.find('[name="additional_info"]').val()
      };
      var reportDialog = reply.find('.review');
      var errorMsgElement = reportDialog.find('.error');
      var submittedCallback = function (data) {
        reportDialog.find('.flagForm').hide();
        animatedShow(reportDialog.find('.flagConfirm'));
        if (callback) {
          callback(data, replyId);
        }
      };
      submit(reply, errorMsgElement, this.addresses.submitFlagURL, data, submittedCallback, eventElement);
    };

    /**
     * Indicate that the client is waiting to contact the server by freezing the screen and showing the loading overlay.
     * Call {@code .close()} on the returned object to end.
     * @return {Object}  the overlay object from the "JQuery TOOLS Overlay" library
     */
    var freezeForLoading = (function () {

      return {
        load: function (eventElement) {
          $(eventElement).addClass("disabled");
          $(".loader").addClass("showing");
        },
        close: function (eventElement) {
          $(eventElement).removeClass("disabled");
          $(".loader").removeClass("showing");
        }
      }
    })();

    /**
     * Submit user input in general to the server.
     *
     * @param parentReply  the reply (as a JQuery element) under which the user gave input
     * @param errorMsgElement  the JQuery element in which to display any error messages
     * @param submitUrl  the URL to send the Ajax request to
     * @param data  the comment's content, as an object that can be sent to the server
     * @param submittedCallback  a function to call after the comment has been submitted without errors
     */
    function submit(parentReply, errorMsgElement, submitUrl, data, submittedCallback, eventElement) {
      // If another submission is unfinished, ignore the input
      if (parentReply.data('submitting')) return;
      parentReply.data('submitting', true);

      freezeForLoading.load(eventElement);

      // In case they were already shown from a previous attempt
      errorMsgElement.hide();
      errorMsgElement.find(".commentErrorMessage").hide();
      sendAjaxRequest(submitUrl, data,
        function (data, textStatus, jqXHR) {
          // The Ajax request had no errors, but the server may have sent back user validation errors.
          var errors = [];
          var serverErrors = (data.validationErrors == null) ? {} : data.validationErrors;
          for (var errorKey in serverErrors) {
            errors.push({key: errorKey, value: serverErrors[errorKey]});
          }

          if (serverErrors['captchaValidationFailure']) {
            // Need to refresh because the third-party server will not accept a second response for the same challenge.
            $("#recaptcha_reload").click();
          }

          if (errors.length > 0) {
            for (var i in errors) {
              var error = errors[i];
              var msg = errorMsgElement.find(".commentErrorMessage[data-error-key='" + error.key + "']");
              displayErrorMessage(msg, error.value);
            }

            animatedShow(errorMsgElement);

            // #respond starting a discussion
            // .report_container reporting a concern
            // .respond_container responding to this posting

            var commentParent = null;

            if (errorMsgElement.closest("#respond").length > 0) {
              commentParent = errorMsgElement.closest("#respond");
            } else if (errorMsgElement.closest(".report_container").length > 0) {
              commentParent = errorMsgElement.closest(".report_container");
            } else if (errorMsgElement.closest(".respond_container").length > 0) {
              commentParent = errorMsgElement.closest(".respond_container");
            } else {
              // do nothing, something went wrong
            }

            if (commentParent) {
              $('html, body').animate({scrollTop: commentParent.offset().top}, 500);
            }

          } else {
            // No validation errors, meaning the comment was submitted successfully and persisted.
            submittedCallback(data);
          }
          parentReply.data('submitting', false);
          freezeForLoading.close(eventElement);
        });
    }

    /**
     * Display an error message element. Fill in its text, replacing arguments with values if necessary.
     * <p>
     * If the message text contains any argument names surrounded by curly braces, then {@code values} is expected to
     * be an object with keys matching those names. Else, {@code values} is ignored and may be any type.
     */
    function displayErrorMessage(messageElement, values) {
      var errorMessage = messageElement.data('error-message');
      var modifiedMessage = errorMessage;
      var argumentPattern = /\{(.*?)\}/g;

      var match;
      while ((match = argumentPattern.exec(errorMessage)) !== null) {
        var argument = match[0];
        var value = values[match[1]];
        modifiedMessage = modifiedMessage.replace(argument, value);
      }

      messageElement.text(modifiedMessage);
      messageElement.show();
    }

    function getRecaptchaFields() {
      return {
        recaptcha_challenge_field: $('#recaptcha_challenge_field').val(),
        recaptcha_response_field: $('#recaptcha_response_field').val()
      };
    }

    /**
     * Pull the input for a submitted comment from the page.
     * @param replyElement  the existing reply, to which the user is responding, as a JQuery element
     * @return {Object}  the response data, formatted to be sent over Ajax
     */
    function getCommentData(replyElement) {
      var data = {
        commentTitle: replyElement.find('[name="comment_title"]').val(),
        comment: replyElement.find('[name="comment"]').val(),
        authorPhone: replyElement.find('[name="author_phone"]').val(),
        authorEmailAddress: replyElement.find('[name="author_email_address"]').val(),
        authorName: replyElement.find('[name="author_name"]').val(),
        authorAffiliation: replyElement.find('[name="author_affiliation"]').val()
      };

      var ciRadio = replyElement.find('input:radio[name="competing"]:checked');
      data.isCompetingInterest = Boolean(ciRadio.val());
      if (data.isCompetingInterest) {
        data.ciStatement = replyElement.find('[name="competing_interests"]').val();
      }

      $.extend(data, getRecaptchaFields(replyElement));

      return data;
    }

    /**
     * Pad a number to a certain width by adding leading zeroes.
     * @param value  a number
     * @param width  desired string width
     * @return {String}  the padded number
     */
    function padZeroes(value, width) {
      value = value.toString();
      while (value.length < width) {
        value = '0' + value;
      }
      return value;
    }

    /**
     * Add a comment in its proper place in its thread.
     * @param parentId  the ID of the comment's parent (defines where to put the new comment)
     * @param childId  the ID of the new comment
     * @param childReply  data for the new comment
     */
    this.putComment = function (parentId, childId, childReply) {
      var comment = cloneWithId('#reply-prototype', 'reply-' + childId);
      var parentReply = getReplyElement(parentId);

      // Clear and hide the old response box
      parentReply.find('.respond_container').hide().data('populated', false);
      parentReply.find('.subresponse').remove();

      var childDepth = parentReply.data('depth') + 1;
      comment.data('depth', childDepth);
      comment.attr('style', 'margin-left: ' + (childDepth * this.indentationWidth) + 'px');

      comment.find('.response_title').text(childReply.title);
      comment.find('.response_body').html(childReply.body);
      comment.find('.response_author_email_address').text(childReply.authorEmailAddress);
      comment.find('.response_author_name').text(childReply.authorName);

      var authorLink = comment.find('a.replyCreator');
      authorLink.text(childReply.creatorDisplayName);
      var authorHref = authorLink.attr('href');
      authorLink.attr('href', authorHref.replace(/\/\d*$/, '/' + childReply.creatorID));

      var parentAuthor = parentReply.find('a.replyCreator');
      var repliedTo = comment.find('a.repliedTo');
      repliedTo.text(parentAuthor.text());
      repliedTo.attr('href', parentAuthor.attr('href'));

      // Format the date. We need to duplicate the FreeMarker date formatting.
      var timestamp = new Date(childReply.createdFormatted); // UTC
      var timestampFormat
        = '<strong>' + $.datepicker.formatDate('dd M yy', timestamp) + '</strong> at <strong>'
        + padZeroes(timestamp.getUTCHours(), 2) + ':' + padZeroes(timestamp.getUTCMinutes(), 2)
        + ' GMT</strong>';
      comment.find('.replyTimestamp').html($(timestampFormat));

      var outer = this;
      comment.find('.flag.btn').click(function () {
        outer.showReportBox(childId);
      });
      comment.find('.respond.btn').click(function () {
        outer.showRespondBox(childId);
      });

      if (childReply.competingInterestStatement != null && childReply.competingInterestStatement.length > 0) {
        comment.find('.competing_interests.absent').remove();
        comment.find('.competing_interests.present .ciStmt').html(childReply.competingInterestStatement);
      } else {
        comment.find('.competing_interests.present').remove();
      }

      // Kludge to forcibly remove junk values from the new comment's containers. TODO: Prevent junk.
      comment.find('.report_container').hide().html('').data('populated', false);
      comment.find('.respond_container').hide().html('').data('populated', false);

      // Insert the new comment and display it to the user
      var replyList = getReplyListFor(parentId);
      comment.hide(); // so we can fade it in
      replyList.append(comment);
      replyList.append(cloneWithId('#replies_to-prototype', 'replies_to-' + childId));
      comment.fadeIn(1000); // to help the user notice where the new content is inserted
      $(window).scrollTop(comment.position().top);
    };

  };
}(jQuery));
