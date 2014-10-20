
(function ($) {
  $.fn.twitter = function (doi) {

    function validateDOI(doi) {
      if (doi == null) {
        throw new Error('DOI is null.');
      }

      doi = encodeURI(doi);

      return doi.replace(new RegExp('/', 'g'), '%2F').replace(new RegExp(':', 'g'), '%3A');
    }

    this.displayTweetsArticleSidebar = function (doi) {
      doi = validateDOI(doi);

      var request = doi + '&source=twitter&info=event';

      var config = ALM_CONFIG;

      var requestUrl = 'http://alm.plos.org/api/v3/articles?api_key=' + config.apiKey + '&ids=' + request;
      var response = '';
      $.ajax({
        type: "GET",
        url: requestUrl,
        json: 'callback',
        dataType: 'json',
        success: function(responseText) {
          // console.log(responseText[0].sources[0].events[0].event.text);
          response = responseText[0].sources[0];//.events[0].event.text;

          return showTweetsArticleSidebar(response);
        },
        failure: function () {
          console.log('ALM request failed: ' + errorText+ " "+ url);
          $('#twitter-aside').append(errorText);
        }
      });

    };

    // linkify is from Ambra hallelujah
    var linkify = function (tweetText) {
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
    };

    var showTweetsArticleSidebar = function (almTweetData) {
      var tweet, tweetText, replyLink, retweetLink, favoriteLink,
        totalTweets, minDisplayTweets, maxDisplayTweets,
        tweetDate,
        tweetInfo,
        tweetActionLink,
        tweetAvatar,
        tweetPlaceholder,
        tweetId,
        replyLink,
        retweetLink,
        favoriteLink,
        tweetActions,
        tweetAvatar,
        tweetUserName,
        tweetHandle,
        dataPrefix,
        wholeTweet,
        listBody,
        tweetAvatarParse;

      totalTweets = almTweetData.metrics.total;
      minDisplayTweets = 2;
      maxDisplayTweets = 5;


      function showit(stuff, limity) {

        //get the data
        function dataPass(i) {
          dataPrefix = stuff[i].event;
          tweetDate = dateParse(dataPrefix.created_at, false, true);
          tweetAvatar = dataPrefix.user_profile_image;
          tweetUserName = dataPrefix.user_name;
          tweetHandle = dataPrefix.user;
          tweetText = linkify(dataPrefix.text);
          tweetId = dataPrefix.id;

          //fix twitImg url
          tweetAvatarParse = tweetAvatar.slice(7,9);

          if (tweetAvatarParse === "a0") {
            tweetAvatar = "http://pbs"+tweetAvatar.slice(9);
          }
          console.log(tweetAvatar);
          //http://a0.twimg.com/profile_images/1379367446/macarthur_olduvai_profile_cropped_normal.jpg
          // user photo, date of post, user names
          tweetPlaceholder = 'resource/img/icon.avatar.placeholder.png';
          tweetInfo = '<a href="http://twitter.com/' + tweetHandle + '"' + '>' +
            '<img src="' + tweetAvatar + '"/>' +
            '<div class="tweetDate">' + tweetDate + '</div>' +
            '<div class="tweetUser"><strong>' + tweetUserName + ' </strong><span>@' +
            tweetHandle + '</span></div></a>';

          //twitter reply/retweet/favorite links
          tweetActionLink = 'https://twitter.com/intent/';
          replyLink = tweetActionLink + 'tweet?in_reply_to' + tweetId;
          retweetLink = tweetActionLink + 'retweet?tweet_id=' + tweetId;
          favoriteLink = tweetActionLink + 'favorite?tweet_id=' + tweetId;

          tweetActions = '<a class="tweet-reply" href="' + replyLink + '"><div>&nbsp;</div>Reply</a>' +
            '<a class="tweet-retweet" href="' + retweetLink + '"><div>&nbsp;</div>Retweet</a>' +
            '<a class="tweet-favorite" href="' + favoriteLink + '"><div>&nbsp;</div>Favorite</a>';

          return listBody = '<div class="tweet-info">' + tweetInfo + '</div><div class="tweetText">' + tweetText + '</div>' +
            '<div id="tweetActions">' + tweetActions + '</div>';
        }

        for (var i=0; i < 2; i++){
          dataPass(i);
          wholeTweet = '<li>'+listBody+'</li>';
          $('#tweetList').append(wholeTweet);

        }

        for (var i=2; i <= 4; i++){
          dataPass(i);
          wholeTweet = '<li class="more-tweets">'+listBody+'</li>';
          $('#tweetList').append(wholeTweet);
          $('.load-more').css('display','block');
        }
      }
      $('.load-more').on('click', function(){
        $('.more-tweets').css('display','block');
        $(this).css('display','none');

        if (totalTweets > 5) {
          $('.view-all').css('display', 'block');
        }
      });

      if (almTweetData && totalTweets > 0) {
        //if totaltweets <= 5, limit = totaltweets
        //if totaltweets  > 5, limit = 5 & append li view all
        showit(almTweetData.events, totalTweets);
      }
    }
  };
})(jQuery);