/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//***************************************
// ALM Service and some other globals
//***************************************
var almService = new $.fn.alm(),
  ids = new Array(),
  //When we get the results back, we put those IDs into this list.
  confirmed_ids = new Array();

$(document).ready(
    function() {
      //***************************************
      //Form events linking in:
      //Note there is also some logic in global.js that binds to the submit event
      //***************************************

      $("#clearJournalFilter").click(function(eventObj) {
        $("input[name|='filterJournals']").each(function (index, element) {
          $(element).removeAttr('checked');
        });

        $("#searchStripForm").submit();
      });

      $("input[name|='filterJournals']").click(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#clearSubjectFilter").click(function(eventObj) {
        $("input[name|='filterSubjects']").each(function (index, element) {
          $(element).removeAttr('checked');
        });

        $("#searchStripForm").submit();
      });

      $("input[name|='filterSubjects']").click(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#clearDateFilter").click(function(eventObj) {
        $("input[name|='filterStartDate']").each(function (index, element) {
          $(element).val('');
        });

        $("input[name|='filterEndDate']").each(function (index, element) {
          $(element).val('');
        });

        $("#searchStripForm").submit();
      });

      $("input[name|='filterDateButton']").click(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#clearAuthorFilter").click(function(eventObj) {
        $("input[name|='filterAuthors']").each(function (index, element) {
          $(element).removeAttr('checked');
        });

        $("#searchStripForm").submit();
      });

      $("#clearArticleTypesFilter").click(function(eventObj) {
        $("input[name|='filterArticleTypes']").each(function (index, element) {
          $(element).removeAttr('checked');
        });

        $("#searchStripForm").submit();
      });

      $("input[name|='filterArticleTypes']").click(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("input[name|='filterAuthors']").click(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#sortPicklist").change(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#pageSizePicklist").change(function(eventObj) {
        $('#db input[name="pageSize"]').val($("#pageSizePicklist").val());
        $("#searchStripForm").submit();
      });

      $("#pageSizePicklistFig").change(function(eventObj) {
        $('#db input[name="pageSize"]').val($("#pageSizePicklistFig").val());
        $("#searchStripForm").submit();
      });

      //***************************************
      //UI control events linking in:
      //***************************************
      var $hdr_search = $('#hdr-search-results');
      var $srch_facets = $('#search-facets');
      var $facets = $srch_facets.find('.facet');
      var $menu_itms = $srch_facets.find('div[data-facet]');

      $menu_itms.each(function() {
        $this = $(this);
        ref = $this.data('facet');
        if ($('#' + ref).length == 0) {
          $this.addClass('inactive');
        }
      });

      $menu_itms.on('click', function() {
        $this = $(this);
        if ($this.hasClass('active') || $this.hasClass('inactive')) { return false; }
        $menu_itms.removeClass('active');
        $facets.hide();
        $facets.find('dl.more').hide();
        $facets.find('.view-more').show();
        $this.addClass('active');
        ref = $this.data('facet');
        $('#' + ref).show();
      });

      $chkbxs = $srch_facets.find(':checkbox');
      $chkbxs.each(function() {
        chkbx = $(this);
        if (chkbx.prop('checked')) {
          chkbx.closest('dd').addClass('checked');
        }
      });

      $chkbxs.on('change', function() {
        chkbx = $(this);
        if (chkbx.prop('checked')) {
          chkbx.closest('dd').addClass('checked');
        } else {
          chkbx.closest('dd').removeClass('checked');
        }
      });

      $srch_facets.find('.view-more').on('click', function() {
        $(this).hide()
            .closest('div.facet').find('dl.more').show();
      });

      $srch_facets.find('.view-less').on('click', function() {
        this_facet = $(this).closest('div.facet');
        this_facet.find('dl.more').hide();
        this_facet.find('.view-more').show();
      });

      $('#startDateAsStringId').datepicker({
        changeMonth: true,
        changeYear: true,
        maxDate: 0,
        dateFormat: "yy-mm-dd",
        onSelect: function( selectedDate ) {
          $('#endDateAsStringId').datepicker('option', 'minDate', selectedDate );
        }
      });

      $('#endDateAsStringId').datepicker({
        changeMonth: true,
        changeYear: true,
        maxDate: 0,
        dateFormat: "yy-mm-dd",
        onSelect: function( selectedDate ) {
          $('#startDateAsStringId').datepicker('option', 'maxDate', selectedDate );
        }
      });

      $toggle_filter = $('<div class="toggle btn">filter by &nbsp;+</div>').toggle(function() {
            $srch_facets.show();
            $toggle_filter.addClass('open');
          }, function() {
            $srch_facets.hide();
            $toggle_filter.removeClass('open');
          }
      ).prependTo($hdr_search.find('div.options'));

      $('#sortPicklist').uniform();

      $('#pageSizePicklist').uniform();

      $('#pageSizePicklistFig').uniform();

      $("li[data-doi]:visible").each(function(index, element) {
        ids[ids.length] = $(element).data("doi");
      });

      almService.getArticleSummaries(ids, setALMSearchWidgets, setALMSearchWidgetsError);

      if ($('#resultView').val() === "fig") {
        showFigSearchView();
      }

      $('#search-view > span[class="list"]').click(function() {
        $('#fig-search-block').hide();
        $('#search-results-block').show();

        $('#resultView').val("");

        $('a[href]').attr('href', function(index, href) {
          var startIndex = href.indexOf('resultView=');
          var endIndex = 0;
          var newResultView = "";
          if (startIndex >= 0) {
            endIndex = href.indexOf('&', startIndex);
            if (endIndex >= 0) {
              href = href.replace(href.substring(startIndex, endIndex), 'resultView=')
            }
          }
          return href;
        });
      });

      $('#search-view > span[class="figs"]').click(function() {
        showFigSearchView();
      });

      var showModalForSavedSearch = function() {
        //if the request is from author/editor facet for save search, setting the search name with anchor name.
        //else the regular search term is used.
        if($(this).attr('name')) {
          $('#text_name_savedsearch').val($(this).attr('name'));
        }
        else if ($('#searchOnResult')) {
          $('#text_name_savedsearch').val($('#searchOnResult').val());
        }
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

      var saveSearch = function() {
        $('#searchName').val($('#text_name_savedsearch').val());
        $('#weekly').val($('#cb_weekly_savedsearch').is(':checked'));
        $('#monthly').val($('#cb_monthly_savedsearch').is(':checked'));

        var uqry= "";
        var query = $('#searchOnResult').attr('value');

        // if saving the author/editor facet search, then set the query to empty and
        //unformattedQry to facet value. (author:name or editor:name)
        if($('#saveSearchFacetVal').val()) {
          $('#searchOnResult').attr('value',"");
          uqry = "?unformattedQuery="+$('#saveSearchFacetVal').val();
        }
        $.ajax({
          type: 'POST',
          url: '/search/saveSearch.action'+uqry,
          data: $('#searchStripForm').serialize(),
          dataFilter: function (data, type) {
            return data.replace(/(\/\*|\*\/)/g, '');
          },
          dataType:'json',
          success: function(response) {
            if (response.exception) {
              var errorMessage = response.exception.message;
              $("#span_error_savedsearch").html("Exception: " + errorMessage);
              return;
            }
            if (response.actionErrors && response.actionErrors.length > 0) {
              //The action in question can only return one message
              var errorMessage = response.actionErrors[0];
              $("#span_error_savedsearch").html("Error: " + errorMessage);
              return;
            }

            $('#mask , #save-search-box').fadeOut(300 , function() {
              $('#mask').remove();
            });
          },
          error: function(req, textStatus, errorThrown) {
            $('#span_error_savedsearch').html(errorThrown.message);
            console.log("error: " + errorThrown.message);
          }
        });

        // Setting back the original value of the query.
        //This is required if the user wants to save the original search.
        $('#searchOnResult').attr('value',query);
        $('#saveSearchFacetVal').attr('value',"");
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

      var removeModal = function() {
        $('#mask , .inlinePopup').fadeOut(300 , function() {
          $('#mask').remove();
        });

        $(document).unbind('keydown', keyDownEventHandler);
        $(document).unbind('click', clickEventHandler);
      };

      var keyDownEventHandler = function(e) {
        if (e.which == 27) {
          removeModal();
        }
        if (e.which == 13) {
          saveSearch();

          //Prevent default event (Submits the form)
          e.preventDefault();
          return false;
        }
      };

      var clickEventHandler = function(e) {
        //If the click happens outside of the modal, close the modal
        if($(e.target).is("inlinePopup") || $(e.target).parents(".inlinePopup").size()) {
          //Do nothing (clicked inside box)
        } else {
          //Close the modal (clicked outside box);
          removeModal();
        }
      };

      $('.save-search-link').bind('click', showModalForSavedSearch);
      $('.login-link').bind('click', showModalForLogin);
      $('#btn-save-savedsearch').bind('click', saveSearch);
      $('.btn-cancel-savedsearch').bind('click', removeModal);
    });

function setFacetSearchValue(id){
  $('#saveSearchFacetVal').attr('value',id);
}

function showFigSearchView() {
  $('#search-results-block').hide();
  $('#fig-search-block').show();

  $('#resultView').val("fig");

  $('div[class="figure"] > img[fakesrc]').each(function () {
    $(this).attr("src", $(this).attr("fakesrc"));
    $(this).removeAttr("fakesrc");
  });

  $('a[href]').attr('href', function(index, href) {
    var startIndex = href.indexOf('resultView=');
    var endIndex = 0;
    var newResultView = "";
    if (startIndex >= 0) {
      endIndex = href.indexOf('&', startIndex);
      if (endIndex >= 0) {
        href = href.replace(href.substring(startIndex, endIndex), 'resultView=fig')
      }
    }
    return href;
  });
}
