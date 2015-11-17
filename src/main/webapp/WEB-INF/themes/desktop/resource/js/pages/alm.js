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
 * ambra.alm
 *
 * This class has utilities for fetching data from the ALM application.
 **/
(function ($) {

  var RESOURCE_PATH = '../resource/img';
  var ARTICLE = {
    citationDoi:  $('meta[name=citation_doi]').attr('content'),
    citationDate: $('meta[name=citation_date]').attr('content'),
    citationTitle: $('meta[name=citation_title]').attr('content')
  };

  $.fn.alm = function () {
    // Added two fallback option
    this.almHost = $('meta[name=almHost]').attr('content') || ALM_CONFIG.host || 'ALM_HOST_NOT_CONFIGURED';
    this.almAPIKey = $('meta[name=almAPIKey]').attr('content') || ALM_CONFIG.apiKey || 'ALM_KEY_NOT_CONFIGURED';

    // default will be 30
    this.almRequestBatchSize = parseInt($('meta[name=almRequestBatchSize]').attr('content')) || 30;

    this.isNewArticle = function (pubDateInMilliseconds) {
      //The article publish date should be stored in the current page is a hidden form variable
      var todayMinus48Hours = (new Date()).getTime() - 172800000;
      return todayMinus48Hours < pubDateInMilliseconds;
    };

    this.isArticle = function (doi) {
      return doi.indexOf('image') === -1;
    };

    this.validateDOI = function (doi) {
      if (doi == null) {
        throw new Error('DOI is null.');
      }
      doi = encodeURI(doi);

      return doi.replace(new RegExp('/', 'g'), '%2F').replace(new RegExp(':', 'g'), '%3A');
    };

    this.getCitesTwitterOnly = function (doi, callBack, errorCallback) {
      doi = this.validateDOI(doi);

      var request = doi + "&source_id=twitter";
      this.getData(request, callBack, errorCallback);
    };

    this.getMediaReferences = function (doi, callBack, errorCallback) {
      doi = this.validateDOI(doi);

      var request = doi + "&source_id=articlecoveragecurated";
      this.getData(request, callBack, errorCallback);
    };

    /*
     * Get summaries and counter data for the collection of article IDs
     * passed in.  If an article is not found, or a source data is not found
     * The data will be missing in the resultset.
     * */
    this.getArticleSummaries = function (dois, callBack, errorCallback) {
      var idString, a, startIndex, endIndex, total, requests = [];

      if (dois.length) {
        total = dois.length;
        startIndex = 0;
        endIndex = (total < this.almRequestBatchSize) ? total : this.almRequestBatchSize;
        while (startIndex < total) {
          idString = "";
          idString += this.validateDOI(dois[startIndex]);

          for (a = (startIndex + 1); a < endIndex; a++) {
            idString += "," + this.validateDOI(dois[a]);
          }

          var request = idString;

          // duplication of code from getData function
          var url = this.almHost + '?api_key=' + this.almAPIKey + '&ids=' + request;
          requests.push($.jsonp({
            url: url,
            context: document.body,
            timeout: 20000,
            callbackParameter: 'callback'
          }));

          startIndex = endIndex;
          endIndex = endIndex + this.almRequestBatchSize;
          if (endIndex > total) {
            endIndex = total;
          }
        }
      }

      $.when.apply($, requests).then(function() {
        // success / done
        var successData = [];

        if (arguments.length >= 2 && arguments[1] === 'success') {
          // single request
          successData = successData.concat(arguments[0]);
        } else {
          // multiple requests
          for (var i = 0; i < arguments.length; i++) {
            successData = successData.concat(arguments[i][0]);
          }
        }

        callBack(successData);
      }, function() {
        // failure
        errorCallback();
      });
    };

    /* Sort the chart data */
    this.sortByYearMonth = function (chartData1, chartData2) {
      if (parseInt(chartData1.year) < parseInt(chartData2.year)) {
        return -1;
      } else {
        if (parseInt(chartData1.year) === parseInt(chartData2.year)) {
          if (parseInt(chartData1.month) === parseInt(chartData2.month)) {
            return 0;
          } else {
            if (parseInt(chartData1.month) < parseInt(chartData2.month)) {
              return -1;
            } else {
              return 1;
            }
          }
        } else {
          return 1;
        }
      }
    };

    /*
     * Massage the chart data into a more 'chartable' structure
     **/
    this.massageChartData = function (sources, pubDateMS) {
      //Do some final calculations on the results
      var pubDate = new Date(pubDateMS),
          pubYear = pubDate.getFullYear();
      //Add one as getMonth is zero based
      var pubMonth = pubDate.getMonth() + 1,
          counterViews = null,
          pmcViews = null,
          result = {};

      for (var a = 0; a < sources.length; a++) {
        if (sources[a].name.toLowerCase() == "counter") {
          counterViews = sources[a].events;
          //Make sure everything is in the right order
          counterViews = counterViews.sort(this.sortByYearMonth);
        }

        if (sources[a].name.toLowerCase() == "pmc") {
          if (sources[a].events && sources[a].events.length > 0) {
            pmcViews = sources[a].events;
            //Make sure everything is in the right order
            pmcViews = pmcViews.sort(this.sortByYearMonth);
          }
        }

        if (sources[a].name.toLowerCase() == "relativemetric") {
          if (sources[a].events != null) {
            result.relativeMetricData = sources[a].events;
          }
        }
      }

      result.totalPDF = 0;
      result.totalXML = 0;
      result.totalHTML = 0;
      result.total = 0;
      result.history = {};

      //Don't display any data from counter for any date before the publication date
      for (var a = 0; a < counterViews.length; a++) {
        if (counterViews[a].year < pubYear || (counterViews[a].year == pubYear && counterViews[a].month < pubMonth)) {
          counterViews.splice(a, 1);
          a--;
        }
      }

      var cumulativeCounterPDF = 0,
          cumulativeCounterXML = 0,
          cumulativeCounterHTML = 0,
          cumulativeCounterTotal = 0;

      //Two loops here, the first one assumes there is no data structure
      //I also assume (for the cumulative counts) that results are in order date descending
      for (var a = 0; a < counterViews.length; a++) {
        var event = counterViews[a];
        var totalViews = this.parseIntSafe(event.html_views) + this.parseIntSafe(event.xml_views) +
            this.parseIntSafe(event.pdf_views);
        var yearMonth = this.getYearMonth(event.year, event.month);

        result.history[yearMonth] = {};
        result.history[yearMonth].source = {};
        result.history[yearMonth].year = event.year;
        result.history[yearMonth].month = event.month;
        result.history[yearMonth].source["counterViews"] = {};
        result.history[yearMonth].source["counterViews"].month = event.month;
        result.history[yearMonth].source["counterViews"].year = event.year;
        result.history[yearMonth].source["counterViews"].totalPDF = this.parseIntSafe(event.pdf_views);
        result.history[yearMonth].source["counterViews"].totalXML = this.parseIntSafe(event.xml_views);
        result.history[yearMonth].source["counterViews"].totalHTML = this.parseIntSafe(event.html_views);
        result.history[yearMonth].source["counterViews"].total = totalViews;

        cumulativeCounterPDF += this.parseIntSafe(event.pdf_views);
        cumulativeCounterXML += this.parseIntSafe(event.xml_views);
        cumulativeCounterHTML += this.parseIntSafe(event.html_views);
        cumulativeCounterTotal += totalViews;

        //Total views so far (for counter)
        result.history[yearMonth].source["counterViews"].cumulativePDF = cumulativeCounterPDF;
        result.history[yearMonth].source["counterViews"].cumulativeXML = cumulativeCounterXML;
        result.history[yearMonth].source["counterViews"].cumulativeHTML = cumulativeCounterHTML;
        result.history[yearMonth].source["counterViews"].cumulativeTotal = cumulativeCounterTotal;

        //Total views so far (for all sources)
        result.history[yearMonth].cumulativeTotal = this.parseIntSafe(result.total) + totalViews;
        result.history[yearMonth].cumulativePDF = result.totalPDF + this.parseIntSafe(event.pdf_views);
        result.history[yearMonth].cumulativeXML = result.totalXML + this.parseIntSafe(event.xml_views);
        result.history[yearMonth].cumulativeHTML = result.totalHTML + this.parseIntSafe(event.html_views);
        result.history[yearMonth].total = totalViews;

        //The grand totals
        result.totalPDF += this.parseIntSafe(event.pdf_views);
        result.totalXML += this.parseIntSafe(event.xml_views);
        result.totalHTML += this.parseIntSafe(event.html_views);
        result.total += totalViews;
      }

      result.totalCounterPDF = cumulativeCounterPDF;
      result.totalCounterXML = cumulativeCounterXML;
      result.totalCounterHTML = cumulativeCounterHTML;
      result.totalCouterTotal = cumulativeCounterTotal;

      var cumulativePMCPDF = 0;
      var cumulativePMCHTML = 0;
      var cumulativePMCTotal = 0;

      if (pmcViews != null) {
        for (var a = 0; a < pmcViews.length; a++) {
          var event = pmcViews[a];
          var totalViews = this.parseIntSafe(event["full-text"]) + this.parseIntSafe(event.pdf);

          // even if we don't display all the pmc data, the running total we display should be correct
          cumulativePMCPDF += this.parseIntSafe(event.pdf);
          cumulativePMCHTML += this.parseIntSafe(event["full-text"]);
          cumulativePMCTotal += totalViews;

          //Total views for the current period
          var yearMonth = this.getYearMonth(event.year, event.month);

          // if counter doesn't have data for this given month, we are going to ignore it.
          // we assume that counter data doesn't have any gaps

          // if we add the yearMonth here in the history,
          // * the order of the data in the history can get messed up.
          // * the code expects the counter data to exist for the given yearMonth in the history not just pmc data
          // See PDEV-1074 for more information
          if (result.history[yearMonth] == null) {
            continue;
          }

          result.history[yearMonth].source["pmcViews"] = {};

          //Total views so far (for PMC)
          result.history[yearMonth].source["pmcViews"].month = event.month;
          result.history[yearMonth].source["pmcViews"].year = event.year;
          result.history[yearMonth].source["pmcViews"].cumulativePDF = cumulativePMCPDF;
          result.history[yearMonth].source["pmcViews"].cumulativeHTML = cumulativePMCHTML;
          result.history[yearMonth].source["pmcViews"].cumulativeTotal = cumulativePMCTotal;

          result.history[yearMonth].source["pmcViews"].totalPDF = this.parseIntSafe(event.pdf);
          result.history[yearMonth].source["pmcViews"].totalXML = "n.a.";
          result.history[yearMonth].source["pmcViews"].totalHTML = this.parseIntSafe(event["full-text"]);
          result.history[yearMonth].source["pmcViews"].total = totalViews;

          //Total views so far
          result.history[yearMonth].total += totalViews;
          result.history[yearMonth].cumulativeTotal += totalViews;
          result.history[yearMonth].cumulativePDF += this.parseIntSafe(event.pdf);
          result.history[yearMonth].cumulativeHTML += this.parseIntSafe(event["full-text"]);

          //The grand totals
          result.totalPDF += this.parseIntSafe(event.pdf);
          result.totalHTML += this.parseIntSafe(event["full-text"]);
          result.total += totalViews;
        }
      }

      result.totalPMCPDF = cumulativePMCPDF;
      result.totalPMCHTML = cumulativePMCHTML;
      result.totalPMCTotal = cumulativePMCTotal;

      //PMC data is sometimes missing months, here let's hack around it.
      for (year = pubYear; year <= (new Date().getFullYear()); year++) {
        var startMonth = (year == pubYear) ? pubMonth : 1;
        for (month = startMonth; month < 13; month++) {
          //Skips months in the future of the current year
          //Month is zero based, '(new Date().getMonth())' is 1 based.
          if (year == (new Date().getFullYear()) && (month - 1) > (new Date().getMonth())) {
            break;
          }

          yearMonth = this.getYearMonth(year, month);

          if (result.history[yearMonth] != null &&
              result.history[yearMonth].source["pmcViews"] == null) {
            result.history[yearMonth].source["pmcViews"] = {};

            result.history[yearMonth].source["pmcViews"].month = month + 1;
            result.history[yearMonth].source["pmcViews"].year = year;
            result.history[yearMonth].source["pmcViews"].cumulativePDF = 0;
            result.history[yearMonth].source["pmcViews"].cumulativeHTML = 0;
            result.history[yearMonth].source["pmcViews"].cumulativeTotal = 0;
            result.history[yearMonth].source["pmcViews"].totalPDF = 0;
            result.history[yearMonth].source["pmcViews"].totalXML = 0;
            result.history[yearMonth].source["pmcViews"].totalHTML = 0;
            result.history[yearMonth].source["pmcViews"].total = 0;

            //Fill in the cumulatives from the previous month (If it exists)
            var prevMonth = 0;
            var prevYear = 0;

            if (month == 1) {
              prevMonth = 12;
              prevYear = year - 1;
            } else {
              prevMonth = month - 1;
              prevYear = year;
            }

            var prevYearMonthStr = this.getYearMonth(prevYear, prevMonth);

            if (result.history[prevYearMonthStr] != null &&
                result.history[prevYearMonthStr].source["pmcViews"] != null) {
              result.history[yearMonth].source["pmcViews"].cumulativePDF =
                  result.history[prevYearMonthStr].source["pmcViews"].cumulativePDF;
              result.history[yearMonth].source["pmcViews"].cumulativeHTML =
                  result.history[prevYearMonthStr].source["pmcViews"].cumulativeHTML;
              result.history[yearMonth].source["pmcViews"].cumulativeTotal =
                  result.history[prevYearMonthStr].source["pmcViews"].cumulativeTotal;
              result.history[yearMonth].source["pmcViews"].totalPDF = 0;
              result.history[yearMonth].source["pmcViews"].totalHTML = 0;
              result.history[yearMonth].source["pmcViews"].total = 0;
            } else {
              result.history[yearMonth].source["pmcViews"].cumulativePDF = 0;
              result.history[yearMonth].source["pmcViews"].cumulativeHTML = 0;
              result.history[yearMonth].source["pmcViews"].cumulativeTotal = 0;
              result.history[yearMonth].source["pmcViews"].totalPDF = 0;
              result.history[yearMonth].source["pmcViews"].totalHTML = 0;
              result.history[yearMonth].source["pmcViews"].total = 0;
            }
          }
        }
      }

      //If there are no PMC views at all, assume the data is just mising
      if (result.totalPMCTotal == 0) {
        result.totalPMCPDF = 0;
        result.totalPMCHTML = 0;
        result.totalPMCTotal = 0;
      }

      return result;
    };

    this.parseIntSafe = function (value) {
      if (isNaN(value)) {
        return 0;
      }

      return parseInt(value);
    };

    this.getYearMonth = function (year, month) {
      if (this.parseIntSafe(month) < 10) {
        return year + "-0" + month;
      } else {
        return year + "-" + month;
      }
    };

    /**
     * Set cross ref text by DOI
     * @param doi the doi
     * @param crossRefID the ID of the document element to place the result
     * @param almErrorID the ID of the document element to place the alm error
     * @parem loadingID the ID of the "loading" element to fade out after completion
     */
    this.setCrossRefText = function (doi, crossRefID, almErrorID, loadingID) {

      var almError = function (response) {
        var errorDiv = $("#" + almErrorID);
        errorDiv.html("Citations are currently not available, please check back later.");
        errorDiv.show("blind", 500);
        $("#" + loadingID).fadeOut('slow');
      };

      var success = function (response) {
        this.setCrossRefLinks(response, crossRefID);
        $("#" + loadingID).fadeOut('slow');
      };

      //The proxy function forces the success method to be run in "this" context.
      this.getCitesCrossRefOnly(doi, $.proxy(success, this), almError);
    };

    this.getCitesCrossRefOnly = function (doi, callBack, errorCallback) {
      doi = this.validateDOI(doi);

      var request = doi + "&source_id=crossref";
      this.getData(request, callBack, errorCallback);
    };

    this.setCrossRefLinks = function (response, crossRefID) {
      var doi = encodeURIComponent(response.data[0].doi);
      var crossRefResponse = this.filterSources(response.data[0].sources, ['crossref'])[0];
      var numCitations = 0;

      if (crossRefResponse.metrics.total > 0) {
        numCitations = crossRefResponse.metrics.total;
        var html = "";

        for (var eventIndex = 0; eventIndex < crossRefResponse.events.length; eventIndex++) {
          var citation = crossRefResponse.events[eventIndex].event;
          var citation_url = crossRefResponse.events[eventIndex].event_url;

          //  Assume there exists: URI, Title, and DOI.  Anything else may be missing.
          html = html + "<li><span class='article'><a href=\"" + citation_url + "\">"
              + citation.article_title + "</a> <span class=\"pubGetPDFLink\" "
              + "id=\"citation_" + this.fixDoiForID(citation.doi) + "\"></span></span>";

          if (citation.contributors) {
            var first_author = "";
            var authors = "";
            var author = "";
            var contributors = citation.contributors.contributor;

            for (var i = 0; i < contributors.length; i++) {
              individualContributor = contributors[i];
              if (individualContributor.first_author === 'true') {
                if (individualContributor.surname) {
                  first_author = individualContributor.surname;
                  if (individualContributor.given_name && individualContributor.given_name.length > 0) {
                    first_author = first_author + " " + individualContributor.given_name.substr(0, 1)
                  }
                }
              } else {
                author = "";
                if (individualContributor.surname) {
                  author = individualContributor.surname;
                  if (individualContributor.given_name && individualContributor.given_name.length > 0) {
                    author = author + " " + individualContributor.given_name.substr(0, 1);
                  }
                  authors = authors + ", " + author;
                }
              }
            }
            authors = first_author + authors;

            html = html + "<span class='authors'>" + authors + "</span>";
          }

          html = html + "<span class='articleinfo'>";
          if (citation.journal_title != null) {
            html = html + citation.journal_title;
          }
          if (citation.year != null) {
            html = html + " " + citation.year;
          }
          if (citation.volume != null) {
            html = html + " " + citation.volume;
          }
          if (citation.issue != null) {
            html = html + "(" + citation.issue + ")";
          }
          if (citation.first_page) {
            html = html + ": " + citation.first_page;
          }
          html = html + ". doi:" + citation.doi + "</span></li>";
        }
      }

      if (numCitations < 1) {
        html = "<h3>No related citations found</h3>";
      } else {
        var pluralization = "";
        if (numCitations != 1) { // This page should never be displayed if less than 1 citation.
          pluralization = "s";
        }

        var dateParts = response.data[0].issued['date-parts'][0];
        html = numCitations + " citation" + pluralization
            + " as recorded by <a href=\"http://www.crossref.org\">CrossRef</a>.  Article published "
            + moment(new Date(dateParts[0], dateParts[1], dateParts[2])).format("MMM DD, YYYY")
            + ". Citations updated on "
            + moment(new Date(response.data[0].update_date)).format("MMM DD, YYYY")
            + ". <ol>" + html + "</ol>";
      }

      $("#" + crossRefID).html(html);
      $("#" + crossRefID).show("blind", 500);
    };

    /**
     * HTML IDs can not have a "/" character in them.  Used to replace / w/ :
     * @param doi
     */
    this.fixDoiForID = function (doi) {
      return doi.replace(/\//g, ":");
    };


    /**
     *  host is the host and to get the JSON response from
     *  chartIndex is the  current index of the charts[] array
     *  callback is the method that populates the chart of  "chartIndex"
     *  errorCallback is the method that gets called when:
     *-The request fails (Network error, network timeout)
     *    --The request is "empty" (Server responds, but with nothing)
     *    --The callback method fails
     **/
    this.getData = function (request, callback, errorCallback) {
      var url = this.almHost + '?api_key=' + this.almAPIKey + '&ids=' + request + "&info=detail";

      //I use a third party plugin here for jsonp requests as jQuery doesn't
      //Handle errors well (with jsonp requests)
      $.jsonp({
        url: url,
        context: document.body,
        timeout: 20000,
        callbackParameter: "callback",
        success: callback,
        error: function (xOptions, msg) {
          errorCallback("Our system is having a bad day. We are working on it. Please check back later.")
        }
      });

      console.log(url);
    };

    /**
     * Sets the Saved text
     *
     * @param doi the doi
     * @param bookMarksID the ID of the element to contain the bookmarks text
     * @parem loadingID the ID of the "loading" element to fade out after completion
     */
    this.setSavedSuccess = function (response, bookMarksID, loadingID, registerVisualElementCallback, countElementShownCallback) {
      var bookMarksNode = $('#' + bookMarksID);
      $("#" + loadingID).fadeOut('slow');
      bookMarksNode.css("display", "none");

      //filter
      var sourceOrder = ['citeulike', 'connotea', 'mendeley'];
      var sources = this.filterSources(response[0].sources, sourceOrder);
      var sourceMap = {}, tooltip = '';

      // create the tiles and map each source to its corresponding tile
      var noTilesCreated = true;
      for (var index = 0; index < sources.length; index++) {
        var source = sources[index];

        if (source.metrics.total > 0) {
          noTilesCreated = false;
          source.display_name = source.display_name.replace(/\s/g, "");
          if (source.name == 'mendeley') {

            sourceMap[source.name] = this.createMetricsTile(source.display_name,
                source.events_url,
                RESOURCE_PATH + '/logo-' + source.name + '.png',
                source.metrics.total);

            var individuals = source.events.reader_count;
            var groups = source.events.group_count;

            tooltip = "<div class=\"tileTooltipContainer\" ><table class=\"tile_mini tileTooltip\" data-js-tooltip-hover=\"target\">" +
                "<thead><tr><th>Individuals</th><th>Groups</th></tr>" +
                "</thead><tbody><tr><td class=\"data1\">" + individuals.format(0, '.', ',') + "</td>" +
                "<td class=\"data2\">" + groups.format(0, '.', ',') + "</td></tr>" +
                "</tbody></table></div>";

          } else if (!source.events_url) {
            sourceMap[source.name] = this.createMetricsTileNoLink(source.display_name,
                RESOURCE_PATH + '/logo-' + source.name + '.png',
                source.metrics.total);

          } else {
            sourceMap[source.name] = this.createMetricsTile(source.display_name,
                source.events_url,
                RESOURCE_PATH + '/logo-' + source.name + '.png',
                source.metrics.total);

          }
        }
      } // end of loop

      // add the source tiles to the page html in the desired order
      for (var index = 0; index < sourceOrder.length; index++) {
        if (sourceOrder[index] in sourceMap) {
          bookMarksNode.append(sourceMap[sourceOrder[index]]);
        }
      }

      $('#MendeleyOnArticleMetricsTab')
          .attr("data-js-tooltip-hover", "trigger")
          .append(tooltip);
      tooltip_hover.init();

      //if no tiles created, do not display header and section
      if (noTilesCreated) {
        $('#socialNetworksOnArticleMetricsPage').css("display", "none");
      } else {
        registerVisualElementCallback('#' + bookMarksID);
        bookMarksNode.show("blind", 500, countElementShownCallback);
      }
    };

    this.setSavedError = function(message, bookMarksID, loadingID, registerVisualElementCallback, countElementShownCallback){
      $("#" + loadingID).fadeOut('slow');
      $("#" + bookMarksID).html("<img src='" + RESOURCE_PATH + "/icon_error.png'/>&nbsp;" + message);
      registerVisualElementCallback();
      $("#" + bookMarksID).show("blind", 500, countElementShownCallback);
    };

    this.createMetricsTile = function (name, url, imgSrc, linkText) {
      return '<div id="' + name + 'OnArticleMetricsTab" class="metrics_tile">' +
          '<a href="' + url + '"><img id="' + name + 'ImageOnArticleMetricsTab" src="' + imgSrc + '" alt="' + linkText + ' ' + name + '" class="metrics_tile_image"/></a>' +
          '<div class="metrics_tile_footer" onclick="location.href=\'' + url + '\';">' +
          '<a href="' + url + '">' + linkText + '</a></div></div>';
    };

    this.createMetricsTileNoLink = function (name, imgSrc, linkText) {
      return '<div id="' + name + 'OnArticleMetricsTab" class="metrics_tile_no_link">' +
          '<img id="' + name + 'ImageOnArticleMetricsTab" src="' + imgSrc + '" alt="' + linkText + ' ' + name + '" class="metrics_tile_image"/>' +
          '<div class="metrics_tile_footer_no_link">' +
          linkText + '</div></div>';
    };

    this.setDiscussedSuccess = function(response, discussedID, loadingID, registerVisualElementCallback, countElementShownCallback){
      $("#" + loadingID).fadeOut('slow');
      var discussedElement = $('#' + discussedID);
      discussedElement.css("display", "none");

      var doi = encodeURI(ARTICLE.citationDoi);
      var source = null, tooltip = "";

      // the order of tiles
      // research blogging, science seeker, nature blogs, wikipedia, wordpress
      // twitter, facebook, reddit, comments,  trackbacks

      var sourceOrder = ['researchblogging','scienceseeker', 'nature', 'wordpress', 'wikipedia', 'twitter', 'facebook', 'reddit'];
      // filter
      var sources = this.filterSources(response[0].sources, sourceOrder);

      var sourceMap = {};

      // create the tiles  and map the sources to their corresponding tiles
      for (var index = 0; index < sources.length; index++) {
        source = sources[index];

        if (source.metrics.total > 0) {
          // remove white spaces in display name
          source.display_name = source.display_name.replace(/\s/g, "");

          if (source.name === 'facebook') {
            //create tile & toggle noTilesCreated
            // facebook does not get a link
            sourceMap[source.name] =  this.createMetricsTileNoLink(source.display_name,
                RESOURCE_PATH + '/logo-' + source.name + '.png', source.metrics.total);

            //using these vars because source goes out of scope when tooltip handler is called
            var likes = source.events[0].like_count;
            var shares = source.events[0].share_count;
            var comments = source.events[0].comment_count;
            tooltip = "<div class=\"tileTooltip\"><table class=\"tile_mini\">" +
                "<thead><tr><th>Likes</th><th>Shares</th><th>Posts</th></tr>" +
                "</thead><tbody><tr>" +
                "<td class=\"data1\">" + likes.format(0, '.', ',') + "</td>" +
                "<td class=\"data2\">" + shares.format(0, '.', ',') + "</td>" +
                "<td class=\"data1\">" + comments.format(0, '.', ',') + "</td>" +
                "</tr>" +
                "</tbody></table></div>";
          } else if (source.name === 'twitter') {
            //use link to our own twitter landing page
            sourceMap[source.name] = this.createMetricsTile(source.display_name,
                '/article/twitter/info:doi/' + doi, RESOURCE_PATH + '/logo-' + source.name + '.png',
                source.metrics.total);

          } else {
            if (!source.events_url) {
              sourceMap[source.name] = this.createMetricsTileNoLink(source.display_name, RESOURCE_PATH + "/logo-" + source.name + '.png', source.metrics.total);
            } else {
              // logic for wikipedia,  we only want to escape the double quotes around the doi (the doi itself is already escaped)
              source.events_url = source.events_url.replace(/"/g, "%22");
              sourceMap[source.name] =  this.createMetricsTile(source.display_name, source.events_url, RESOURCE_PATH + "/logo-" + source.name + '.png', source.metrics.total);
            }
          }
        }
      } // end of for loop

      // add the source tiles to the page html in the desired order
      for (var index = 0; index < sourceOrder.length; index++) {
        if (sourceOrder[index] in sourceMap) {
          discussedElement.append(sourceMap[sourceOrder[index]]);
        }
      }
      $('#notesAndCommentsOnArticleMetricsTab').appendTo(discussedElement);
      $('#trackbackOnArticleMetricsTab').appendTo(discussedElement);

      $("#FacebookOnArticleMetricsTab").tooltip({
        delay: 250,
        fade: 250,
        track: true,
        showURL: false,
        bodyHandler: function () {
          return $(tooltip);
        }
      });

      registerVisualElementCallback('#' + discussedID);
      discussedElement.show('blind', 500, countElementShownCallback);
    };
    this.setDiscussedError = function (message, discussedID, loadingID, registerVisualElementCallback, countElementShownCallback) {

      var discussedElement = $('#' + discussedID);
      discussedElement.css('display', 'none');

      var html = '<img src="' + RESOURCE_PATH + '/icon_error.png"/>&nbsp;' + message;
      discussedElement.html(html);

      $("#" + loadingID).fadeOut('slow');
      registerVisualElementCallback();
      discussedElement.show('blind', 500, countElementShownCallback);

    };

    this.setCitesSuccess = function(response, citesID, loadingID, registerVisualElementCallback, countElementShownCallback){
      $("#" + loadingID).fadeOut('slow');
      $cities = $("#" + citesID);
      $cities.css("display", "none");

      var numCitesRendered = 0;
      var doi = encodeURI(ARTICLE.citationDoi);
      var html = "";

      // Citation Sources should always start with Scopus (if an entry for Scopus exists)
      // followed by the rest of the sources in alphabetical order.
      var sources = this.filterSources(response[0].sources, ["crossref", "pubmed", "scopus", "wos","pmceurope", "pmceuropedata", "datacite"]);
      var sourceOrder = ['scopus','crossref','pubmed','wos', 'pmceurope', 'pmceuropedata', 'datacite', 'google'];
      var sourceMap = {};

      // create the tiles and map the sources to their corresponding tiles
      for (var index = 0; index < sources.length; index++ ) {
        var source = sources[index];
        if (source.metrics.total > 0) {
          numCitesRendered++;
          var url = source.events_url;
          // remove white spaces in display name
          source.display_name = source.display_name.replace(/\s/g, "");
          // removing registered trademark symbol from web of science
          source.display_name = source.display_name.replace("\u00ae", "");

          //  If CrossRef, then compose a URL to our own CrossRef Citations page.
          if (source.name.toLowerCase() == 'crossref') {
            sourceMap[source.name] = this.createMetricsTile(source.display_name,
                "/article/crossref/info:doi/" + doi,
                RESOURCE_PATH + "/logo-" + source.name + ".png",
                source.metrics.total);
          } else if (source.events_url) {
            //  Only list links that HAVE DEFINED URLS
            sourceMap[source.name] =  this.createMetricsTile(source.display_name,
                url,
                RESOURCE_PATH + "/logo-" + source.name + ".png",
                source.metrics.total);
          } else {
            sourceMap[source.name] = this.createMetricsTileNoLink(source.display_name,
                RESOURCE_PATH + "/logo-" + source.name + ".png",
                source.metrics.total);
          }
        }
      } // end of loop
      var docURL = "http://dx.plos.org/" + doi.replace("info%3Adoi/", "");
      // add the source tiles to the page html in the desired order
      if (numCitesRendered != 0) {
        // Google Scholar tile is created if some other citation metrics is available
        sourceMap['google'] =  this.createMetricsTile("GoogleScholar",
            "http://scholar.google.com/scholar?hl=en&lr=&cites=" + docURL,
            RESOURCE_PATH + "/logo-google-scholar.png",
            "Search");

        for (var index = 0; index < sourceOrder.length; index++) {
          if (sourceOrder[index] in sourceMap) {
            html = html + sourceMap[sourceOrder[index]];
          }
        }
      } else {
        // Google Scholar link is displayed if no citation metric is available
        html = "No related citations found<br/>Search for citations in <a href=\"http://scholar.google.com/scholar?hl=en&lr=&cites=" + docURL + "\">Google Scholar</a>";
      }


      $cities.html(html);
      registerVisualElementCallback('#' + citesID);
      $cities.show("blind", 500, countElementShownCallback);

    };

    this.setCitesError = function(message, citesID, loadingID, registerVisualElementCallback, countElementShownCallback) {
      $("#" + loadingID).fadeOut('slow');
      $("#" + citesID).html("<img src='" + RESOURCE_PATH + "/icon_error.png'/>&nbsp;" + message);
      registerVisualElementCallback();
      $("#" + citesID).show("blind", 500, countElementShownCallback);
    };

    this.setF1000Success = function (response, f1kHeaderID, f1kSpinnerID, f1kContentID, registerVisualElementCallback, countElementShownCallback) {
      //add the goods then show the area which is by default hidden

      var f1k = this.filterSources(response[0].sources, ['f1000']).pop();

      //TODO - delete: this is here to prevent an exception as f1000 is not active and will be null
      if (!f1k || f1k.metrics.total == 0) {
        return;
      }

      $('#' + f1kHeaderID).show("blind", 500);

      registerVisualElementCallback('#' + f1kContentID);
      $("#" + f1kSpinnerID).fadeOut('slow');
      $('#' + f1kContentID).append(this.createMetricsTile(f1k.display_name,
          f1k.events_url,
          RESOURCE_PATH + '/logo-' + f1k.name + '.png',
          f1k.metrics.total)).show("blind", 500, countElementShownCallback);
    };

    this.setChartData = function (doi, usageID, loadingID, registerVisualElementCallback, countElementShownCallback, markChartShownCallback) {
      //citation_date format = 2006/12/20
      //citation_date format = 2006/2/2
      // 12/11/2015: Citation date format is not the same anymore. Format: 'Oct 13, 2003'

      var publishDate = moment(ARTICLE.citationDate, "MMM DD, YYYY").toDate();
      var publishDatems = publishDate.getTime();

      if (this.isNewArticle(publishDatems)) {
        //The article is less then 2 days old, and there is no data
        //give the user a good error message
        $("#" + usageID).html('This article was only recently published. ' +
            'Although we update our data on a daily basis (not in real time), there may be a 48-hour ' +
            'delay before the most recent numbers are available.<br/><br/>');
        registerVisualElementCallback();
        $("#" + usageID).show("blind", 500, countElementShownCallback);
        $("#" + loadingID).fadeOut('slow');
        markChartShownCallback();
      } else {
        if (this.isArticle(doi)) {
          var almError = function (message) {
            registerVisualElementCallback();
            $("#" + loadingID).fadeOut('slow');
            $("#" + usageID).html("<img src='" + RESOURCE_PATH + "/icon_error.png'/>&nbsp;" + message);
            $("#" + usageID).show("blind", 500, countElementShownCallback);
            markChartShownCallback();
          };

          var success = function (response) {
            var $usage = $("#" + usageID);
            $("#" + loadingID).fadeOut('slow');
            $usage.css("display", "none");

            var data = this.massageChartData(response.data[0].sources, publishDatems);

            var summaryTable = $('<div id="pageViewsSummary"><div id="left"><div class="header">Total Article Views</div>' +
                '<div class="totalCount">' + data.total.format(0, '.', ',') + '</div>' +
                '<div class="pubDates">' + moment(publishDate).format("MMM DD, YYYY") + ' (publication date)' +
                '<br>through ' + moment().format("MMM DD, YYYY") + '*</div></div><div id="right">' +
                '<table id="pageViewsTable"><tbody><tr><th></th><th nowrap="">HTML Page Views</th>' +
                '<th nowrap="">PDF Downloads</th><th nowrap="">XML Downloads</th><th>Totals</th></tr><tr>' +
                '<td class="source1">PLOS</td><td>' + data.totalCounterHTML.format(0, '.', ',') + '</td>' +
                '<td>' + data.totalCounterPDF.format(0, '.', ',') + '</td><td>' + data.totalCounterXML.format(0, '.', ',') + '</td>' +
                '<td class="total">' + data.totalCouterTotal.format(0, '.', ',') + '</td></tr><tr><td class="source2">PMC</td>' +
                '<td>' + data.totalPMCHTML.format(0, '.', ',') + '</td><td>' + data.totalPMCPDF.format(0, '.', ',') + '</td>' +
                '<td>n.a.</td><td class="total">' + data.totalPMCTotal.format(0, '.', ',') + '</td></tr><tr><td>Totals</td>' +
                '<td class="total">' + data.totalHTML.format(0, '.', ',') + '</td><td ' +
                'class="total">' + data.totalPDF.format(0, '.', ',') + '</td><td class="total">' + data.totalXML.format(0, '.', ',') +
                '</td><td class="total">' + data.total.format(0, '.', ',') + '</td></tr>' +
                '<tr class="percent"><td colspan="5"><b>' + ((data.totalPDF / data.totalHTML) * 100).format(2, '.', ',') +
                '%</b> of article views led to PDF downloads</td></tr></tbody></table></div></div>');

            var dataHistoryKeys = Object.keys(data.history);
            $usage.append(summaryTable);

            // Display the graph only if there are at least two data points (months)
            var isGraphDisplayed = Object.keys(data.history).length > 1;
            if (isGraphDisplayed) {

              var options = this.buildChartOptions(data, dataHistoryKeys);

              for (var key in data.history) {
                if (data.history[key].source.pmcViews != null) {
                  options.series[0].data.push({ name: key, y: data.history[key].source.pmcViews.cumulativeTotal });
                } else {
                  options.series[0].data.push({ name: key, y: 0 });
                }
                options.series[1].data.push({ name: key, y: data.history[key].source.counterViews.cumulativeTotal });
              }

              $usage.append($('<div id="chart"></div>')
                  .css("width", "600px")
                  .css("height", "200px"));

              //chart is redrawn once upon creation, so need to count this to synch display rendering
              registerVisualElementCallback();
              var chart = new Highcharts.Chart(options);

              this.addRelativeMetricInfo(data, dataHistoryKeys, chart, $usage, registerVisualElementCallback);

            } // end if (isGraphDisplayed)

            $usage.append($('<p>*Although we update our data on a daily basis, there may be a 48-hour delay before the most recent numbers are available. PMC data is posted on a monthly basis and will be made available once received.</p>'));

            this.addFigshareTile(response.data[0]);

            registerVisualElementCallback();
            $usage.show("blind", 500, function () {
              countElementShownCallback();
              markChartShownCallback();
            });

          };

          doi = this.validateDOI(doi);
          var request = doi + '&source=pmc,counter,relativemetric,figshare';
          this.getData(request, $.proxy(success, this), almError);
        }
      }
    };

    this.buildChartOptions = function(data, dataHistoryKeys) {
      var options = {
        chart: {
          renderTo: "chart",
          animation: false,
          margin: [40, 40, 40, 80]
        },
        credits: {
          enabled: false
        },
        exporting: {
          enabled: false
        },
        title: {
          text: null
        },
        legend: {
          enabled: false
        },
        xAxis: {
          title: {
            text: "Months",
            style: {
              fontFamily: "'FS Albert Web Regular', Verdana, sans-serif",
              fontWeight: "normal",
              color: "#000"
            },
            align: "high"
          },
          labels: {
            step: (dataHistoryKeys.length < 15) ? 1 : Math.round(dataHistoryKeys.length / 15),
            formatter: function () {
              return this.value + 1;
            }
          },
          categories: []
        },
        yAxis: [
          {
            title: {
              text: "Cumulative Views",
              style: {
                fontFamily: "'FS Albert Web Regular', Verdana, sans-serif",
                fontWeight: "normal",
                color: "#000",
                height: "50px"
              }
            },
            labels: {
              style: {
                color: "#000"
              }
            }
          }
        ],
        plotOptions: {
          column: {
            stacking: "normal"
          },
          animation: false,
          series: {
            pointPadding: 0,
            groupPadding: 0,
            borderWidth: 0,
            shadow: false
          }
        },
        series: [
          {
            name: "PMC",
            type: "column",
            data: [],
            color: "#6d84bf"
          },
          {
            name: "PLOS",
            type: "column",
            data: [],
            color: "#3c63af"
          }
        ],
        tooltip: {
          //Make background invisible
          backgroundColor: "rgba(255, 255, 255, 0.0)",
          useHTML: true,
          shared: true,
          shadow: false,
          borderWidth: 0,
          borderRadius: 0,
          positioner: function (labelHeight, labelWidth, point) {
            var newX = point.plotX + (labelWidth / 2) + 25,
                newY = point.plotY - (labelHeight / 2) + 25;
            return { x: newX, y: newY };
          },
          formatter: function () {
            var key = this.points[0].key,
                h = data.history,
                formattedDate = moment(new Date(h[key].year, h[key].month - 1, 2)).format('MMMM YYYY');

            return '<table id="mini" cellpadding="0" cellspacing="0">'
                + '<tr><th></td><td colspan="2">Views in ' + formattedDate
                + '</td><td colspan="2">Views through ' + formattedDate
                + '</td></tr><tr><th>Source</th><th class="header1">PLOS</th><th class="header2">PMC</th>'
                + '<th class="header1">PLOS</th><th class="header2">PMC</th></tr>'
                + '<tr><td>HTML</td><td class="data1">' + h[key].source.counterViews.totalHTML + '</td>'
                + '<td class="data2">' + (h[key].source.hasOwnProperty("pmcViews") ?
                    h[key].source.pmcViews.totalHTML.format(0, '.', ',') : "n.a.") + '</td>'
                + '<td class="data1">' + h[key].source.counterViews.cumulativeHTML.format(0, '.', ',') + '</td>'
                + '<td class="data2">' + (h[key].source.hasOwnProperty("pmcViews") ?
                    h[key].source.pmcViews.cumulativeHTML.format(0, '.', ',') : "n.a.") + '</td></tr>'
                + '<tr><td>PDF</td><td class="data1">' + h[key].source.counterViews.totalPDF + '</td>'
                + '<td class="data2">' + (h[key].source.hasOwnProperty("pmcViews") ?
                    h[key].source.pmcViews.totalPDF.format(0, '.', ',') : "n.a.") + '</td>'
                + '<td class="data1">' + h[key].source.counterViews.cumulativePDF.format(0, '.', ',') + '</td>'
                + '<td class="data2">' + (h[key].source.hasOwnProperty("pmcViews") ?
                    h[key].source.pmcViews.cumulativePDF.format(0, '.', ',') : "n.a.") + '</td></tr>'
                + '<tr><td>XML</td><td class="data1">' + h[key].source.counterViews.totalXML + '</td>'
                + '<td class="data2">n.a.</td>'
                + '<td class="data1">' + h[key].source.counterViews.cumulativeXML.format(0, '.', ',') + '</td>'
                + '<td class="data2">n.a.</td></tr>'
                + '<tr><td>Total</td><td class="data1">' + h[key].source.counterViews.total + '</td>'
                + '<td class="data2">' + (h[key].source.hasOwnProperty("pmcViews") ?
                    h[key].source.pmcViews.total.format(0, '.', ',') : "n.a.") + '</td>'
                + '<td class="data1">' + h[key].source.counterViews.cumulativeTotal.format(0, '.', ',') + '</td>'
                + '<td class="data2">' + (h[key].source.hasOwnProperty("pmcViews") ?
                    h[key].source.pmcViews.cumulativeTotal.format(0, '.', ',') : "n.a.") + '</td></tr>'
                + '</table>';
          }
        }
      };

      return options;
    };

    this.addRelativeMetricInfo = function(data, dataHistoryKeys, chart, usage, registerVisualElementCallback) {

      // check to see if there is any data
      if (data.relativeMetricData != null) {
        var subjectAreas = data.relativeMetricData.subject_areas;
        if (subjectAreas && subjectAreas.length > 0) {
          var subjectAreaList = [];

          // loop through each subject area and add the data to the chart
          for (var i = 0; i < subjectAreas.length; i++) {
            var subjectAreaId = subjectAreas[i].subject_area;
            var subjectAreaData = subjectAreas[i].average_usage;

            // product wants the graph to display if and only if it is a line (not a dot)
            if (subjectAreaData.length >= 2) {
              subjectAreaList.push(subjectAreaId);

              // make sure the data will fit the graph
              if (subjectAreaData.length > dataHistoryKeys.length) {
                subjectAreaData = subjectAreaData.slice(0, dataHistoryKeys.length);
              }

              // add the data for the given subject area to the chart
              registerVisualElementCallback();
              chart.addSeries({
                    id: subjectAreaId,
                    data: subjectAreaData,
                    type: "line",
                    color: "#01DF01",
                    marker: {
                      enabled: false,
                      states: {
                        hover: {
                          enabled: false
                        }
                      }
                    }
                  }
              );

              // hide the line
              registerVisualElementCallback();
              chart.get(subjectAreaId).hide();
            }
          }

          // make sure we have subject areas to add to the select control
          if (subjectAreaList.length > 0) {
            // build the drop down list of subject areas
            var defaultSubjectAreaSelected;
            var subjectAreasDropdown = $('<select id="subject_areas"></select>');
            // sort the list so that the subject areas are grouped correctly
            subjectAreaList.sort();
            for (i = 0; i < subjectAreaList.length; i++) {
              var subjectArea = subjectAreaList[i].substr(1);
              var subjectAreaLevels = subjectArea.split("/");

              if (subjectAreaLevels.length == 1) {
                // add the first level subject area
                subjectAreasDropdown.append($('<option></option>').attr('value', subjectAreaList[i]).text(subjectAreaLevels[0]));
              } else if (subjectAreaLevels.length == 2) {
                // add the second level subject area
                subjectAreasDropdown.append($('<option></option>').attr('value', subjectAreaList[i]).html("&nbsp;&nbsp;&nbsp;" + subjectAreaLevels[1]));

                if (defaultSubjectAreaSelected == null) {
                  defaultSubjectAreaSelected = subjectAreaList[i];
                }
              }
            }

            // if there wasn't a second level subject area to pick, pick the first first level subject area
            if (defaultSubjectAreaSelected == null) {
              defaultSubjectAreaSelected = subjectAreaList[0];
            }

            // select the subject area that should be selected when the page loads
            subjectAreasDropdown.find('option[value="' + defaultSubjectAreaSelected + '"]').attr("selected", "selected")
            // display the line in the chart for the selected subject area
            chart.get(defaultSubjectAreaSelected).show();

            // when a subject area is selected, display the correct data (line)
            subjectAreasDropdown.change(function () {

              $("#subject_areas option").each(function () {
                chart.get($(this).val()).hide();
              });

              chart.get($(this).val()).show();
              var linkToRefset = $('input[name="refsetLinkValue"]').val();
              $('#linkToRefset').attr("href", linkToRefset.replace("SUBJECT_AREA", $(this).val()))

            });

            // build the output
            var descriptionDiv = $('<div></div>').html('<span class="colorbox"></span>&nbsp;Compare average usage for articles published in <b>'
                + new Date(data.relativeMetricData.start_date).getUTCFullYear() + "</b> in the subject area: "
                + '<a href="/static/almInfo#relativeMetrics" class="ir" title="More information">info</a>');

            // build the link to the search result reference set
            var linkToRefset = "/search/advanced?pageSize=12&unformattedQuery=(publication_date:[" + data.relativeMetricData.start_date + " TO " + data.relativeMetricData.end_date + "]) AND subject:\"SUBJECT_AREA\"";

            var description2Div = $('<div></div>').append(subjectAreasDropdown)
                .append('&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<a id="linkToRefset" href="' + encodeURI(linkToRefset.replace("SUBJECT_AREA", defaultSubjectAreaSelected)) + '" >Show reference set</a>')
                .append('<input type="hidden" name="refsetLinkValue" value="' + encodeURI(linkToRefset) + '" >');

            var relativeMetricDiv = $('<div id="averageViewsSummary"></div>').append(descriptionDiv).append(description2Div);

            usage.append(relativeMetricDiv);
          }
        }
      }
    }

    this.addFigshareTile = function(response) {

      var source;

      for (var i = 0; i < response.sources.length; i++) {
        source = response.sources[i];
        if (source.name.toLowerCase() == "figshare") {
          if (source.metrics.total <= 0) {
            return;
          }

          var tileName = source.name;
          var tile = this.createMetricsTileNoLink(tileName,
              RESOURCE_PATH + "/logo-" + tileName + ".png",
              source.metrics.total);

          $('#views').append(tile);

          var popup = $('<div id="dropdown-figshare" class="metric-dropdown" data-dropdown-content></div>'), dialogTable = $('<table class=\"tile_mini\"></table>'),
              item, totalStat, key;

          // build tooltip
          for (i = 0; i < source.events.length; i++) {
            item = source.events[i], totalStat = 0, key = "";
            if (typeof item.doi !== 'undefined' && item.doi.length > 0) {
              // if the doi ends in (.s\d+), it refers to SIs
              var pattern = /\.s\d+$/g;
              if (item.doi.length == 1 && !pattern.test(item.doi[0])) {
                key = item.doi[0].replace("http://dx.doi.org/", "");
              } else {
                key = "SI";
              }
            }

            var itemInfo = {};
            totalStat = item.stats.downloads + item.stats.page_views;
            itemInfo.stat = "<td class=\"data1\">" + totalStat + "</td>";
            itemInfo.link =  item.figshare_url;
            itemInfo.title = item.files[0];

            if (itemInfo) {
              var link = "<a href=\"" + itemInfo.link + "\" target=_blank>" + itemInfo.title + "</a>";
              dialogTable.append("<tr><td>" + link + "</td>" + itemInfo.stat + "</tr>");
            }

            if (key === "SI") {
              var link = "<a href=\"" + itemInfo.link + "\" target=_blank>  Supporting Info Files </a>";
              dialogTable.append("<tr><td>" + link + "</td>" + itemInfo.stat + "</tr>");
            }
          }
          break;
        }
      }

      popup.append(dialogTable);

      $('#figshareImageOnArticleMetricsTab')
          .attr('data-options', 'align:right')
          .attr('data-dropdown', 'dropdown-figshare')
          .after(popup);

      $(document).foundation('dropdown', 'reflow');
    };

    this.setMetricsTab = function (doi, registerVisualElementCallback, countElementShownCallBack, allTilesRegisteredCallBack) {
      doi = this.validateDOI(doi);

      //succeed!
      var success = function(response){
        this.setCitesSuccess(response.data, "relatedCites", "relatedCitesSpinner", registerVisualElementCallback, countElementShownCallBack);
        this.setSavedSuccess(response.data, "relatedBookmarks", "relatedBookmarksSpinner", registerVisualElementCallback, countElementShownCallBack);
        this.setDiscussedSuccess(response.data, "relatedBlogPosts", "relatedBlogPostsSpinner", registerVisualElementCallback, countElementShownCallBack);
        this.setF1000Success(response.data, "f1kHeader","f1KSpinner","f1kContent", registerVisualElementCallback, countElementShownCallBack);
        allTilesRegisteredCallBack();
      };

      //fail!
      var fail = function(message){
        this.setCitesError(message, "relatedCites", "relatedCitesSpinner", registerVisualElementCallback, countElementShownCallBack);
        this.setSavedError(message, "relatedBookmarks", "relatedBookmarksSpinner", registerVisualElementCallback, countElementShownCallBack);
        this.setDiscussedError(message, "relatedBlogPosts", "relatedBlogPostsSpinner", registerVisualElementCallback, countElementShownCallBack);
        //F1000 Prime section is by default hidden, so no need to keep track of any visual rendering
        // and also because it is by default hidden, no need to do a thing
        allTilesRegisteredCallBack();
      };

      this.getData(doi, $.proxy(success, this), $.proxy(fail, this));
    };

    this.filterSources = function(sources, validNames) {
      var validSources = [];

      for (var i = 0; i < sources.length; i++) {
        if ($.inArray(sources[i].name.toLowerCase(), validNames) > -1) {
          validSources.push(sources[i]);
        }
      }

      return validSources;
    };

    this.enforceOrder = function(sources, orderArray) {
      var sourceNames = [];
      for (var n = 0; n < sources.length; n++) {
        sourceNames.push(sources[n].name);
      }

      var orderedSources = [];
      for (var d = 0; d < orderArray.length; d++) {
        var index = $.inArray(orderArray[d], sourceNames);
        if (index > -1) {
          orderedSources.push(sources[index]);
        }
      }
      return orderedSources;
    }
  };

  function onReadyALM() {
      var almService = new $.fn.alm(),
          doi = ARTICLE.citationDoi;

      var almError = function () {};
      var almSuccess = function (response) {
        /*
          Previously here was the signposts building code
          after the removal of that code, the Media Coverage Link code
          got left behind since they were entangled.
        */

        var responseObject, sources;

        if (response && response.length > 0) {
          responseObject = response[0].data[0];

          //distinguish sources
          var articleCoverageCurated;
          sources = responseObject.sources;

          for(var i = 0; i < sources.length; i += 1){
            if (sources[i].name.toLowerCase() == 'articlecoveragecurated') {
              articleCoverageCurated = sources[i];
            }
          }

          // this logic is NOT part of the almSignposts logic.
          // this logic adds "Media Coverage" link on the left hand side article nav
          // only on initial article page load
          if (articleCoverageCurated.metrics.total > 0) {
            buildMediaCoverageLink(articleCoverageCurated.metrics.total);
          }
        }
      };

      almService.getArticleSummaries([ doi ], almSuccess, almError);
  }

  function jumpToALMSection(){
    //if url contains a reference to an alm section, jump there
    var url = $(location).attr('href');
    var hashIndex = url.indexOf('#');
    if (hashIndex === -1) {
      return;
    }

    var almSectionID = url.slice(hashIndex);
    scrollTo(0, $(almSectionID).position().top);
  }

  $(document).ready(onReadyALM);
  $(document).ready(onLoadALM);

  function onLoadALM() {
    var almService = new $.fn.alm();
    var doi = ARTICLE.citationDoi;

    var elementsRegisteredCount = 0;
    var elementShownCount = 0;
    var metricsComplete = false;
    var chartComplete = false;

    var allSectionsDisplayed = function(){
      return metricsComplete && chartComplete && elementsRegisteredCount === elementShownCount;
    };

    var registerVisualElement = function(){
      elementsRegisteredCount++;
    };

    var countElementShown = function(){
      elementShownCount++;
      if (allSectionsDisplayed()) {
        jumpToALMSection();
      }
    };

    var markTilesShown = function(){
      metricsComplete = true;
      if (allSectionsDisplayed()) {
        jumpToALMSection();
      }
    };

    var markChartShown = function(){
      chartComplete = true;
      if (allSectionsDisplayed()) {
        jumpToALMSection();
      }
    };

    almService.setMetricsTab(doi, registerVisualElement, countElementShown, markTilesShown);
    almService.setChartData(doi, "usage", "chartSpinner", registerVisualElement, countElementShown, markChartShown);
  }

  /* Some common display functions for the browse and search results pages */

  function setALMSearchWidgets(articles) {
    articles = articles[0].data;
    for (a = 0; a < articles.length; a++) {
      var article = articles[a];
      var doi = article.doi;
      var sources = article.sources;
      var scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter;
      scopus = citeulike = counter = mendeley = crossref = wos = pmc = pubmed = facebook = twitter = null;

      //get references to specific sources
      var sourceNames = [];
      for (var s = 0; s < sources.length; s++) {
        sourceNames.push(sources[s].name);
      }
      scopus = sources[sourceNames.indexOf('scopus')];
      citeulike = sources[sourceNames.indexOf('citeulike')];
      pubmed = sources[sourceNames.indexOf('pubmed')];
      counter = sources[sourceNames.indexOf('counter')];
      mendeley = sources[sourceNames.indexOf('mendeley')];
      crossref = sources[sourceNames.indexOf('crossref')];
      wos = sources[sourceNames.indexOf('wos')];
      pmc = sources[sourceNames.indexOf('pmc')];
      facebook = sources[sourceNames.indexOf('facebook')];
      twitter = sources[sourceNames.indexOf('twitter')];

      //determine if article cited, bookmarked, or socialised, or even seen
      var hasData = false;
      if (scopus.metrics.total > 0 ||
          citeulike.metrics.total > 0 ||
          pmc.metrics.total + counter.metrics.total > 0 ||
          mendeley.metrics.total > 0 ||
          facebook.metrics.total + twitter.metrics.total > 0) {
        hasData = true;
      }

      //show widgets only when you have data
      if (hasData) {
        confirmed_ids[confirmed_ids.length] = doi;
        makeALMSearchWidget(doi, scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter);
      }
    }
    confirmALMDataDisplayed();
  }

  function makeALMSearchWidget(doi, scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter) {
    var nodeList = getSearchWidgetByDOI(doi);
    var metricsURL = getMetricsURL(doi);

    var anim = $(nodeList).fadeOut(250, function() {
      var searchWidget = $("<span></span>");
      searchWidget.addClass("almSearchWidget");

      buildWidgetText(searchWidget, metricsURL, scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter);

      $(nodeList).html("");
      $(nodeList).append(searchWidget);
      $(nodeList).fadeIn(250);
    });
  }

//TODO: messy but correct - clean up
  function buildWidgetText(node, metricsURL, scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter) {
    var newNode = null;

    var total = pmc.metrics.total + counter.metrics.total;
    var totalHTML = pmc.metrics.html + counter.metrics.html;
    var totalPDF = pmc.metrics.pdf + counter.metrics.pdf;
    //alm response json has no metric for xml, but xml = total - pdf - html
    var totalXML = total - totalPDF - pmc.metrics.html - counter.metrics.html;
    if (total > 0) {
      newNode = $("<a></a>")
          .attr("href", metricsURL + "#usage")
          .html("Views: " + total.format(0, '.', ','))
          .addClass("data");

      newNode.tooltip({
        delay: 250,
        fade: 250,
        top: -40,
        left: 20,
        track: true,
        showURL: false,
        bodyHandler: function () {
          return "<span class=\"searchResultsTip\">HTML: <b>" + totalHTML.format(0, '.', ',') + "</b>"
              + ", PDF: <b>" + totalPDF.format(0, '.', ',') + "</b>"
              + ", XML: <b>" + totalXML.format(0, '.', ',') + "</b>"
              + ", Grand Total: <b>" + total.format(0, '.', ',') + "</b></span>";
        }
      });


      node.append($("<span></span>").append(newNode));
    } else {
      node.appendChild($("<span></span>")
          .addClass("no-data")
          .html("Views: Not available"));
    }

    //using scopus for display
    if (scopus.metrics.total > 0) {
      newNode = $("<a></a>")
          .attr("href", metricsURL + "#citations")
          .html("Citations: " + scopus.metrics.total.format(0, '.', ','))
          .addClass("data");

      newNode.tooltip({
        delay: 250,
        fade: 250,
        top: -40,
        left: 20,
        track: true,
        showURL: false,

        bodyHandler: function () {
          //adding citation sources manually and IN ALPHABETIC ORDER
          //if this is generified, remember to sort, and remember the comma
          var someSources = [crossref, pubmed, wos];
          var tipText = scopus.display_name + ": <b>" + scopus.metrics.total.format(0, '.', ',') + "</b>"; //scopus.metrics.total always > 0

          for (var s = 0; s < someSources.length; s++) {
            var source = someSources[s];
            if (source.metrics.total > 0) {
              tipText += ', ' + source.display_name + ": <b>" + source.metrics.total.format(0, '.', ',') + "</b>";
            }
          }

          return "<span class=\"searchResultsTip\">" + tipText + "</span>";
        }
      });

      //new dijit.Tooltip({ connectId: newNode, label: tipText });
      appendBullIfNeeded(node);
      node.append($("<span></span>").append(newNode));
    } else {
      appendBullIfNeeded(node);
      node.append($("<span></span>")
          .html("Citations: None")
          .addClass("no-data"));
    }

    var markCount = mendeley.metrics.total + citeulike.metrics.total;
    if (markCount > 0) {
      newNode = $("<a></a>")
          .attr("href", metricsURL + "#other")
          .html("Saves: " + markCount.format(0, '.', ','))
          .addClass("data");

      appendBullIfNeeded(node);

      newNode.tooltip({
        delay: 250,
        fade: 250,
        top: -40,
        left: 20,
        track: true,
        showURL: false,
        bodyHandler: function () {
          var tipText = "";

          if (mendeley.metrics.total > 0) {
            tipText += mendeley.display_name + ": <b>" + mendeley.metrics.total.format(0, '.', ',') + "</b>";
          }

          if (citeulike.metrics.total > 0) {
            if (tipText != "") {
              tipText += ", "
            }
            tipText += citeulike.display_name + ": <b>" + citeulike.metrics.total.format(0, '.', ',') + "</b>";
          }


          return "<span class=\"searchResultsTip\">" + tipText + "</span>";
        }
      });

      node.append($("<span></span>").append(newNode));
    } else {
      appendBullIfNeeded(node);
      node.append($("<span></span>")
          .html("Saves: None")
          .addClass("no-data"));
    }

    var shareCount = facebook.metrics.shares + twitter.metrics.total;
    if (shareCount > 0) {
      newNode = $("<a></a>")
          .attr("href", metricsURL + "#other")
          .html("Shares: " + shareCount)
          .addClass("data");

      appendBullIfNeeded(node);

      newNode.tooltip({
        delay: 250,
        fade: 250,
        top: -40,
        left: 20,
        track: true,
        showURL: false,
        bodyHandler: function () {
          var tipText = "";

          if (facebook.metrics.shares > 0) {
            tipText += facebook.display_name + ": <b>" + facebook.metrics.shares.format(0, '.', ',') + "</b>";
          }

          if (twitter.metrics.total > 0) {
            if (tipText != "") {
              tipText += ", "
            }
            tipText += twitter.display_name + ": <b>" + twitter.metrics.total.format(0, '.', ',') + "</b>";
          }

          return "<span class=\"searchResultsTip\">" + tipText + "</span>";
        }
      });

      node.append($("<span></span>").append(newNode));
    } else {
      appendBullIfNeeded(node);
      node.append($("<span></span>")
          .html("Shares: None")
          .addClass("no-data"));
    }
  }

  function appendBullIfNeeded(node) {
    if (node.length > 0) {
      node.append("&nbsp;&bull;&nbsp;");
    }
  }

  function getSearchWidgetByDOI(doi) {
    return $("li[data-doi='" + doi  + "'] span.metrics");
  }

  function getMetricsURL(doi){
    return $($("li[data-doi='" + doi  + "']")[0]).data("metricsurl");
  }

  function setALMSearchWidgetsError() {
    confirmALMDataDisplayed();
  }

  function makeALMSearchWidgetError(doi, message) {
    var nodeList = getSearchWidgetByDOI(doi);
    var spanNode = nodeList[0];

    var errorMsg = $("<span></span>");
    errorMsg.addClass("inlineError");
    errorMsg.css("display","none");
    errorMsg.html(message);

    $(spanNode).find("span").fadeOut(250, function() {
      $(spanNode).append(errorMsg);
      $(errorMsg).fadeIn(250);
    });
  }

  /*
   * Walk through the ids and confirmed_ids list.  If
   * If some ids are not confirmed.  Lets let the
   * front end know that no data was received.
   * */
  function confirmALMDataDisplayed() {
    if (confirmed_ids != null) {
      for(var a = 0; a < confirmed_ids.length; a++) {
        for(var b = 0; b < ids.length; b++) {
          if (confirmed_ids[a] == ids[b]) {
            ids.remove(b);
          }
        }
      }
    }

    //if any ids are left.  We know there is no data
    //Make note of that now.
    for(a = 0; a < ids.length; a++) {
      var nodeList = $("li[data-doi='" + ids[a] + "']");
      var pubDate = $(nodeList[0]).data("pdate");

      //If the article is less then two days old and there is no data,
      //it's not really an error, alm is a few days behind
      if (pubDate > ((new Date().getTime()) -  172800000)) {
        makeALMSearchWidgetError(ids[a],
            "Metrics unavailable for recently published articles. Please check back later.");
      } else {
        makeALMSearchWidgetError(ids[a],
            "<img src='" + RESOURCE_PATH + "/icon_error.png'/>&nbsp;Metrics unavailable. Please check back later.");
      }
    }
  }

  function buildMediaCoverageLink(coverageTotal) {
    var mediaCoverageLink = $("<a></a>")
        .attr("href", "/article/related/info:doi/" + ARTICLE.citationDoi)
        .text("Media Coverage (" + coverageTotal + ")");

    // the media coverage link should be above the "Figures" link
    // if "Figures" link doesn't exist, add it to the bottom of the list
    if ($("#nav-article-page #nav-figures").length > 0) {
      $("#nav-article-page #nav-figures").before($("<li></li>").append(mediaCoverageLink));
    } else {
      $("#nav-article-page ul:nth-child(2)").append($("<li></li>").append(mediaCoverageLink));
    }
  }

  /**
   * Adds the media coverage link when a user clicks on the article tab on the article page
   */
  function addMediaCoverageLink() {
    var almService = new $.fn.alm();

    var almSuccess = function (response) {
      var data, sources, source;

      if (response && response.length > 0) {
        data = response[0];
        sources = data.sources;

        for(var i = 0; i < sources.length; i++){
          source = sources[i];

          if (source.name.toLowerCase() == 'articlecoveragecurated') {
            if (source.metrics.total > 0) {
              buildMediaCoverageLink(source.metrics.total);
            }
            break;
          }
        }
      }
    };

    // do nothing
    var almError = function() {};

    almService.getArticleSummaries([ ARTICLE.citationDoi ], almSuccess, almError);
  }

})(jQuery);

//Stolen from:
//http://stackoverflow.com/questions/149055/how-can-i-format-numbers-as-money-in-javascript
Number.prototype.format = function (c, d, t) {
  var n = this, c = isNaN(c = Math.abs(c)) ? 2 : c, d = d == undefined ? "," : d, t = t == undefined ? "." :
      t, s = n < 0 ? "-" : "", i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
  return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d +
      Math.abs(n - i).toFixed(c).slice(2) : "");
};