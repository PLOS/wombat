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

var CommentsClass = function () {

  var self = this;

  self.init = function () {

    $('.flag-link').click(function (e) {
      e.preventDefault();
      self.flagComment($(this));
    });

    $('.respond-link').click(function (e) {
      e.preventDefault();
      self.respondToComment($(this));
    });

  };

  self.respondToComment = function ($respondButton) {
    var commentID = $respondButton.closest('.comment').attr('data-id');

    var supportsFixedPosition = Modernizr.positionfixed;

    if (!supportsFixedPosition) { //if there is no support for fixed position, we need to handle the modal a different way

      //PL-INT - need to insert logic for capturing the current figure and loading the proper URL if browser doesn't support fixed position
      //Note that this function could be called from somewhere other than figures, though currently figures is the only template which implements a tabbed modal window
      window.location = 'temp-response-form.html';

    } else {

      siteContentClass.showModalWindow(self.loadResponseForm, commentID, "full"); //callback, options, method
      //PL-INT - implement proper data attributes for responding to a post

    }

  };

  self.loadResponseForm = function (commentID) {

    siteContentClass.$modalInfoWindow.find('.close').one('click', function (e) { //enable close functionality
      e.preventDefault();
      siteContentClass.hideModalWindow(null, null, "full"); //callback, removeContent, method
    });


    return $.ajax({
      url: "ajax/comment-response.html" //PL-INT should construct the URL dynamically here linking to the proper comment
    }).done(function (data) {
        siteContentClass.$modalInfoWindow.find(".modal-content").html(data);

        siteContentClass.$modalInfoWindow.find('#form-post').one('click', function (e) {
          e.preventDefault();
          self.submitResponseRequest(commentID);
        });

        siteContentClass.$modalInfoWindow.find('#form-cancel').one('click', function (e) {
          e.preventDefault();
          siteContentClass.hideModalWindow(null, null, "full");
        });

      });

  };

  self.submitResponseRequest = function (commentID) {
    var submissionData = {};

    submissionData.commentID = commentID;
    submissionData.subject = siteContentClass.$modalInfoWindow.find('#response-subject').val();
    submissionData.response = siteContentClass.$modalInfoWindow.find('#response-text').val();
    submissionData.compete = siteContentClass.$modalInfoWindow.find('.response-compete:checked').val();
    submissionData.competingInterest = siteContentClass.$modalInfoWindow.find('#competing-interest').val();

    //PL-INT - Insert code to submit the specified response, utilizing submissionData object for form values
    self.loadSubmissionConfirmation();

  };

  self.flagComment = function ($flagButton) {
    var commentID = $flagButton.closest('.comment').attr('data-id');

    var supportsFixedPosition = Modernizr.positionfixed;

    if (!supportsFixedPosition) { //if there is no support for fixed position, we need to handle the modal a different way

      //PL-INT - need to insert logic for capturing the current figure and loading the proper URL if browser doesn't support fixed position
      //Note that this function could be called from somewhere other than figures, though currently figures is the only template which implements a tabbed modal window
      window.location = 'temp-flag-form.html';

    } else {

      siteContentClass.showModalWindow(self.loadFlagForm, commentID, "full"); //callback, options, method
      //PL-INT - implement proper data attributes for flagging a post
    }

  };

  self.loadFlagForm = function (commentID) {

    siteContentClass.$modalInfoWindow.find('.close').one('click', function (e) { //enable close functionality
      e.preventDefault();
      siteContentClass.hideModalWindow(null, null, "full"); //callback, removeContent, method
    });


    return $.ajax({
      url: "ajax/comment-flag.html" //PL-INT should construct the URL dynamically here linking to the proper comment
    }).done(function (data) {
        siteContentClass.$modalInfoWindow.find(".modal-content").html(data);

        siteContentClass.$modalInfoWindow.find('#form-post').one('click', function (e) {
          e.preventDefault();
          self.submitFlagRequest(commentID);
        });

        siteContentClass.$modalInfoWindow.find('#form-cancel').one('click', function (e) {
          e.preventDefault();
          siteContentClass.hideModalWindow(null, null, "full");
        });

      });

  };

  self.submitFlagRequest = function (commentID) {
    var submissionData = {};

    submissionData.commentID = commentID;
    submissionData.reason = siteContentClass.$modalInfoWindow.find('.flag-reason:checked').val();
    submissionData.additional_info = siteContentClass.$modalInfoWindow.find('#additional-info').val();

    //PL-INT - Insert code to submit the specified comment for removal, utilizing submissionData object for form values
    self.loadSubmissionConfirmation();

  };

  self.loadSubmissionConfirmation = function () {
    return $.ajax({
      url: "ajax/comment-submitted.html" //PL-INT should construct the URL dynamically here linking to the proper comment
    }).done(function (data) {
        siteContentClass.$modalInfoWindow.find(".modal-content").html(data);
      });
  }

};

var commentsClass;

$(document).ready(function () {

  commentsClass = new CommentsClass();
  commentsClass.init();

});