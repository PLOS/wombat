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

//Share - contains javascript necessary for saving and sharing content

var ShareContent = function () {
  var self = this;

  self.init = function () {

    //global dom references
    self.$modalInfoWindow = $('.modal-info-window');
    self.$articleItems = $('#article-items');

    self.$savedAmt = $('#saved-amt');
    self.$savedListOptions = $('#saved-list-options');

    self.$optionShare = $('#option-share');
    self.$optionEmail = $('#option-email');
    self.$optionEdit = $('#option-edit');
    self.$optionClearAll = $('#option-clear-all');

    //global variables
    self.windowHeight = self.$modalInfoWindow.height();

    //events
    $('.save-article:not(.remove-only)').click(function (e) { //occurs on results and article pages
      e.preventDefault();
      self.toggleSave($(this));
    });

    $('.save-article.remove-only').click(function (e) { //occurs on saved items page
      e.preventDefault();
      self.confirmRemoval($(this));
    });

    self.$optionEdit.click(function (e) {
      self.editClick($(this));
    });

    self.$optionShare.click(function (e) {
      self.shareClick($(this));
    });

    self.$optionEmail.click(function (e) {
      self.emailClick($(this));
    });

    self.$optionClearAll.click(function (e) {
      self.removeAllClick($(this));
    });

  }; //end init

  self.toggleSave = function ($saveButton) { //save

    var isSaved = $saveButton.hasClass('saved');
    var isFull = $saveButton.hasClass('full');

    //captures whether the button is clicked from an individual article page or from saved articles page. 
    //Values will be either "individual" or "multi"
    var listType = $saveButton.attr('data-list-type');

    var articleID; //PL-INT - determine what info we need to pass here to the remove function

    if (listType == "individual") {
      articleID = $saveButton.closest('#article-content').attr('data-article-id');
    } else if (listType == "multi") { //article is being removed from a multi article listing
      articleID = $saveButton.closest('.article-item');
    }

    if (isSaved) { //unsave the article
      $saveButton.removeClass('saved');
      if (isFull) {
        $saveButton.text("+ Add article to my list");
      } else {
        $saveButton.text("x");
      }

      self.removeArticleFromSavedList(articleID, listType);
    } else { //save the article
      $saveButton.addClass('saved');
      if (isFull) {
        $saveButton.text("- Remove article from my list");
      } else {
        $saveButton.text("y");
      }
      self.addArticleToSavedList(articleID, listType);
    }
  }; //end toggleSave

  self.editClick = function ($currentButton) {
    var isActive = $currentButton.hasClass('active');

    if (isActive) {

      self.clearOptions($currentButton);

    } else {

      self.activateOptions($currentButton);
      siteContentClass.hideModalWindow(null, true); //callback, remove

    }

  }; //end editClick

  self.shareClick = function ($currentButton) {
    var isActive = $currentButton.hasClass('active');

    if (isActive) { //hide share modal and remove active class

      self.clearOptions($currentButton);
      siteContentClass.hideModalWindow(null, true); //callback, remove

    } else { //show the share modal

      self.activateOptions($currentButton);
      self.shareList('social');

    }

  }; //end shareClick

  self.emailClick = function ($currentButton) {
    var isActive = $currentButton.hasClass('active');

    if (isActive) { //hide modal and remove active class

      self.clearOptions($currentButton);

    } else { //share the list via email

      self.activateOptions($currentButton);
      self.shareList('email');

    }

  }; //end emailClick

  self.removeAllClick = function ($currentButton) {
    var isActive = $currentButton.hasClass('active');

    if (isActive) { //hide modal and remove active class

      self.clearOptions($currentButton);
      siteContentClass.hideModalWindow(null, true); //callback, remove

    } else { //share the list via email

      self.activateOptions($currentButton);
      siteContentClass.showModalWindow(shareClass.removeAllConfirmationShown);

    }


  };

  self.clearOptions = function ($currentButton) {

    $currentButton.removeClass('active');
    self.$articleItems.removeClass('active');

  }; //end clearOptions

  self.activateOptions = function ($currentButton) {

    self.$savedListOptions.find('.active').removeClass('active');
    $currentButton.addClass('active');

    var optionMethod = $currentButton.attr('data-method');

    switch (optionMethod) {
      case 'edit':
        self.$articleItems.addClass('active');
        break;
      case 'share':
      case 'clear-all':
      case 'email':
        self.$articleItems.removeClass('active');
        break;
    }

  }; //end activateOptions

  self.shareList = function (shareMethod) {
    var articlesToShare = [];

    $('.article-item').each(function (i) {
      var articleID = $(this).attr('data-article-id');
      articlesToShare[i] = articleID;
    });

    if (shareMethod == "email") {

      //PL-INT - Need to pass these article IDs (articlesToShare) to a share function
      siteContentClass.hideModalWindow(null, true); //callback, remove
      //self.clearOptions(self.$option-email);

    } else if (shareMethod == "social") {

      var options = {};
      options.articlesToShare = articlesToShare;
      siteContentClass.showModalWindow(shareClass.shareModalShown, options);

    }

  }; //end shareList

  self.shareModalShown = function (options) {

    $.ajax({
      url: "ajax/share-methods.html"
    }).done(function (data) {
        self.$modalInfoWindow.find(".modal-content").html(data);
        self.addShareCloseListener('clear', self.$optionShare); //parameter indicates we should clear the buttons of their active state
        self.addShareClickListener(options);
      });

  };

  self.addShareClickListener = function (options) {

    self.$modalInfoWindow.find('.share-method').click(function (e) {

      e.preventDefault();
      var shareMethod = $(this).attr('data-method');
      self.shareArticlesOnNetwork(options, method);
      siteContentClass.hideModalWindow(null, true); //callback, remove
      self.clearOptions(self.$optionShare);

    });

  };

  self.shareArticlesOnNetwork = function (options, method) {

    var articlesToShare = options.articlesToShare;
    //PL-INT - add share functionality here. This function provides the array of articles and site/network name to be shared.

  };

  self.addShareCloseListener = function (method, $button) { //clear or maintain - clear removes the share option button ($button)'s active state
    self.$modalInfoWindow.find('.close').one('click', function (e) { //enable close functionality
      e.preventDefault();
      siteContentClass.hideModalWindow(null, true); //callback, remove

      if (method == "clear") {
        self.clearOptions($button);
      }

    });
  };

  self.addArticleToSavedList = function (articleID, listType) { //occurs on results and article pages

    //PL-INT - insert article save logic here

  }; //end addArticleToSavedList

  self.confirmRemoval = function ($saveButton) { //occurs on the saved items page
    var listType = $saveButton.attr('data-list-type');
    var articleID = $saveButton.closest('.article-item').attr('data-article-id');
    var options = {};

    options.listType = listType;
    options.articleID = articleID;

    siteContentClass.showModalWindow(shareClass.removalConfirmationShown, options); //callback, options

  }; //end confirmRemoval

  self.removalConfirmationShown = function (options) {

    $.ajax({
      url: "ajax/confirm-article-removal.html"
    }).done(function (data) {
        self.$modalInfoWindow.find(".modal-content").html(data);
        self.addRemovalListeners(options);
        self.addShareCloseListener('maintain');
      });

  }; //end removalConfirmationShown

  self.removeAllConfirmationShown = function () { //called when user wants to clear entire saved articles list
    $.ajax({
      url: "ajax/confirm-articles-removal.html"
    }).done(function (data) {
        self.$modalInfoWindow.find(".modal-content").html(data);
        self.addRemovalAllListener();
        self.addShareCloseListener('clear', self.$optionClearAll);
      });
  };

  self.addRemovalListeners = function (options) {

    self.$modalInfoWindow.find('#remove-article').one('click', function (e) { //enable close functionality
      e.preventDefault();
      self.removeArticleFromSavedList(options.articleID, options.listType);
      siteContentClass.hideModalWindow(null, true); //callback, remove
    });

  }; //end addRemovalListeners

  self.addRemovalAllListener = function (options) {

    self.$modalInfoWindow.find('#remove-article').one('click', function (e) { //enable close functionality
      e.preventDefault();
      self.removeAllArticles();
    });

  }; //end addRemovalListeners

  self.removeAllArticles = function () {

    //PL-INT - insert remove all articles logic here

    self.$articleItems.empty();
    self.$savedAmt.text('0');
    siteContentClass.hideModalWindow(null, true); //callback, remove
    self.clearOptions(self.$optionClearAll);
    self.disableOptions();

  }; //end removeAllArticles

  self.removeArticleFromSavedList = function (articleID, listType) { //occurs on results and article pages

    if (listType == "remove") {
      $('.article-item[data-article-id="' + articleID + '"]').remove(); //remove article physically from the DOM

      //change the text showing the amount of articles saved
      var numArticlesString = self.$savedAmt.text();
      var numArticles = parseInt(numArticlesString);
      numArticles--;

      self.$savedAmt.text(numArticles);

      if (numArticles == 0) {
        self.disableOptions();
      }

    }

    //PL-INT - insert article removal logic here

  }; //end removeArticleFromSavedList

  self.disableOptions = function () {

    self.$savedListOptions.addClass('inactive');
    self.$optionEdit.off('click');
    self.$optionShare.off('click');
    self.$optionEmail.off('click');
    self.$optionClearAll.off('click');

    //PL-INT Ideally this function should be called upon entering this page if there are no saved items, so that share / remove buttons cannot be clicked

  }

};


var shareClass;

$(document).ready(function () {

  shareClass = new ShareContent();
  shareClass.init();

});