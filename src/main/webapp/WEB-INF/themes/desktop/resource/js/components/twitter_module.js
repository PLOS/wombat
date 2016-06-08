var TwitterModule;

(function ($) {
  TwitterModule = Class.extend({
    $listEl: $('#tweetList'),
    $containerEl: $('.twitter-container'),
    init: function () {
      this.loadData();
    },

    loadData: function () {
      var that = this;

      var query = new AlmQuery();

      query.getArticleTweets(ArticleData.doi)
          .then(function (articleData) {
            if (articleData[0].sources[0] && articleData[0].sources[0].events) {
              return articleData[0].sources[0].events;
            }
            else {
              throw new ErrorFactory('NoTwitterDataError', '[TwitterModule::loadData] - No twitter data available');
            }
          })
          .then(function (twitterData) {
            var itemTemplate = _.template($('#twitterModuleItemTemplate').html());
            twitterData = _.map(twitterData, function (item) {
              item = item.event;
              item.text = that.addTweetTextLinks(item.text);
              item.created_at = moment(item.created_at).format('D MMM YYYY');

              var tweetAvatar = item.user_profile_image;
              var tweetAvatarParse = tweetAvatar.slice(7, 9);
              if (tweetAvatarParse === "a0") {
                item.user_profile_image = "http://pbs" + tweetAvatar.slice(9);
              }

              return item;
            });
            var templateCompiled = itemTemplate({items: twitterData});

            that.$listEl.html(templateCompiled);
            that.onImageFailEventBind();

            if (twitterData.length > 5) {
              that.$listEl.find('li:gt(4)').hide();
              var showMoreButton = that.$containerEl.find('.load-more');
              var viewAllButton = that.$containerEl.find('.view-all');
              showMoreButton.on('click', function () {
                that.$listEl.find('li').show();
                viewAllButton.show();
                showMoreButton.hide();
              });
              showMoreButton.show();
            }

            that.$containerEl.show();
          })
          .fail(function (error) {
            console.log(error);
          });
    },

    onImageFailEventBind: function () {
      var avatarPlaceholder = WombatConfig.imgPath + 'icon.avatar.placeholder.png';
      this.$containerEl.find('.imgLoad').on('error', function () {
        var newImage = $(this).attr('src', avatarPlaceholder);
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