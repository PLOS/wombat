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

    $('select[name="revisionLink"]').change( function(){
      var revisionLink = $(this).val();
      window.location.href = revisionLink;
    });

    //events
    $('#article-type-menu').find('li').click(function (e) {
      var $currentButton = $(this);
      self.switchArticleListMethod($currentButton);
    });

    $('.author-info').click(function (e) {
      e.preventDefault();
      self.showAuthorInfo($(this));
    });

    $('.author-more, .author-less').click(function (e) {
      e.preventDefault();
      self.toggleMoreAuthors($(this));
    });

    $('.filter-button').click(function (e) {
      self.toggleFilterButton($(this));
    });

    $('#display-options button').click(function (e) {
      self.setDisplayOption($(this));
    });

    $('.modal-tab').click(function (e) {
      e.preventDefault();
      self.hideModalTab($(this));
    });

    // attach event handlers for in-page links. this also wraps the handling
    // of showing references in the reference panel.
    $('a.xref').click(function (e) {
      self.navigateToInPageLink($(e.target));
    });

    var $collapsible = $('.collapsible');
    if ($collapsible.length) {
      $collapsible.collapsiblePanel();
    }

    // "Take me to the desktop site" link.  We link to the current page with an extra URL parameter.
    // Apache will detect this param, redirect to the corresponding desktop ambra page, and set a
    // session cookie such that the user will stay on the desktop site.
    $('#full-site-link').click(function(e) {
      var url = window.location.href;
      url += (url.indexOf('?') == -1) ? '?' : '&';
      url += 'fullSite';
      window.location.href = url;
    });
  }; //end init

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

  };

  self.showModalTab = function () {

    $('.modal-tab').css({'display': 'block'}).animate({'top': '-35px'}, 100);

  };

  self.figureModalShown = function () {

    self.$modalInfoWindow.find('.close').one('click', function (e) { //enable close functionality
      e.preventDefault();
      self.hideModalWindow(self.showModalTab, false, "tab"); //callback, removeContent
    });

  };

  self.switchArticleListMethod = function ($currentButton) { //switch between different methods of displaying article content

    var isActive = $currentButton.hasClass('active');

    if (!isActive) { //if it's not active find any buttons who are and disable them, then execute the proper list method
      $('#article-type-menu').find('li.active').removeClass('active');
      $currentButton.addClass('active');

      var listMethod = $currentButton.attr('data-method');
      $('#section').val(listMethod);
      $('#hpSectionForm').submit();
    }
  }; //end switchArticleListMethod

  self.toggleMoreAuthors = function ($clickedLink) {
    var $moreLink = $('.author-more');
    var $lessLink = $('.author-less');
    var $moreList = $('.more-authors-list');
    $moreLink.toggle();
    $moreList.toggle(100, function () {
      $lessLink.toggle();
    });
  };

  self.showAuthorInfo = function ($authorLink) {
    var authorID = $authorLink.attr('data-author-id'); //PL-INT - determine what info needs be captured here
    var $authorMeta = $('#author-meta-' + authorID);

    // There are some rare cases where we don't have enough info for authors to show
    // this.  If that's the case the div won't be available.
    if ($authorMeta.length == 1) {
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
    }
  }; //end showAuthorInfo

  self.authorModalShown = function (options) {
    var authorID = options.authorID;
    self.$modalInfoWindow.find('.close').one('click', function (e) { //enable close functionality
      e.preventDefault();
      self.hideModalWindow(null, true); //callback, remove
    });

    self.loadAuthorInfo(authorID);

  }; //end authorModalShown

  self.loadAuthorInfo = function (authorID) {
    var $authorMeta = $('#author-meta-' + authorID);
    self.$modalInfoWindow.find('.modal-content').html($authorMeta.html());

    // Set the "search for this author" link target.
    var authorName = $authorMeta.find('.author-full-name').html();
    $('.modal-search').attr('href', 'search?author=' + encodeURIComponent(authorName));
  };

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

  };

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

  }; //end hideModalWindow

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

  }; //end showModalWindow

  self.disableContentScrolling = function () {
    ambraNavigation.$containerMain.addClass('inactive');
  };

  self.enableContentScrolling = function () {
    ambraNavigation.$containerMain.removeClass('inactive');
  };

  self.toggleFilterButton = function ($filterButton) {
    var isActive = $filterButton.hasClass('active');
    var $filterBox = $('.filter-box');

    if (isActive) { //hide the filter box and remove active state

      $filterButton.removeClass('active');
      $filterBox.removeClass('active');
      self.resetFilterBox($filterBox);

    } else { //show filter box and add active state. Enable cancel and apply

      $filterButton.addClass('active');
      $filterBox.addClass('active');

      $filterBox.find('.cancel').one('click', function () {
        self.toggleFilterButton($filterButton);
      });
    }

  };

  self.setDisplayOption = function ($displayButton) {
    var isActive = $displayButton.hasClass('active');

    if (!isActive) { //if it's not active, activate it and toggle content visibility

      var $displayGroup = $displayButton.parent('.buttongroup');
      $displayGroup.find('.active').removeClass('active');
      $displayButton.addClass('active');

      var displayType = $displayButton.attr('data-type');
      $('#article-items').attr('class', displayType);

    }
  };

  self.showReference = function (ref_id) {
    // first off, close any open reference
    self.hideReference();

    // grab the parent LI element as it has the content we want to show
    var $ref = $(ref_id).parent();

    // generate the skeleton markup for the panel
    var ref_panel_markup = [
      "<div id='reference-panel'>",
      "<a class='close coloration-text-color'>v</a>",
      "<div id='ref-panel-content'>",
      "</div>",
      "</div>"
    ].join("\n");

    // append it to the DOM as a child of the main site wrapper
    $('#container-main').append($(ref_panel_markup));

    // append the content of the reference to the container in the panel
    $('#ref-panel-content').append($ref.html());

    // allow X to close the reference panel
    $("#reference-panel .close").click(self.hideReference);

    // attach a scroll handler to remove the element on scroll
    $(window).on('scroll', self.referenceScrollHandler);

  };

  self.hideReference = function () {
    // remove the ref panel from the DOM
    $('#reference-panel').remove();

    // be courteous and stop listening for scroll events when we don't need to
    $(window).off('scroll', self.referenceScrollHandler);
  };

  self.referenceScrollHandler = function () {
    // dispose of the panel immediately; no need to keep it upon scroll
    self.hideReference();
  };

  self.navigateToInPageLink = function ($clicked_el) {

    // grab the target href, which is the ID of the ref (duh).
    // note that the href is also already pre-formed for a DOM query for the 
    // element ID ('#test')
    var target_id = $clicked_el.attr('href');

    // remove period in system-generated ref ID (it conflicts with the class designator)
    var target_id_replaced = target_id.replace( /(:|\.|\[|\])/g, "\\$1" );
    var target_el = $( target_id_replaced);

    // handle special reference link case by testing to see if target_el is a 
    // child of the reference list
    var references_test = target_el.closest('ol.references');

    if (references_test.length) {
      this.showReference(target_id_replaced);

      // all other in-page links scroll to target
    } else {
      // figure out which accordion panel the target link is in.
      var panel_to_open = target_el.closest('li.accordion-item');


      // if accordion panel is already open, then 
      if (panel_to_open.hasClass('expanded')) {
        $('html, body').scrollTop(target_el.offset().top);

      } else {
        // close current accordion panel and open new accordion panel. pass 
        // toggleMainAccordion the 'a' element as that's what it expects.
        // pass target element to toggleMainAccordion as second arg to indicate 
        // we want to scroll to it
        ambraNavigation.toggleMainAccordion(panel_to_open.children('a.expander'), target_el);



      }
    }


  }
};


// Begin collapsible

// Collapsible div used on the 500 page (error.ftl) to hold the exception stacktrace.
// Based on code from http://www.darreningram.net/pages/examples/jQuery/CollapsiblePanelPlugin.aspx
// (No copyright statements there, but including for attribution.)

(function ($) {
  $.fn.extend({
    collapsiblePanel: function () {
      return $(this).each(ConfigureCollapsiblePanel);
    }
  });
})(jQuery);

function ConfigureCollapsiblePanel() {
  $(this).addClass("ui-widget");

  // Check if there are any child elements, if not then wrap the inner text within a new div.
  if ($(this).children().length == 0) {
    $(this).wrapInner("<div></div>");
  }

  // Wrap the contents of the container within a new div.
  $(this).children().wrapAll("<div class='collapsible-content'></div>");

  // Create a new div as the first item within the container.  Put the title of the panel in here.
  $("<div class='collapsible-title ui-widget-header'><div>" + $(this).attr("title") + "</div></div>").prependTo($(this));

  // Assign a call to CollapsibleContainerTitleOnClick for the click event of the new title div.
  $(".collapsible-title", this).click(CollapsibleContainerTitleOnClick);

  // Keep the widget closed initially.
  $(".collapsible-content", $(this).parent()).hide();
}

function CollapsibleContainerTitleOnClick() {

  // The item clicked is the title div... get this parent (the overall container) and toggle the content within it.
  $(".collapsible-content", $(this).parent()).slideToggle();
}

// End collapsible


var siteContentClass;

$(document).ready(function () {

  siteContentClass = new SiteContent();
  siteContentClass.init();

});
