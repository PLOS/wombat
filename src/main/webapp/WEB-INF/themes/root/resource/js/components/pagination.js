var Pagination;

(function ($) {

  Pagination = Class.extend({
    totalRecords: 0,
    currentPage: 0,
    itemsPerPage: 0,
    callback: function (currentPage) {

    },

    init: function (currentPage, totalRecords, itemsPerPage, callback) {
      this.setCurrentPage(currentPage);
      this.setTotalRecords(totalRecords);
      this.setItemsPerPage(itemsPerPage);

      this.callback = callback;

      this.bindPaginationEvents();
    },
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
    getTotalPages: function () {
      return Math.ceil(this.getTotalRecords()/this.getItemsPerPage());
    },

    createPaginationElement: function () {
      if(this.getTotalPages() > 1) {
        var that = this;
        var paginationTemplate = _.template($('#resultsPaginationTemplate').html());
        var paginationData = [];
        var startPages = [];
        var middlePages = [];
        var finalPages = [];
        var pages = [];

        //Create previos page object
        paginationData.push({
          type: 'previous-page',
          disabled: (this.getCurrentPage() == 1)
        });

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

        if(this.getCurrentPage() >= 5 && this.getCurrentPage() <= (this.getTotalPages()-4)) {
          var i = this.getCurrentPage() - 1;
          var limit = this.getCurrentPage() + 1;
          while(i <= limit) {
            middlePages.push(i);
            i++;
          }
        }

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

        pages = _.union(startPages, middlePages, finalPages);

        _.each(pages, function (page, index) {
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
