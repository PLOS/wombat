var MetricsViewedSection;

(function ($) {
  MetricsViewedSection = MetricsTabComponent.extend({
    $element: $('#views'),
    $loadingEl: $('#chartSpinner'),
    $headerEl: $('#viewedHeader'),
    $chartElement: $('#usage'),
    sourceOrder: ['figshare'],
    chartData:[],
    highchartPlosSeriesData: [],
    highchartPMCSeriesData: [],

    loadData: function (data) {
      this._super(data);
      this.$chartElement.hide();

      this.filterChartData();
      this.createSummaryTable();
      this.createHighchart();
      this.$chartElement.append($('<p>*Although we update our data on a daily basis, there may be a 48-hour delay before the most recent numbers are available. PMC data is posted on a monthly basis and will be made available once received.</p>'));
      this.createTiles();

      this.afterLoadData();
    },

    afterLoadData: function () {
      this.showComponent();
      this.$chartElement.show();
    },

    dataError: function () {
      //In case of error we show the text for data no available
      var template = _.template($('#viewedSectionNoDataTemplate').html());
      var templateHtml = template();

      this.$element.append(templateHtml).show();
      this.$loadingEl.hide();
    },

    newArticleError: function () {
      //In case of error we show the text for new article error
      var template = _.template($('#viewedSectionNewArticleErrorTemplate').html());
      var templateHtml = template();

      this.$element.append(templateHtml).show();
      this.$loadingEl.hide();
    },

    //Join year and month to a string, used as key to the history object and as ordering factor for the events
    getYearMonthString: function (month, year, separator) {
      if(!separator) {
        separator = '';
      }
      //if month less then 10, append a 0 before it
      if(month < 10) {
        month = "0" + month.toString();
      }
      else {
        month = month.toString();
      }

      return year.toString()+separator+month;
    },

    filterEvents: function (sourceName, pubYearMonth) {
      var that = this;
      var events = _.filter(this.data.sources, function (source) { return source.name.toLowerCase() == sourceName});

      if(events[0] && events[0].events && events[0].events.length > 0) {
        //Filter only events after the publication date
        events = _.filter(events[0].events, function (event) {
          return parseInt(that.getYearMonthString(event.month, event.year)) >= pubYearMonth;
        });
        //Order the events by date
        events = _.sortBy(events, function (event) {
          return parseInt(that.getYearMonthString(event.month, event.year));
        });
      }
      else {
        events = [];
      }

      return events;
    },

    //Create the default data to all the months in the history, between the publication year and today. This way if is missing some month in ALM, the chart is rendered right.
    createHistoryDataStructure: function (pubYear, pubMonth) {
      var todayDate = new Date();
      var todayYear = todayDate.getFullYear();
      var todayMonth = todayDate.getMonth()+1;
      var history = {};

      //We do a loop through all the years between the publication year and today year
      for(var year=pubYear; year <= todayYear; year++) {
        //If current year is equal to pubYear and todayYear we limit the initial month to the pubMonth and limit the end to the todayMonth
        if(year == pubYear && year == todayYear) {
          for(var month = pubMonth; month <= todayMonth; month++) {
            history[this.getYearMonthString(month, year, '-')] = this.createHistoryMonthStructure(month, year);
          }
        }
        //If current year is equal to pubYear and different todayYear we limit the initial month to the pubMonth and limit the end to 12
        else if (year == pubYear && year != todayYear) {
          for(var month = pubMonth; month <= 12; month++) {
            history[this.getYearMonthString(month, year, '-')] = this.createHistoryMonthStructure(month, year);
          }
        }
        //If current year is equal to todayYear and different pubYear we limit the initial month to 1 and limit the end to todayMonth
        else if(year != pubYear && year == todayYear) {
          for(var month = 1; month <= todayMonth; month++) {
            history[this.getYearMonthString(month, year, '-')] = this.createHistoryMonthStructure(month, year);
          }
        }
        //If the current year is not equal to todayMonth or pubYear we limit to the calendar (1-12)
        else {
          for(var month = 1; month <= 12; month++) {
            history[this.getYearMonthString(month, year, '-')] = this.createHistoryMonthStructure(month, year);
          }
        }
      }

      return history;
    },

    //Creates the default structure for a month in the history, this is populated by populateHistoryData
    createHistoryMonthStructure: function (month, year) {
      return {
        source: {
          counterViews: {
            month: month,
            year: year,
            totalPDF: 0,
            totalXML: 0,
            totalHTML: 0,
            total: 0,
            cumulativePDF: 0,
            cumulativeXML: 0,
            cumulativeHTML: 0,
            cumulativeTotal: 0
          },
          pmcViews: {
            month: month,
            year: year,
            cumulativePDF: 0,
            cumulativeXML: 'n.a.',
            cumulativeHTML: 0,
            cumulativeTotal: 0,
            totalPDF: 0,
            totalXML: 'n.a.',
            totalHTML: 0,
            total: 0
          }
        },
        year: year,
        month: month,
        cumulativeTotal: 0,
        cumulativePDF: 0,
        cumulativeXML: 0,
        cumulativeHTML: 0,
        total: 0
      };
    },

    populateHistoryData: function (historyStructure, counterViews, pmcViews) {
      var that = this;
      var cumulativeCounterPDF = 0;
      var cumulativeCounterXML = 0;
      var cumulativeCounterHTML = 0;
      var cumulativeCounterTotal = 0;
      var cumulativePMCPDF = 0;
      var cumulativePMCHTML = 0;
      var cumulativePMCTotal = 0;

      _.each(historyStructure, function (historyMonth, key) {
        var counterViewsData = _.findWhere(counterViews, { month: historyMonth.month.toString(), year: historyMonth.year.toString() });
        var pmcViewsData = _.findWhere(pmcViews, { month: historyMonth.month.toString(), year: historyMonth.year.toString() });

        var counterViewsSource = historyStructure[key].source.counterViews;
        var pmcViewsSource = historyStructure[key].source.pmcViews;

        if(counterViewsData) {
          counterViewsSource.totalPDF = parseInt(counterViewsData.pdf_views);
          counterViewsSource.totalXML = parseInt(counterViewsData.xml_views);
          counterViewsSource.totalHTML = parseInt(counterViewsData.html_views);
          counterViewsSource.total = counterViewsSource.totalPDF + counterViewsSource.totalXML + counterViewsSource.totalHTML;
        }

        cumulativeCounterPDF += counterViewsSource.totalPDF;
        cumulativeCounterXML += counterViewsSource.totalXML;
        cumulativeCounterHTML += counterViewsSource.totalHTML;
        cumulativeCounterTotal += counterViewsSource.total;

        counterViewsSource.cumulativePDF = cumulativeCounterPDF;
        counterViewsSource.cumulativeXML = cumulativeCounterXML;
        counterViewsSource.cumulativeHTML = cumulativeCounterHTML;
        counterViewsSource.cumulativeTotal = cumulativeCounterTotal;

        if(pmcViewsData) {
          pmcViewsSource.totalPDF = parseInt(pmcViewsData.pdf);
          pmcViewsSource.totalHTML = parseInt(pmcViewsData['full-text']);
          pmcViewsSource.total = pmcViewsSource.totalPDF + pmcViewsSource.totalHTML;
        }

        cumulativePMCPDF += pmcViewsSource.totalPDF;
        cumulativePMCHTML += pmcViewsSource.totalHTML;
        cumulativePMCTotal += pmcViewsSource.total;

        pmcViewsSource.cumulativePDF = cumulativePMCPDF;
        pmcViewsSource.cumulativeHTML = cumulativePMCHTML;
        pmcViewsSource.cumulativeTotal = cumulativePMCTotal;

        historyMonth.total = counterViewsSource.total + pmcViewsSource.total;
        historyMonth.cumulativePDF = counterViewsSource.cumulativePDF + pmcViewsSource.cumulativePDF;
        historyMonth.cumulativeXML = counterViewsSource.cumulativeXML;
        historyMonth.cumulativeHTML = counterViewsSource.cumulativeHTML + pmcViewsSource.cumulativeHTML;
        historyMonth.cumulativeTotal = counterViewsSource.cumulativeTotal + pmcViewsSource.cumulativeTotal;

        that.highchartPlosSeriesData.push({ name: key, y: counterViewsSource.cumulativeTotal });
        that.highchartPMCSeriesData.push({ name: key, y: pmcViewsSource.cumulativeTotal });
      });

      return historyStructure;
    },

    filterChartData: function () {
      var pubDate = moment(ArticleData.date, "MMM DD, YYYY").toDate();
      var pubYear = pubDate.getFullYear();
      var pubMonth = pubDate.getMonth() + 1;
      var pubYearMonth = parseInt(this.getYearMonthString(pubMonth, pubYear));
      var filteredData = {};

      var counterViews = this.filterEvents('counter', pubYearMonth);
      var pmcViews = this.filterEvents('pmc', pubYearMonth);
      var relativeMetric = _.filter(this.data.sources, function (source) { return source.name.toLowerCase() == 'relativemetric'});

      if(relativeMetric[0] && relativeMetric[0].events && !_.isEmpty(relativeMetric[0].events.subject_areas)) {
        filteredData.relativeMetricData = relativeMetric[0].events;
      }

      /*
      * Totals calculations:
      * We reduce all the counterViews and pmcViews total to calculate the total views for every kind of file (PDF, XML, HTML).
      * In the end, we sum the counter + pmc to have the grand total. These totals are used in the table.
      */

      var counterViewsTotalPDF = _.reduce(counterViews, function (memo, event) { return memo + parseInt(event.pdf_views); }, 0);
      var counterViewsTotalXML = _.reduce(counterViews, function (memo, event) { return memo + parseInt(event.xml_views); }, 0);
      var counterViewsTotalHTML = _.reduce(counterViews, function (memo, event) { return memo + parseInt(event.html_views); }, 0);
      var counterViewsTotal = counterViewsTotalPDF + counterViewsTotalXML + counterViewsTotalHTML;

      var pmcViewsTotalPDF = _.reduce(pmcViews, function (memo, event) { return memo + parseInt(event.pdf); }, 0);
      var pmcViewsTotalHTML = _.reduce(pmcViews, function (memo, event) { return memo + parseInt(event['full-text']); }, 0);
      var pmcViewsTotal = pmcViewsTotalPDF + pmcViewsTotalHTML;


      filteredData.totalCounterPDF = counterViewsTotalPDF;
      filteredData.totalCounterXML = counterViewsTotalXML;
      filteredData.totalCounterHTML = counterViewsTotalHTML;
      filteredData.totalCouterTotal = counterViewsTotal;

      filteredData.totalPMCPDF = pmcViewsTotalPDF;
      filteredData.totalPMCHTML = pmcViewsTotalHTML;
      filteredData.totalPMCTotal = pmcViewsTotal;

      filteredData.totalPDF = counterViewsTotalPDF + pmcViewsTotalPDF;
      filteredData.totalXML = counterViewsTotalXML;
      filteredData.totalHTML = counterViewsTotalHTML + pmcViewsTotalHTML;
      filteredData.total = counterViewsTotal + pmcViewsTotal;

      /*
      * Data history:
      * The data history is used by the highcharts.js to create a chart with the history month by month of the access data.
      * There is a error on PMC, some months is not being returned, because of this, first we create a structure with all the months then populate with the PMC and counter data.
      */

      var historyStructure = this.createHistoryDataStructure(pubYear, pubMonth);
      filteredData.history = this.populateHistoryData(historyStructure, counterViews, pmcViews);

      this.chartData = filteredData;
    },

    createSummaryTable: function () {
      var summaryTableTemplate =  _.template($('#pageViewsSummary').html());
      var data = this.chartData;
      var dataFormat = 0;
      var summaryTableData = {
        total: data.total.format(dataFormat),
        pubDatesFrom: moment(ArticleData.date, "MMM DD, YYYY").format("MMM DD, YYYY"),
        pubDatesTo:  moment().format("MMM DD, YYYY"),
        totalCounterHTML: data.totalCounterHTML.format(dataFormat),
        totalPMCHTML: data.totalPMCHTML.format(dataFormat),
        totalHTML:  data.totalHTML.format(dataFormat),
        totalCounterPDF: data.totalCounterPDF.format(dataFormat),
        totalPMCPDF:  data.totalPMCPDF.format(dataFormat),
        totalPDF:  data.totalPDF.format(dataFormat),
        totalCounterXML:  data.totalCounterXML.format(dataFormat),
        totalXML: data.totalXML.format(dataFormat),
        totalCouterTotal: data.totalCouterTotal.format(dataFormat),
        totalPMCTotal: data.totalPMCTotal.format(dataFormat),
        totalViewsPDFDownloads: ((data.totalPDF / data.totalHTML) * 100).format(2, ".", ",")
      };

      var summaryTable = summaryTableTemplate(summaryTableData);
      this.$chartElement.append(summaryTable);
    },

    buildHighchartOptions: function (dataHistoryKeys) {
      var data = this.chartData;

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
              fontFamily: "'Open Sans', Verdana, sans-serif",
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
                fontFamily: "'Open Sans', Verdana, sans-serif",
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
            data: this.highchartPMCSeriesData,
            color: "#6d84bf"
          },
          {
            name: "PLOS",
            type: "column",
            data: this.highchartPlosSeriesData,
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
                history = data.history,
                formattedDate = moment(new Date(history[key].year, history[key].month - 1, 2)).format('MMM YYYY');
            // Variable declared for optimization purposes
            var source = history[key].source;

            var template = _.template($('#viewedHighchartTooltipTemplate').html());

            return template({ source: source, formattedDate: formattedDate });
          }
        }
      };

      return options;
    },

    createHighchart: function () {
      // Display the graph only if there are at least two data points (months)
      var showGraph = $('#article-metrics').attr('data-showTooltip');

      if(_.keys(this.chartData.history).length > 1 && showGraph) {
        var dataHistoryKeys = Object.keys(this.chartData.history);
        var highchartOptions = this.buildHighchartOptions(dataHistoryKeys);

        this.$chartElement.append($('<div id="chart"></div>')
          .css("width", "600px")
          .css("height", "200px"));

        var chart = new Highcharts.Chart(highchartOptions);
        this.createRelativeMetricInfo(chart, dataHistoryKeys);
      }
    },

    createRelativeMetricInfo: function (chart, dataHistoryKeys) {
      var that = this;
      if(_.has(this.chartData, 'relativeMetricData') && !_.isEmpty(this.chartData.relativeMetricData)) {
        var template = _.template($('#relativeMetricTemplate').html());
        var subjectAreas = _.sortBy(this.chartData.relativeMetricData.subject_areas, function (subject_area) { return subject_area.subject_area; });
        var subjectAreasList = {};
        _.each(subjectAreas, function (subjectArea) {
          var subjectAreaData = subjectArea.average_usage;
          //Check if the data is not bigger than the chart
          if (subjectAreaData.length > dataHistoryKeys.length) {
            subjectAreaData = subjectAreaData.slice(0, dataHistoryKeys.length);
          }
          //Add the line to the chart
          chart.addSeries({
              id: subjectArea.subject_area,
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

          //Hide the added line
          chart.get(subjectArea.subject_area).hide();

          var subjectAreaTitles = subjectArea.subject_area.split('/');
          if(subjectAreaTitles.length > 2) {
            var item = that.formatSubjectArea(subjectAreaTitles, true);
            if(_.has(subjectAreasList, s.slugify(subjectAreaTitles[1]))) {
              subjectAreasList[s.slugify(subjectAreaTitles[1])].children.push(item);
            }
            else {
              var parentItem = that.formatSubjectArea(subjectAreaTitles, false);
              subjectAreasList[s.slugify(subjectAreaTitles[1])] = parentItem;
              subjectAreasList[s.slugify(subjectAreaTitles[1])].children.push(item);
            }
          }
          else {
            var item = that.formatSubjectArea(subjectAreaTitles, false);
            subjectAreasList[s.slugify(subjectAreaTitles[1])] = item;
          }
        });

        var baseLinkQueryParams = "?pageSize=12&unformattedQuery=(publication_date:[" + this.chartData.relativeMetricData.start_date + " TO " + this.chartData.relativeMetricData.end_date + "]) AND subject:\"SUBJECT_AREA\"";

        var templateData = {
          yearPublished: new Date(this.chartData.relativeMetricData.start_date).getUTCFullYear(),
          subjectAreasList: subjectAreasList,
          referenceUrl: WombatConfig.metrics.referenceUrl
        };

        this.$chartElement.append(template(templateData));

        //Select the first subject area
        var firstSubjectInSelect = $('#subject_areas option:eq(0)').attr('value');
        this.selectSubjectArea(firstSubjectInSelect, chart, baseLinkQueryParams);

        //Callback for the subject area selector
        $('#subject_areas').on('change', function () {
          var value = $(this).val();
          that.selectSubjectArea(value, chart, baseLinkQueryParams);
        });

      }
    },

    selectSubjectArea: function (subjectArea, chart, baseLinkQueryParams) {
      var activeArea = $('#subject_areas').data('active-area');
      var baseLinkToRefset = $('#linkToRefset').data('base-link') + baseLinkQueryParams;
      if(activeArea && chart.get(activeArea)) {
        chart.get(activeArea).hide();
      }
      if(chart.get(subjectArea)) {
        chart.get(subjectArea).show();
      }


      $('#linkToRefset').attr('href', baseLinkToRefset.replace('SUBJECT_AREA', subjectArea));

      $('#subject_areas').data('active-area', subjectArea);
    },

    formatSubjectArea: function (subjectAreaTitles, isChildren) {
      var item = {};

      if(isChildren) {
        item.id = subjectAreaTitles.join('/');
        item.title = subjectAreaTitles[2];
      }
      else {
        item.id = '/'+subjectAreaTitles[1];
          item.title = subjectAreaTitles[1];
        item.children = [];
      }

      return item;
    }

  });
})(jQuery);