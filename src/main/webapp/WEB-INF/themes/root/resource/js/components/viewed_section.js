var ViewedSection;

(function ($) {
  ViewedSection = MetricsTabComponent.extend({
    $element: $('#views'),
    $loadingEl: $('#chartSpinner'),
    $chartElement: $('#usage'),
    sourceOrder: ['figshare'],
    chartData:[],

    loadData: function (data) {
      this._super(data);
      this.$chartElement.hide();

      this.filterChartData();
      this.createSummaryTable();
      this.createTiles();

      this.afterLoadData();
    },

    afterLoadData: function () {
      this._super();
      this.$chartElement.show();
    },

    //Join year and month to a string as a counter
    getYearMonthString: function (month, year, separator) {
      if(!separator) {
        separator = '';
      }
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
          return parseInt(that.getYearMonthString(event.month, event.year)) > pubYearMonth;
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

    filterChartData: function () {
      var that = this;
      var pubDate = moment(ArticleData.date, "MMM DD, YYYY").toDate();
      var pubYear = pubDate.getFullYear();
      var pubMonth = pubDate.getMonth() + 1;
      var pubYearMonth = parseInt(this.getYearMonthString(pubMonth, pubYear));
      var filteredData = {};

      var counterViews = this.filterEvents('counter', pubYearMonth);
      var pmcViews = this.filterEvents('pmc', pubYearMonth);
      var relativeMetric = _.filter(this.data.sources, function (source) { return source.name.toLowerCase() == 'relativemetrics'});

      if(relativeMetric[0] && relativeMetric[0].events && relativeMetric[0].events.length > 0) {
        filteredData.relativeMetricData = relativeMetric;
      }

      filteredData.totalPDF = 0;
      filteredData.totalXML = 0;
      filteredData.totalHTML = 0;
      filteredData.total = 0;
      filteredData.history = {};


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
    }
  });
})(jQuery);