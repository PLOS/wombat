(function ($) {

  $(document).ready(function() {
    RangeDatepicker.init($('#dateFilterStartDate'), $('#dateFilterEndDate'));

    $('#sortOrder').on('change', function() {
      $('#searchResultsForm').submit();
    });

    $('#resultsPerPageDropdown').on('change', function() {

      // Due to the way the CSS for the page currently works, it's difficult to have the <form>
      // extend all the way down to this dropdown, so we instead set a hidden field here.
      $('#resultsPerPage').val($('#resultsPerPageDropdown').val());
      $('#searchResultsForm').submit();
    });

    // Code to make tooltips around disabled controls work on tablets.

    var $visibleTooltip;

    $('[data-js-tooltip-hover=trigger]').on('click', function() {

      // Since the two icons are close together, we have to hide the adjacent
      // tooltip first if it is visible.
      if ($visibleTooltip !== undefined) {
        $visibleTooltip.removeClass('visible');
      }
      $visibleTooltip = $(this).find('[data-js-tooltip-hover=target]');
      $visibleTooltip.addClass('visible');

      // handling click on whole document is not working, and
      // probably not a good idea anyway. So I am auto-hiding the
      // tooltip after a timeout.
      var $toolTip = $visibleTooltip;
      setTimeout(function() {
        $toolTip.removeClass('visible');
      }, 5000);
    });
  });
  // making the checkbox also link in the filter column
  $('#searchFilters li a input').on('change',function(){
    var filterlink = $(this).parent('a').attr('href');
     window.location.assign(filterlink);
  });

  // initialize toggle for search filter item list
  plos_toggle.init();
// advanced search opens on empty tag.
  if($('#searchControlBarForm').attr('data-advanced-search')) {
    $('#simpleSearchLink, .edit-query').show();
    $('#advancedSearchLink').hide();
    
    AdvancedSearch.init('.advanced-search-container', function (err) {
      // Only show after it has been initialized
      $('.advanced-search-container').show();
      $('.advanced-search-inputs-container input[type=text]').first().focus();
    });
    
  }

  // Advanced search behaviour
  $('.advanced-search-toggle-btn').on('click', function (e) {
    e.preventDefault();
    $('.advanced-search-toggle-btn, .edit-query').toggle();
    if (AdvancedSearch.isInitialized('.advanced-search-container')) {
      $('.advanced-search-container').slideUp(function () {
        // Only destroy after it has been hidden
        AdvancedSearch.destroy('.advanced-search-container');
      });
    } else {
      AdvancedSearch.init('.advanced-search-container', function (err) {
        if (err) return console.log(err.message);
        // Only show after it has been initialized
        $('.advanced-search-container').slideDown();
      });
    }
  });

})(jQuery);

var keyDownEventHandler = function(e) {
  //console.log('key down event');

  if (e.which === 13) {
    saveSearchAlert();

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

var saveSearch = function() {
  var name = $('#text_name_savedsearch').val();
  var query = $('#alert_query_savedsearch').val();
  var weekly = $('#cb_weekly_savedsearch').is(':checked');
  var monthly = $('#cb_monthly_savedsearch').is(':checked');

  $.ajax({
    type: 'POST',
    url: 'searchalert/add',
    data: 'name=' + encodeURIComponent(name)
          + "&query=" + encodeURIComponent(query)
          + (weekly ? "&frequency=weekly" : "") + (monthly ? "&frequency=monthly" : ""),
    dataType:'json',
    success: function(response) {
      if (response.error) {
        var errorMessage = response.error;
        $('#span_error_savedsearch').html(errorMessage);
        return;
      }

      removeModal();
    },
    error: function(req, textStatus, errorThrown) {
      $('#span_error_savedsearch').html(errorThrown);
      console.log('error: ' + errorThrown);
    }
  });
};

var showModalForLogin = function(e) {
  $('#span_error_savedsearch').html('');

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

var showModalForSavedSearch = function(e) {
  ////if the request is from author/editor facet for save search, setting the search name with anchor name.
  ////else the regular search term is used.
  //if($(this).attr('name')) {
  //  $('#text_name_savedsearch').val($(this).attr('name'));
  //}
  //else if ($('#searchOnResult')) {
  //  $('#text_name_savedsearch').val($('#searchOnResult').val());
  //}
  $('#text_name_savedsearch').val($('#controlBarSearch').val());
  $('#span_error_savedsearch').html('');

  //logic to show the pop-up
  var saveSearchBox = $('#save-search-box');

  //Fade in the Popup
  $(saveSearchBox).fadeIn(300);

  //Set the center alignment padding + border see css style
  var popMargTop = ($(saveSearchBox).height() + 24) / 2;
  var popMargLeft = ($(saveSearchBox).width() + 24) / 2;

  $(saveSearchBox).css({
    'margin-top' : -popMargTop,
    'margin-left' : -popMargLeft
  });

  // Add the mask to body
  $('body').append('<div id="mask"></div>');
  $('#mask').fadeIn(300);

  $(document).bind('keydown', keyDownEventHandler);
  $(document).bind('click', clickEventHandler);
  $('#text_name_savedsearch').focus();
  //This may seems a bit odd, but this sets the cursor at the end of the string
  var input = $('#text_name_savedsearch')[0];
  input.selectionStart = input.selectionEnd = input.value.length;

  return false;
};

$(document).ready(function() {
  $('#save-search-link').bind('click',showModalForSavedSearch);
  $('#login-link').bind('click',showModalForLogin);
  $('.btn-cancel-savedsearch').bind('click', removeModal);
  $('#btn-save-savedsearch').bind('click', saveSearch);
});
