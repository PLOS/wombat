//Content - contains javascript necessary for loading and displaying content

var SiteContent = function () {
  var self = this;

  self.init = function () {

    //global dom references
    self.$modalInfoWindow = $('.modal-info-window');
    self.$articlePagination = $('#article-pagination');

    //global variables
    self.windowHeight = self.$modalInfoWindow.height();
    self.modalPosition = self.windowHeight;

    //events
    $('#article-type-menu').find('li').on('click', function (e) {
      var $currentButton = $(this);
      self.switchArticleListMethod($currentButton);
    });

    $('.author-info').on('click', function (e) {
      e.preventDefault();
      self.showAuthorInfo($(this));
    });

    $('.filter-button').on('click', function (e) {
      self.toggleFilterButton($(this));
    });

    $('#display-options button').on('click', function (e) {
      self.setDisplayOption($(this));
    });

    $('.modal-tab').on('click', function (e) {
      e.preventDefault();
      self.hideModalTab($(this));
    });

    self.$articlePagination.find('.number').on('click', function (e) {
      e.preventDefault();
      self.gotoResultsPage($(this));
    });

    self.$articlePagination.find('.switch').on('click', function (e) {
      e.preventDefault();
      self.switchResultsPage($(this));
    });

  } //end init


  self.gotoResultsPage = function ($pageLink) {
    var pageNumber = $pageLink.attr('data-page');

    self.$articlePagination.find('.number.active').removeClass('active');
    $pageLink.addClass('active');

    self.loadPage(pageNumber);

  }

  self.switchResultsPage = function ($pageLink) {
    var pageNumber = parseInt(self.$articlePagination.find('.number.active').attr('data-page'));
    var switchMethod = $pageLink.attr('data-method');

    var $activePageLink = self.$articlePagination.find('.number.active');

    if (switchMethod == "previous") {
      if (pageNumber != 1) {
        $activePageLink.removeClass('active');
        pageNumber--;
        $activePageLink.prev().addClass('active');
        self.loadPage(pageNumber);
      }
    } else if (switchMethod == "next") {
      var numSeq = self.$articlePagination.find('a.seq').length;
      var totalPages = parseInt(self.$articlePagination.find('a.last').attr('data-page'));

      if (pageNumber != totalPages) {
        $activePageLink.removeClass('active');
        pageNumber++;
        if (numSeq == pageNumber) {
          //PL-INT - if this is the last element in the sequence of page numbers, update pagination, otherwise go to the next page
          $activePageLink.next().addClass('active');
          self.loadPage(pageNumber);
        } else {
          $activePageLink.next().addClass('active');
          self.loadPage(pageNumber);
        } //end check for end of sequence

      } // end check for end of pages

    } //end next page method

  } //end switchResultsPage

  self.loadPage = function (pageNumber) {
    //PL-INT - insert logic to navigate to the chosen page. The ajax call below is to static content
    $.ajax({
      url: "temp/ajax/articles-recent.html"
    }).done(function (data) {
        $("#article-results").html(data);
      });
  }

  self.hideModalTab = function ($modalTab) {

    var supportsFixedPosition = Modernizr.positionfixed;

    if (!supportsFixedPosition) { //if there is no support for fixed position, we need to handle the modal a different way

      //PL-INT - need to insert logic for capturing the current figure and loading the proper URL if browser doesn't support fixed position
      //Note that this function could be called from somewhere other than figures, though currently figures is the only template which implements a tabbed modal window
      window.location = 'temp-figure-info.html'; //redirects to homepage

    } else {

      self.modalPosition = self.windowHeight - 10; //we want part of the window showing here
      $modalTab.animate({'top': '0px'}, 100, function () {
        $modalTab.css({'display': 'none'});
        self.showModalWindow(self.figureModalShown, null, "tab");
      });

    }

  }

  self.showModalTab = function () {

    $('.modal-tab').css({'display': 'block'}).animate({'top': '-35px'}, 100);

  }

  self.figureModalShown = function () {

    self.$modalInfoWindow.find('.close').one('click', function (e) { //enable close functionality
      e.preventDefault();
      self.hideModalWindow(self.showModalTab, false, "tab"); //callback, removeContent
    });

  }

  self.switchArticleListMethod = function ($currentButton) { //switch between different methods of displaying article content

    var isActive = $currentButton.hasClass('active');

    if (!isActive) { //if it's not active find any buttons who are and disable them, then execute the proper list method
      $('#article-type-menu').find('li.active').removeClass('active');
      $currentButton.addClass('active');

      var listMethod = $currentButton.attr('data-method');
      self.loadArticleList(listMethod);
    }

  } //end switchArticleListMethod

  self.loadArticleList = function (listMethod) {
    //PL-INT - Put in logic to call the proper article result set and replace ajax call below
    $.ajax({
      url: "temp/ajax/articles-" + listMethod + ".html"
    }).done(function (data) {
        $("#article-results").html(data);
      });
    // END PL-INT
  } //end loadArticleList  

  self.showAuthorInfo = function ($authorLink) {
    var authorID = $authorLink.attr('data-author-id'); //PL-INT - determine what info needs be captured here
    var supportsFixedPosition = Modernizr.positionfixed;

    if (!supportsFixedPosition) { //if there is no support for fixed position, we need to handle the modal a different way
      window.location = 'temp-author-info.html'; //redirects to homepage
    } else {
      var isActive = self.$modalInfoWindow.hasClass('active');

      if (isActive) { //if the window is active, just load the content

        self.loadAuthorInfo(authorID);

      } else { //animate the window, then load the content

        var options = { };
        options.authorID = authorID;
        self.showModalWindow(self.authorModalShown, options); //callback, options

      }

    } //end support for fixed position

  } //end showAuthorInfo

  self.authorModalShown = function (options) {

    var authorID = options.authorID;

    self.$modalInfoWindow.find('.close').one('click', function (e) { //enable close functionality
      e.preventDefault();
      self.hideModalWindow(null, true); //callback, remove
    });

    self.loadAuthorInfo(authorID);

  } //end authorModalShown

  self.loadAuthorInfo = function (options) {

    var authorID = options.authorID;

    return $.ajax({
      url: "temp/ajax/author-info.html" //PL-INT should construct the URL dynamically here
    }).done(function (data) {
        self.$modalInfoWindow.find(".modal-content").html(data);
      });

  } //end loadAuthorInfo

  self.findOpenModals = function (callback) {

    var isActive = self.$modalInfoWindow.hasClass('active');

    if (isActive) { //if a modal window is open, we'll close it

      var displayMethod = self.$modalInfoWindow.attr('data-method');
      ambraNavigation.modalOptions.showModalOnMenuClose = true; //we want to reactivate this modal when the user re-hides the navigation
      ambraNavigation.modalOptions.displayMethod = displayMethod;
      self.hideModalWindow(callback, null, displayMethod);

    } else {

      ambraNavigation.modalOptions.showModalOnMenuClose = false; //since the modal was never open, we won't need to show it again when we return from the menu

      if (typeof callback === "function") {
        callback();
      }

    }

  }

  self.hideModalWindow = function (callback, removeContent, method) {

    self.$modalInfoWindow.removeClass('active');

    if (removeContent == true) {
      self.$modalInfoWindow.find(".modal-content").empty(); //clear the window 
    }

    var modalHidePosition;

    if (method == "full" || method == "tab") {
      modalHidePosition = "-100%";
    } else {
      modalHidePosition = -self.modalPosition + 'px';
    }

    self.$modalInfoWindow.animate({ 'bottom': modalHidePosition }, 200,function () {

      self.enableContentScrolling();

      if (method != "tab") {
        self.$modalInfoWindow.hide();
      } else {
        self.$modalInfoWindow.animate({'margin-bottom': '10px' }, 50);
      }

      if (typeof callback === "function") {
        callback();
      }

    }).promise();

  } //end hideModalWindow

  self.showModalWindow = function (callback, options, method) {

    var modalShowPosition;

    if (method == "full" || method == "tab") {
      modalShowPosition = '0%';
    } else {
      modalShowPosition = '0px';
    }

    self.$modalInfoWindow.show();
    self.disableContentScrolling(); //only allow the modal to scroll, not the content below it

    if (method == "tab") {
      self.$modalInfoWindow.animate({ 'margin-bottom': '0px' }, 50);
    }

    self.$modalInfoWindow.animate({ 'bottom': modalShowPosition }, 200, function () {

      self.$modalInfoWindow.addClass('active');
      if (typeof callback === "function") {
        callback(options);
      }

    });

  } //end showModalWindow

  self.disableContentScrolling = function () {
    ambraNavigation.$containerMain.addClass('inactive');
  }

  self.enableContentScrolling = function () {
    ambraNavigation.$containerMain.removeClass('inactive');
  }

  self.toggleFilterButton = function ($filterButton) {
    var isActive = $filterButton.hasClass('active');
    var $filterBox = $('.filter-box');

    if (isActive) { //hide the filter box and remove active state

      $filterButton.removeClass('active');
      $filterBox.removeClass('active');

    } else { //show filter box and add active state. Enable cancel and apply

      $filterButton.addClass('active');
      $filterBox.addClass('active');

      $filterBox.find('.cancel').one('click', function () {
        self.toggleFilterButton($filterButton);
      });

      $filterBox.find('.apply').one('click', function () {
        self.applyFilter($filterBox, $filterButton);
      });

    }

  }

  self.applyFilter = function ($filterBox, $filterButton) {
    var filterFunction = $filterBox.attr('data-function');

    //PL-INT - Insert filter logic here - function below captures form values for date and sort

    switch (filterFunction) {
      case 'date-and-sort':
        var dateVal = $filterBox.find('.date select').val();
        var sortVal = $filterBox.find('.sort select').val();
        self.toggleFilterButton($filterButton); //closes the filter box
        break;
    }

    var $searchForm = $('#sortAndFilterSearchResults');
    $searchForm.sortOrder.value = sortVal;
    $searchForm.submit();
  }

  self.resetFilterBox = function ($filterBox) {

    $filterBox.find('select').each(function (index) {
      $(this).prop('selectedIndex', 0); //sets all select boxes to the first option in the list
    });

  }

  self.setDisplayOption = function ($displayButton) {
    var isActive = $displayButton.hasClass('active');

    if (!isActive) { //if it's not active, activate it and toggle content visibility

      var $displayGroup = $displayButton.parent('.buttongroup');
      $displayGroup.find('.active').removeClass('active');
      $displayButton.addClass('active');

      var displayType = $displayButton.attr('data-type');
      $('#article-items').attr('class', displayType);

    }
  }

}


var siteContentClass;

$(document).ready(function () {

  siteContentClass = new SiteContent();
  siteContentClass.init();

});