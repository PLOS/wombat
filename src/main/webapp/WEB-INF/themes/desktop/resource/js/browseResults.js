/**
 * Copyright (c) 2007-2013 by Public Library of Science
 */

//***************************************
// ALM Service and some other globals
//***************************************
var almService = new $.fn.alm(),
  ids = new Array(),
  //When we get the results back, we put those IDs into this list.
  confirmed_ids = new Array();

var keyDownEventHandler = function(e) {
  //console.log('key down event');

  if (e.which == 13) {
    saveAlert();

    //Prevent default event (Submits the form)
    e.preventDefault();
    return false;
  }

  if (e.which == 27) {
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
    url: '/search/saveJournalAlert.action',
    data: 'category=' + encodeURIComponent(category),
    dataType:'json',
    success: function(response) {
      if (response.exception) {
        var errorMessage = response.exception.message;
        $('#save-journal-alert-error').html('Exception: ' + errorMessage);
        return;
      }

      if (response.actionErrors && response.actionErrors.length > 0) {
        //The action in question can only return one message
        var errorMessage = response.actionErrors[0];
        $('#save-journal-alert-error').html(errorMessage);
        return;
      }

      removeModal();
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
    url: '/search/unsubscribeJournalAlert.action',
    data: 'category=' + encodeURIComponent(category),
    dataType:'json',
    success: function(response) {
      if (response.exception) {
        var errorMessage = response.exception.message;
        $('#save-journal-alert-error').html('Exception: ' + errorMessage);
        return;
      }

      if (response.actionErrors && response.actionErrors.length > 0) {
        //The action in question can only return one message
        var errorMessage = response.actionErrors[0];
        $('#save-journal-alert-error').html(errorMessage);
        return;
      }

      removeModal();
      //$("#save-journal-alert-link").removeClass("subscribed");
    },
    error: function(req, textStatus, errorThrown) {
      $('#save-journal-alert-error').html(errorThrown.message);
      console.log('error: ' + errorThrown.message);
    }
  });
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
  $('#btn-save-journal-alert').bind('click', saveAlert);
  $('.btn-cancel-alert').bind('click', removeModal);
  $('#btn-unsubscribe-journal-alert').bind('click', unsubscribeAlert);

  $('li[data-doi]').each(function(index, element) {
    ids[ids.length] = $(element).data('doi');
  });

  if(ids.length > 0) {
    almService.getArticleSummaries(ids, setALMSearchWidgets, setALMSearchWidgetsError);
  }
});



