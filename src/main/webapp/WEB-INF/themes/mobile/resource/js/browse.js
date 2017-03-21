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

//contains scripts necessary to browse taxonomy

var AmbraBrowse = function () {

  var self = this;

  self.init = function () {
    //global DOM references
    self.$browseBackHeader = $('#browse-back-header');
    self.$browseBackButton = self.$browseBackHeader.find('.back');
    self.$topicName = self.$browseBackButton.find('#topic-name');
    self.$browseContainer = $('#browse-container');

    //global variables
    self.currentLevel = 1; //stores how deep we are in the taxonomy
    self.levelName;
    self.previousLevelName;

    //events
    self.bindBrowseForward(); //enable forward navigation on all currently loaded links
    self.bindBrowseBack();

  };

  self.browseForward = function ($browseLink) {

    var $browseItem = $browseLink.parent('li');
    var $currentLevel = $browseItem.closest('.browse-level');

    var categoryID = $browseItem.attr('data-category'); //PL-INT determine what information needs to be captured here
    self.levelName = $browseItem.find('.browse-link').text();
    self.previousLevelName = $currentLevel.attr("data-name");

    self.currentLevel = parseInt($currentLevel.attr('data-level'));
    var levelUp = self.currentLevel + 1;

    var hasLoaded = $browseLink.hasClass('has-loaded');

    if (hasLoaded) { //if we've loaded this content before, we just need to show it

      $targetLevel = $('.browse-level[data-level="' + levelUp + '"]');

      $currentLevel.animate({'right': '100%'}, 200, function () { //out with the old

        self.animateForward($currentLevel, $targetLevel);

      });

    } else { //we need to load it, then show it. Also need to make sure any succeeding level is removed

      self.removeLevel(levelUp);

      $browseLink.addClass('has-loaded');
      $targetLevel = $('<div class="browse-level">'); //create a new browse level element
      $targetLevel.attr({'data-level': levelUp, 'data-parent': categoryID}); //new div will be one level deeper
      $targetLevel.css({'right': '-100%'}); //align it off screen right for now

      //PL-INT - logic to retrieve necessary information about current browsing location and content to be loaded goes here

      $.ajax({
        url: "temp/ajax/browse-level.html?" + categoryID
      }).done(function (data) {
          $targetLevel.html(data);

          $currentLevel.animate({'right': '100%'}, 200, function () { //push old level off screen

            self.$browseContainer.append($targetLevel);
            self.animateForward($currentLevel, $targetLevel);

          });

        });

    } //end hasLoaded

  }; // end browseForward

  self.animateForward = function ($currentLevel, $targetLevel) {

    var removalLevel = self.currentLevel - 1;

    $targetLevel.addClass('active'); //makes it visible (display:block)
    $currentLevel.removeClass('active');
    self.$browseBackHeader.show(); //make sure the header is shown if not already

    $targetLevel.animate({'right': '0%'}, 200, function () { //bring in new level

      self.removeLevel(removalLevel); //get rid of anything more than one level back

      self.currentLevel = self.currentLevel + 1;
      self.updateLevelInfo($targetLevel);

    });

  };

  self.removeLevel = function (targetLevel) {

    var $removeLevel = $('.browse-level[data-level="' + targetLevel + '"]');
    if ($removeLevel.length >= 1) {
      $removeLevel.remove();
    }

  };

  self.browseBack = function () {

    var levelDown = self.currentLevel - 1;

    if (levelDown == 1) { //can't go back any further

      self.$browseBackHeader.hide(); //hide the back button

    }

    var $targetLevel = $('.browse-level[data-level="' + levelDown + '"]');
    var $currentLevel = $('.browse-level[data-level="' + self.currentLevel + '"]');

    self.levelName = $targetLevel.attr("data-name");

    $currentLevel.animate({'right': '-100%'}, 200, function () {

      self.animateBack($currentLevel, $targetLevel);

    });

  }; //end browseBack

  self.animateBack = function ($currentLevel, $targetLevel) {

    var removalLevel = self.currentLevel + 1;

    $targetLevel.addClass('active');
    $currentLevel.removeClass('active');

    $targetLevel.animate({'right': '0%'}, 200, function () { //bring in new level

      self.removeLevel(removalLevel);
      self.currentLevel = self.currentLevel - 1;
      //console.log('current level: ' + self.currentLevel);

      //load the parent, if it exists
      var parentID = $targetLevel.attr('data-parent');

      if (typeof parentID != 'undefined') { //If we're not at the root level

        var $parentLevel = $('<div class="browse-level">');
        var levelDown = self.currentLevel - 1;

        self.previousLevelName = "FPO Level Name"; //PL-INT - This will need to be set dynamically, perhaps by passing the parentID variable to determine its parent's name (see below)
        self.updateLevelInfo($targetLevel);

        $.ajax({
          url: "temp/ajax/browse-level.html?" + parentID
        }).done(function (data) {

            $parentLevel.html(data);
            $parentLevel.css({'right': '100%'});
            $parentLevel.attr({'data-level': levelDown });

            if (levelDown > 1) { //if it's not the root level

              //PL-INT - need to append parent here. Note that we are faking this logic with a static value, but it should be resolved by PLOS upon integration
              //PL-INT - supposedly what you have here is a lookup based of the parent level's parent, based on variable parentID
              $parentLevel.attr({'data-parent': '00'});

            }

            self.$browseContainer.prepend($parentLevel);

          });

      } else {
        //insert logic for arriving at the main menu
      }

    });

  };

  self.updateLevelInfo = function ($targetLevel) {
    $targetLevel.attr({'data-name': self.levelName }); //update the browse level with the current level name
    self.$topicName.text(self.previousLevelName); //update back button with previous level name
    //console.log('prev: ' + self.previousLevelName + ', current: ' + self.levelName);
  };

  self.bindBrowseForward = function () {

    self.$browseContainer.click('.browse-right:not(.inactive)', function (e) {
      e.preventDefault();
      self.browseForward($(this));
    });

  };

  self.bindBrowseBack = function () {

    self.$browseBackButton.click(function (e) {
      e.preventDefault();
      self.browseBack();
    });

  }

};

var ambraBrowse;

$(document).ready(function () {

  ambraBrowse = new AmbraBrowse();
  ambraBrowse.init();

});
