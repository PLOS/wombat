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

//***************************************
// ALM Service and some other globals
//***************************************

var keyDownEventHandler = function(e) {
  //console.log('key down event');

  if (e.which === 13) {
    saveAlert();

    //Prevent default event (Submits the form)
    e.preventDefault();
    return false;
  }

  if (e.which === 27) {
    removeModal();
  }
};

var clickEventHandler = function(e) {
  //If the click happens outside of the modal, close the modal
  if($(e.target).is("inlinePopup") || $(e.target).parents('.inlinePopup').size()) {
    //Do nothing
    //console.log('inside box');
  } else {
    //Close the modal
    //console.log('outside box');
    removeModal();
  }
};

var removeModal = function() {
  $(document).unbind('keydown', keyDownEventHandler);
  $(document).unbind('click', clickEventHandler);

  $('#mask , .inlinePopup').fadeOut(300 , function() {
    $('#mask').remove();
  });
};

var saveAlert = function() {
  var category = $('#save-journal-alert-link').data('category');

  $.ajax({
    type: 'POST',
    url: '../subjectalert/add',
    data: 'subject=' + encodeURIComponent(category),
    dataType:'json',
    success: function(response) {
      if (response.error) {
        var errorMessage = response.error;
        $('#save-journal-alert-error').html(errorMessage);
        return;
      }

      removeModal();
      $("#save-journal-alert-link").addClass("subscribed");
      setTimeout(function() {
        $("#btn-save-journal-alert").val("Unsubscribe");
        $("#text-journal-alert-prompt").html("You are currently subscribed to:");
      }, 500);

    },
    error: function(req, textStatus, errorThrown) {
      $('#span_error_savedsearch').html(errorThrown.message);
      console.log('error: ' + errorThrown.message);
    }
  });
};

var unsubscribeAlert = function() {
  var category = $('#save-journal-alert-link').data('category');

  $.ajax({
    type: 'POST',
    url: '../subjectalert/remove',
    data: 'subject=' + encodeURIComponent(category),
    dataType:'json',
    success: function(response) {
      if (response.error) {
        var errorMessage = response.error;
        $('#save-journal-alert-error').html(errorMessage);
        return;
      }

      removeModal();
      $("#save-journal-alert-link").removeClass("subscribed");
      setTimeout(function() {
        $("#btn-save-journal-alert").val("Save");
        $("#text-journal-alert-prompt").html("Create a weekly email alert for:");
      }, 500);
    },
    error: function(req, textStatus, errorThrown) {
      $('#save-journal-alert-error').html(errorThrown.message);
      console.log('error: ' + errorThrown.message);
    }
  });
};

var saveOrUnsubscribeAlert = function() {
  var subscribed = $("#save-journal-alert-link").hasClass("subscribed");
  if (subscribed)
    unsubscribeAlert();
  else
    saveAlert();
};

var showModalForJournalAlert = function(e) {
  $('#save-journal-alert-error').html('');

  //logic to show the pop-up
  var journalAlertBox = $('#journal-alert-box');

  //Fade in the Popup
  $(journalAlertBox).fadeIn(300);

  //Set the center alignment padding + border see css style
  var popMargTop = ($(journalAlertBox).height() + 24) / 2;
  var popMargLeft = ($(journalAlertBox).width() + 24) / 2;

  $(journalAlertBox).css({
    'margin-top' : -popMargTop,
    'margin-left' : -popMargLeft
  });

  // Add the mask to body
  $('body').append('<div id="mask"></div>');
  $('#mask').fadeIn(300);

  $(document).bind('keydown', keyDownEventHandler);
  $(document).bind('click', clickEventHandler);

  return false;
};

var showModalForLogin = function(e) {
  $('#save-journal-alert-error').html('');

  //logic to show the pop-up
  var loginBox = $('#login-box');

  //Fade in the Popup
  $(loginBox).fadeIn(300);

  //Set the center alignment padding + border see css style
  var popMargTop = ($(loginBox).height() + 24) / 2;
  var popMargLeft = ($(loginBox).width() + 24) / 2;

  $(loginBox).css({
    'margin-top' : -popMargTop,
    'margin-left' : -popMargLeft
  });

  // Add the mask to body
  $('body').append('<div id="mask"></div>');
  $('#mask').fadeIn(300);

  $(document).bind('keydown', keyDownEventHandler);
  $(document).bind('click', clickEventHandler);

  return false;
};

$(document).ready(function() {
  $('#save-journal-alert-link').bind('click',showModalForJournalAlert);
  $('#login-link').bind('click',showModalForLogin);
  $('#btn-save-journal-alert').bind('click', saveOrUnsubscribeAlert);
  $('.btn-cancel-alert').bind('click', removeModal);
  $('#btn-unsubscribe-journal-alert').bind('click', unsubscribeAlert);
  $(".list-title").dotdotdot({
    height      : 45
  });
});



