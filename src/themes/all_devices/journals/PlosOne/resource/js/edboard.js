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

var EditorialBoard;

(function ($) {
  EditorialBoard = Class.extend({
    $searchInputEl: $('#editors-search-input'),
    $searchButtonEl: $('.editors-search-button'),
    $searchResetEl: $('.editors-search-reset'),
    $loadingEl: $('#loading'),
    solrHost: SOLR_CONFIG.solrHost,
    $resultListEl: $('.results'),
    resultTotalRecords: 0,
    resultCurrentPage: 1,
    resultCurrentOffset: 0,
    resultTotalPages: 1,
    resultItemsPerPage: 50,
    currentSearchTerm: null,
    pagination: null,

    init: function () {
      var that = this;
      this.pagination = new Pagination(this.resultCurrentPage, this.resultTotalRecords, this.resultItemsPerPage, function (currentPage) {
        that.resultCurrentPage = currentPage;

        that.getResultList();
      });
      this.initializeAutocomplete();
      this.getResultList();

    },
    //Loading spinner functions
    showLoading: function () {
      this.$loadingEl.show();
    },
    hideLoading: function () {
      this.$loadingEl.hide();
    },
    replaceTag: function(tag) {
      var tagsToReplace = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;'
      };
      return tagsToReplace[tag] || tag;
    },
    safe_tags_replace: function(str) {
      return str.replace(/[&<>]/g, this.replaceTag);
    },
    //Autocomplete functions
    bindAutocompleteEvents: function () {
      var that = this;

      this.$searchInputEl.on('typeahead:asyncrequest', function () { //Show loading on start autocomplete request.
        that.showLoading();
      }).on('typeahead:asyncreceive', function () { //Hide loading after autocomplete request.
        that.hideLoading();
      }).on('keydown', function (e) {
        //Process search on enter keydown if the autocomplete list is closed.
        var code = e.keyCode || e.which;
        if (code == 13 && !$('.tt-menu').hasClass('tt-open')) {
          that.currentSearchTerm = $(this).typeahead('val');
          that.getResultList();
        }
      }).on('keyup', function () {
        var inputValue = that.$searchInputEl.typeahead('val');
        if (!_.isEmpty(inputValue) && that.$searchResetEl.css('display') != 'block') {
          that.$searchResetEl.css("display", "block");
        }
        else if (_.isEmpty(inputValue) && that.$searchResetEl.css('display') == 'block') {
          that.$searchResetEl.css("display", "none");
        }
      });

      this.$searchResetEl.on('click', function () {
        that.$searchInputEl.typeahead('val', "");
        that.$searchResetEl.css("display", "none");
        that.currentSearchTerm = null;
        that.getResultList();
        that.$searchInputEl.focus();
      });

      this.$searchButtonEl.on('click', function () {
        that.currentSearchTerm = that.safe_tags_replace(that.$searchInputEl.typeahead('val'));
        that.getResultList();
      });
    },
    //If the term starts and ends with ", it's specific.
    isSpecificTerm: function (term) {
      return term[0] == '"' && term[term.length - 1] == '"';
    },
    //Generate solr query to the autocomplete terms
    generateAutocompleteQuery: function (entry, noPrefix) {
      var that = this;
      // Pattern for not specific term
      // As ae_name do not support partial name matching, we are using ae_name_facet
      var fieldsPattern = "(ae_subject_facet: %TERM% OR ae_name_facet: %TERM%)";
      //Pattern for term specific
      var specificTermPattern = "ae_name_facet: %TERM%";
      var hasMultipleTerm = (entry.indexOf(',') > -1 && entry.indexOf(',') != entry.length - 1);
      var query = null;
      //The last term is always returned in the prefix variable, if not multiple, only return the prefix.
      var prefix = null;
      var terms = [];

      //If has more than one term, each term should be mapped to the fieldsPattern.
      if (hasMultipleTerm || noPrefix) {
        terms = entry.replace(', ', ',').split(",");
        if (!noPrefix) {
          prefix = $.trim(terms.pop().toLowerCase());
        }

        //Filter terms not empty
        terms = _.filter(terms, function (term) {
          var term = $.trim(terms);
          return term.length > 0;
        });
        if (!noPrefix) {
          terms = terms.slice(0);
        }
        //Map all terms to the pattern, checking if it's specific or not.
        query = _.map(terms, function (term) {
          term = $.trim(term);
          if (that.isSpecificTerm(term)) {
            return specificTermPattern.split('%TERM%').join(term);
          }
          else {
            var enclosed_term = '"' + term + '"';
            return fieldsPattern.split('%TERM%').join(enclosed_term.toLowerCase());
          }
        });
      }
      else {
        //If has only one term, return it in the prefix and a general query.
        query = ["*:*"];
        prefix = $.trim(entry.toLowerCase());
      }

      var response = {
        query: query,
        prefix: prefix,
        terms: terms
      };
      return response;
    },

    //Initialize all the autocomplete functions and datasource
    initializeAutocomplete: function () {
      var that = this;
      //Bind the custom autocomplete events
      this.bindAutocompleteEvents();

      //Options for the typeahead
      var typeaheadOptions = {
        hint: false,
        highlight: true
      };

      //Function for the people data source, responsible for the ajax request and object mapping and treatment.
      var peopleDataSource = function (entry, syncResults, asyncResults) {
        var actualQuery = that.generateAutocompleteQuery(entry);
        var query = actualQuery.query;
        //Split the person name in a array and map to a custom query for each name.
        var prefixParts = actualQuery.prefix.split(' ');
        var prefixLastPart = prefixParts.pop();
        var prefixQuery = _.map(prefixParts, function (part) {
          return 'ae_name_facet: "' + part + '"';
        });
        //Append the last name to the prefix query with a custom query.
        prefixQuery.push('ae_name_facet: "' + prefixLastPart.toLowerCase() + '"');

        //If has more than one term, concat the others terms query with the people name query.
        if (query[0] != "*:*") {
          prefixQuery = query.concat(prefixQuery);
        }

        prefixQuery = prefixQuery.join(' AND ');

        //Data to send to the solr API
        var data = [
          {name: "wt", value: "json"},
          {name: "q", value: prefixQuery},
          {name: "fl", value: "ae_name"},
          {name: "rows", value: 20},
          {name: "fq", value: "doc_type:(section_editor OR academic_editor)"},
          {name: "sort", value: "ae_last_name asc"},
          {name: "facet", value: false}
        ];

        $.ajax({
          url: that.solrHost,
          context: document.body,
          timeout: 10000,
          data: data,
          jsonp: "json.wrf", dataType: "jsonp",
          success: function (json_names, textStatus, xOptions) {
            //Join all the terms to a string divided by commas, to use as new input value after a autocomplete option is chosen.
            var termsString = actualQuery.terms.join(', ');
            if (!_.isEmpty(termsString)) {
              termsString = termsString + ', ';
            }
            //Map all the results found to a new object structure
            var response = _.map(json_names.response.docs, function (doc) {
              return {
                value: termsString + doc.ae_name,
                name: doc.ae_name,
                term: doc.ae_name
              };
            });

            //Filter the result to don't show a result already in the input.
            response = _.filter(response, function (item) {
              return (_.indexOf(actualQuery.terms, item.term) < 0);
            });

            //Send results to typeahead render
            asyncResults(response);
          },
          error: function (xOptions, error) {
            console.log('error');
          }
        });
      };

      //Function for the subject data source, responsible for the ajax request and object mapping and treatment.
      var subjectsDataSource = function (entry, syncResults, asyncResults) {
        var actualQuery = that.generateAutocompleteQuery(entry); //todo: 'actual'-named variables are a smell and should be refactored
        var query = actualQuery.query.join(" AND ");

        var data = [
          {name: "wt", value: "json"},
          {name: "q", value: query},
          {name: "fq", value: "doc_type:(section_editor OR academic_editor)"},
          {name: "facet", value: true},
          {name: "facet.field", value: "ae_subject_facet"},
          {name: "facet.sort", value: "index"},
          {name: "facet.mincount", value: 1},
          {name: "facet.limit", value: 5},
          {name: "facet.prefix", value: actualQuery.prefix}
        ];

        $.ajax({
          url: that.solrHost,
          context: document.body,
          timeout: 10000,
          data: data,
          jsonp: "json.wrf", dataType: "jsonp",
          success: function (json_subjects, textStatus, xOptions) {
            var items = _.filter(json_subjects.facet_counts.facet_fields.ae_subject_facet,
              function (item, key) {
                return (key % 2 == 0)
              });
            var count = _.filter(json_subjects.facet_counts.facet_fields.ae_subject_facet,
              function (item, key) {
                return !(key % 2 == 0)
              });
            var termsString = actualQuery.terms.join(', ');

            if (!_.isEmpty(termsString)) {
              termsString = termsString + ', ';
            }

            //todo: add some explanatory documentation?
            var response = _.map(items, function (item, key) {
              return {name: item + " (" + count[key] + ")", value: termsString + item, term: item};
            });

            response = _.filter(response, function (item) {
              return (_.indexOf(actualQuery.terms, item.term) < 0);
            });

            asyncResults(response);
          },
          error: function (xOptions, error) {
            console.log('error');
          }
        });
      };

      //Load underscore templates for the data sources title and item.
      var sectionHeaderTemplate = _.template($('#autocompleteSectionTitleTemplate').html());
      var itemTemplate = _.template($('#autocompleteItemTemplate').html());

      //todo: combine into one configurable Dataset object
      //People and Subject data source configuration object for typeahead.
      var peopleDataset = {
        name: 'people',
        source: peopleDataSource,
        async: true,
        displayKey: 'value',
        limit: 5,
        templates: {
          header: sectionHeaderTemplate({sectionTitle: 'People'}),
          suggestion: function (item) {
            return itemTemplate(item);
          }
        }

      };

      var subjectsDataset = {
        name: 'subjects',
        source: subjectsDataSource,
        async: true,
        displayKey: 'value',
        limit: 5,
        templates: {
          header: sectionHeaderTemplate({sectionTitle: 'Subject Areas'}),
          suggestion: function (item) {
            return itemTemplate(item);
          }
        }
      };

      //Initialize typeahead to the $seachInputEl with the options and data sets.
      this.$searchInputEl.typeahead(typeaheadOptions, subjectsDataset, peopleDataset);
    },

    getResultList: function () {
      var that = this;
      this.showLoading();
      var listTemplate = _.template($('#editorsListTemplate').html());
      this.calculateCurrentOffset();
      var query = "*:*";

      if (!_.isEmpty(this.currentSearchTerm)) {
        var actualQuery = this.generateAutocompleteQuery(this.currentSearchTerm, true);
        query = actualQuery.query.join(' AND ');
      }

      var data = [
        {name: "wt", value: "json"},
        {name: "q", value: query},
        {name: "fl", value: "doc_type,ae_name,ae_institute,ae_country,ae_subject,ae_ORCID"},
        {name: "rows", value: this.resultItemsPerPage},
        {name: "start", value: this.resultCurrentOffset},
        {name: "fq", value: "doc_type:(section_editor OR academic_editor)"},
        {name: "sort", value: "ae_last_name asc,ae_name asc"},
        {name: "facet", value: false}
      ];

      // todo: combine with other similar ajax calls (2)
      $.ajax({
        url: that.solrHost,
        context: document.body,
        timeout: 10000,
        data: data,
        jsonp: "json.wrf", dataType: "jsonp",
        success: function (data, textStatus, xOptions) { //todo: different success functions for different ajax calls
          var response = data.response;
          that.resultTotalRecords = response.numFound;

          that.pagination.setTotalRecords(that.resultTotalRecords);
          that.pagination.setCurrentPage(that.resultCurrentPage);

          var list = listTemplate(response);
          var pagination = that.pagination.createPaginationElement();
          var counters = that.createCountersElement();

          that.$resultListEl.html('');
          that.$resultListEl.append(counters);
          that.$resultListEl.append(pagination);
          that.$resultListEl.append(list);
          that.$resultListEl.append(pagination);
          that.hideLoading();
        },
        error: function (xOptions, error) {
          that.hideLoading();
        }
      });

    },
    //todo: explanatory documentation
    createCountersElement: function () {
      var counterTemplate = _.template($('#resultsCounterTemplate').html());

      var lastRecord = this.resultCurrentOffset + this.resultItemsPerPage;
      if (lastRecord > this.resultTotalRecords) {
        lastRecord = this.resultTotalRecords;
      }

      return counterTemplate({
        firstRecord: this.resultCurrentOffset + 1,
        lastRecord: lastRecord,
        totalRecords: this.resultTotalRecords,
        currentSearchTerm: this.currentSearchTerm
      });
    },
    calculateCurrentOffset: function () {
      this.resultCurrentOffset = (this.resultCurrentPage - 1) * this.resultItemsPerPage;
    }

  });

  new EditorialBoard();
})(jQuery);