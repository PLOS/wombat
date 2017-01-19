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

/*
* MetricsTabComponent:
* This is the base class for any component in MetricsTab, it already has the functions needed to filter the sources and create the tiles.
* The MetricsTabComponent also encapsulate the essential methods to a MetricsTab component work (loadData, newArticleError and dataError).
* The 'loadData()' is responsible to load the data to the component, the 'newArticleError()' is responsible to handle 'NewArticleError' error and the 'dataError()' handles any other error.
*
* Every MetricsTab component should extend this class and be instantiated in 'MetricsTab.components'.
*
* Usage example:
*
* var MyComponent = MetricsTabComponent.extend({
*   $loadingEl: $('#MyComponentSpinner'),
*   $element: $('#MyComponent'),
*   sourceOrder: ['facebook', 'twitter', 'pmc'],
*
*   loadData: function (data) {
*     this._super(data);
*
*     this.createTiles();
*
*     this.afterLoadData();
*   }
* });
*/

var MetricsTabComponent;

(function ($) {
  MetricsTabComponent = Class.extend({
    //Loading spinner jQuery Element
    $loadingEl: null,
    //The main jQuery element of the component, where the tiles should be appended.
    $element: null,
    //Header for the section jQuery Element
    $headerEl: null,
    //The order that the sources needs to be displayed in the tiles
    sourceOrder: [],
    //Filled by the loadData function
    data: [],
    //Filled by the filterSources function
    sources:[],

    init: function () {
      this.$loadingEl.show();
      this.$element.hide();
    },

    loadData: function (data) {
      //Fix for CrossRef, the metrics.total is not calculated by ALM
      var crossrefIndex = _.findIndex(data.sources, {name: 'crossref'});
      if(crossrefIndex >= 0){
        var crossref = data.sources[crossrefIndex];
        if(!_.has(crossref, 'metrics')) {
          crossref.metrics = { total: 0 };
        }

        if(_.has(crossref, 'by_month') && crossref.metrics.total <= 0) {
          var total = _.reduce(crossref.by_month, function (memo, obj) { return memo + obj.total }, 0);
          crossref.metrics.total = total;
        }
      }

      this.data = data;
      this.filterSources();
    },

    showComponent: function () {
      this.$loadingEl.hide();
      this.$element.show();
      this.$headerEl.show();
    },

    hideComponent: function () {
      this.$loadingEl.hide();
      this.$element.hide();
      this.$headerEl.hide();
    },

    //Default behavior when the component is empty, hide all the components
    emptyComponent: function () {
      this.hideComponent();
    },

    afterLoadData: function () {
      if(this.sources.length) {
        this.showComponent();
      }
      else {
        this.emptyComponent();
      }
    },

    filterSources: function () {
      var that = this;
      //Filter only the sources that are in the sourceOrder and metrics total is bigger then 0
      var sourcesUnordered = _.filter(this.data.sources, function (source) {
        return _.contains(that.sourceOrder, source.name) && source.metrics.total > 0;
      });
      //Transform the sourceOrder array in object and invert the value with the key
      var sourceOrderKeys = _.invert(_.object(_.pairs(this.sourceOrder)));
      //Reorder sources based on the key of that value in the original sourceOrder
      this.sources = _.sortBy(sourcesUnordered, function(source) { return sourceOrderKeys[source.name] });
    },

    formatName: function (name) {
      //Remove spaces
      name = name.replace(/\s/g, "");
      // removing registered trademark symbol from web of science
      name = name.replace("\u00ae", "");
      return name;
    },

    createTiles: function () {
      var that = this;
      _.each(this.sources, function (source) {
        source.display_name = that.formatName(source.display_name);
        var tile = new MetricTile(source);
        tile.createTile(that.$element);
      });
    },

    newArticleError: function () {
      this.$element.hide();
      this.$loadingEl.hide();
    },

    dataError: function () {
      this.$element.hide();
      this.$loadingEl.hide();
    }

  });
})(jQuery);