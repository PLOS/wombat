/*
* Pagination Class:
* This component is a JavaScript version of the project default pagination, it behaves in the same way the FTL pagination macro. It's used to AJAX request paginations.
*
* Usage reference:
*
* var pagination = new Pagination(currentPage, totalRecords, itemsPerPage, function (newPageNumber) {
*   currentPage = newPageNumber;
*   processRequest();
* });
*
* For examples of implementation, you can look at SearchResult class or EditorialBoard class.
*
*/

var Pagination;

(function ($) {

  Pagination = Class.extend({
    totalRecords: 0,
    currentPage: 1,
    itemsPerPage: 0,
    /*
    * The default callback function, the callback is called every time a user clicks in a item on the pagination, the currentPage is the number of the new page.
    * This function should be used to process a new ajax request using the new page offset.
    */
    callback: function (currentPage) {

    },

    /*
    * The init function for the class, arguments:
    * currentPage: the current page to the pagination start.
    * totalRecords: total records for the query, in page or not.
    * itemsPerPage: total records displayed per page, used to calculate the pages quantity.
    * callback: function to be called after a page change. Should follow the pattern established in the default callback function.
    */
    init: function (currentPage, totalRecords, itemsPerPage, callback) {
      // Set variables in class
      this.setCurrentPage(currentPage);
      this.setTotalRecords(totalRecords);
      this.setItemsPerPage(itemsPerPage);
      this.callback = callback;

      // Bind all the pagination events to the page
      this.bindPaginationEvents();
    },

    // Getters and setters for the class variables. Should be used instead direct access to the variables.
    setTotalRecords: function (totalRecords) {
      this.totalRecords = totalRecords;
    },
    setCurrentPage: function (currentPage) {
      this.currentPage = currentPage;
    },
    setItemsPerPage: function (itemsPerPage) {
      this.itemsPerPage = itemsPerPage;
    },

    getTotalRecords: function () {
      return this.totalRecords;
    },
    getCurrentPage: function () {
      return this.currentPage;
    },
    getItemsPerPage: function () {
      return this.itemsPerPage;
    },
    // The total pages is calculated based on the total records and items per page.
    getTotalPages: function () {
      return Math.ceil(this.getTotalRecords()/this.getItemsPerPage());
    },

    /*
    * Creates the DOM element to append to the HTML. You should generate a new element every time you do a AJAX request and append it to the page manually.
    */
    createPaginationElement: function () {
      // Only generates a element if has more than one page.
      if(this.getTotalPages() > 1) {
        var that = this;
        var paginationTemplate = _.template($('#resultsPaginationTemplate').html());
        /*
        * For each element on the pagination (prev page button, page button, separator, etc) we append a new page object. The format for the page object is:
        * {
        *   type: string (type of the page element, options: 'previous-page', 'next-page', 'divider', 'number')
        *   disabled: boolean (disable the click on the element, valid for 'previous-page' and 'next-page' type only)
        *   current: boolean (is the current page element, valid for 'number' type only)
        *   page: integer (the page number for this element, valid for 'number' type only)
        * }
        * */
        var paginationData = [];

        /*
        * The pagination is made of 3 parts:
        * startPages: first section before the separator, if the current page is between 1 and 5, this part will be a array from 1 to 5. If the current page is bigger than 5, this array will have only the page 1.
        * middlePages: second section before the separator, if the current page is bigger than 5 and less than getTotalPages-4, this part will contain the following structure: [currentPage-1, currentPage, currentPage+1]. If don't attend to this requirements, will be empty.
        * finalPages: last section of the pagination, if the current page is bigger than getTotalPages-4, this part will be a array from getTotalPages-4 to getTotalPages. If the current page is less than getTotalPages-4, this array will have only the last page number.
        *
        * The pagination is made in three different arrays to avoid repetition between them and by the end is merged to form the pages array.
        * */
        var startPages = [];
        var middlePages = [];
        var finalPages = [];
        // The final result of merging startPages, middlePages and finalPages.
        var pages = [];

        //Create previos page object
        paginationData.push({
          type: 'previous-page',
          disabled: (this.getCurrentPage() == 1)
        });

        // Logic for the startPages
        if(this.getCurrentPage() < 5 || this.getTotalPages() == 5) {
          var i = 1;
          var limit = 5;
          if(this.getTotalPages() < 5) {
            limit = this.getTotalPages();
          }
          while(i <= limit) {
            startPages.push(i);
            i++;
          }
        }
        else {
          startPages.push(1);
        }

        // Logic for the middlePages
        if(this.getCurrentPage() >= 5 && this.getCurrentPage() <= (this.getTotalPages()-4)) {
          var i = this.getCurrentPage() - 1;
          var limit = this.getCurrentPage() + 1;
          while(i <= limit) {
            middlePages.push(i);
            i++;
          }
        }

        // Logic for the finalPages
        if(this.getTotalPages() > 5 && this.getCurrentPage() > (this.getTotalPages()-4)) {
          var i = this.getTotalPages()-4;
          if(i < 1) {
            i = 1;
          }
          var limit = this.getTotalPages();
          while(i <= limit) {
            finalPages.push(i);
            i++;
          }
        }
        else {
          finalPages.push(this.getTotalPages());
        }

        // Use underscore union to avoid repetition
        pages = _.union(startPages, middlePages, finalPages);

        // Gets the pages array and do a each to put into a page object.
        _.each(pages, function (page, index) {
          // If the current page is different than the last index-1, we add a divider before it.
          if(index > 0 && pages[index - 1] != page-1) {
            paginationData.push({
              type: 'divider'
            });
          }
          paginationData.push({
            type: 'number',
            page: page,
            current: (page == that.getCurrentPage())
          });
        });

        //Create next page object
        paginationData.push({
          type: 'next-page',
          disabled: (this.getCurrentPage() == this.getTotalPages())
        });

        return paginationTemplate({ pages: paginationData });
      }
    },
    
    // All the pagination events are bind to the main tag, we cannot bind it to the div.pagination because it's dinamic also.
    bindPaginationEvents: function () {
      var that = this;
      $('main').on('click', 'a.number', function (e) {
        e.preventDefault();
        that.setCurrentPage(parseInt($(this).data('page')));
        that.callback(that.getCurrentPage());
      }).on('click', 'a.previous-page', function (e) {
        e.preventDefault();
        that.setCurrentPage(that.getCurrentPage()-1);
        that.callback(that.getCurrentPage());
      }).on('click', 'a.next-page', function (e) {
        e.preventDefault();
        that.setCurrentPage(that.getCurrentPage()+1);
        that.callback(that.getCurrentPage());
      });
    },
  });

})(jQuery);
