var TwitterModule;

(function ($) {
  TwitterModule = Class.extend({
    $listEl: $('#tweetList'),
    $containerEl: $('.twitter-container'),
    $showMoreButtonEl: null,
    $viewAllButtonEl: null,

    init: function () {
      this.$showMoreButtonEl = this.$containerEl.find('.load-more');
      this.$viewAllButtonEl = this.$containerEl.find('.view-all');

      this.loadData();
    },

    loadData: function () {
      var that = this;

      var query = new AlmQuery();


      query.getArticleTweets(ArticleData.doi)
          .then(function (articleData) {
            // Check if we have the twitter source and also if it has events
            if (articleData[0].sources[0] && articleData[0].sources[0].events.length) {
              return articleData[0].sources[0].events;
            }
            else {
              throw new ErrorFactory('NoTwitterDataError', '[TwitterModule::loadData] - No twitter data available');
            }
          })
          .then(function (twitterData) {
            var itemTemplate = _.template($('#twitterModuleItemTemplate').html());
            // Map twitter data to be in the template pattern
            twitterData = _.map(twitterData, function (item) {
              item = item.event;
              item.text = that.addTweetTextLinks(item.text);
              var momentDate = moment(item.created_at);
              item.timestamp = momentDate.valueOf();
              item.created_at = momentDate.format('D MMM YYYY');

              // Change the profile pic domain if is an old one
              var tweetAvatar = item.user_profile_image;
              var tweetAvatarParse = tweetAvatar.slice(7, 9);
              if (tweetAvatarParse === "a0") {
                item.user_profile_image = "http://pbs" + tweetAvatar.slice(9);
              }

              return item;
            });

            // Sort by date and descend it
            twitterData = _.sortBy(twitterData, function (item) {
              return item.timestamp;
            });
            twitterData = _.last(twitterData.reverse(), 10);
            console.log(twitterData);
            var templateCompiled = itemTemplate({items: twitterData});

            that.$listEl.html(templateCompiled);
            // Need to bind this event after the element is loaded because the 'error' event do not allow data binding.
            that.bindImageFailEvent();

            // If there is more then 5 tweets, we show the 'show more button' and bind the on click event.
            if (twitterData.length > 5) {
              that.bindShowMoreEvent();
            }

            that.$containerEl.show();
          })
          .fail(function (error) {
            // As the module is hidden by default, we just log the error.
            console.log(error);
          });
    },

    bindShowMoreEvent: function () {
      var that = this;
      this.$listEl.find('li:gt(4)').hide();
      this.$showMoreButtonEl.on('click', function () {
        that.$listEl.find('li').show();
        that.$viewAllButtonEl.show();
        that.$showMoreButtonEl.hide();
      });
      this.$showMoreButtonEl.show();
    },

    bindImageFailEvent: function () {
      // If the image fails to load, we load a placeholder instead.
      var avatarPlaceholder = WombatConfig.imgPath + 'icon.avatar.placeholder.png';
      this.$containerEl.find('.imgLoad').on('error', function () {
        var newImage = $(this).attr('src', avatarPlaceholder);
        // Just change the src for the image do not work, so we need to change the parent html.
        $(this).parent().html(newImage);
      });
    },

    addTweetTextLinks: function (tweetText) {
      //Add an extra space so we capture urls/tags/usernames that end on the final character
      tweetText = tweetText + " ";

      //Replace URLs with a real link
      var urlRegex = /((ht|f)tp(s?):\/\/[a-zA-Z0-9\-\.\/]+?)([\s])/g;
      var newValue = tweetText.replace(urlRegex, function (match, url, eol, offset, original) {
        return "<a href=\"" + url + "\">" + url + "</a> ";
      });

      //Replace tags with a link to a search for the tag
      var tagRegex = /#([A-Za-z0-9/-]+?)([:\s])/g;
      newValue = newValue.replace(tagRegex, function (match, tag, spacer, offset, original) {
        return "<a href=\"https://twitter.com/#!/search/%23" + tag + "\">#" + tag + "</a>" + spacer;
      });

      //Replace Retweet username with a link to the user profile
      var usernameRegex = /@([A-Za-z0-9_]+)([:\s])/g;
      newValue = newValue.replace(usernameRegex, function (match, username, spacer, offset, original) {
        return "<a href=\"https://twitter.com/" + username + "\">@" + username + "</a>" + spacer;
      });

      return newValue;
    }

  });

  new TwitterModule();
})(jQuery);