(function ($) {
  $.fn.twitter = function (doi) {
    var tweet, tweetText, totalTweets, initData, minDisplayTweets, maxDisplayTweets, dataSort, datePrefix, dataPrefix, tweetDate, tweetDateOther, tweetInfo, tweetActionLink, tweetAvatar, tweetPlaceholder, tweetId, replyLink, retweetLink, favoriteLink, tweetActions, tweetUserName, tweetHandle, wholeTweet, listBody, tweetAvatarParse;

    function validateDOI(doi) {
      if (doi == null) {
        throw new Error('DOI is null.');
      }

      doi = encodeURI(doi);

      return doi.replace(new RegExp('/', 'g'), '%2F').replace(new RegExp(':', 'g'), '%3A');
    }

    this.getSidebarTweets = function (doi) {
      doi = validateDOI(doi);
      var config, requestUrl, errorText;

      config = ALM_CONFIG;

      requestUrl = config.host + '?api_key=' + config.apiKey + '&ids=' + doi + '&info=detail&source=twitter';

      errorText = '<li>Our system is having a bad day. We are working on it. Please check back later.</li>';

      $.ajax({
        url:         requestUrl,
        dataType:    'jsonp',
        contentType: "text/json; charset=utf-8",
        type:        "GET"
      }).done(function (data) {
        initData = data.data[0];
        if (initData.sources === undefined) {
          //do nothing
        } else {
          totalTweets = data.data[0].sources[0].metrics.total;
          minDisplayTweets = 2;
          maxDisplayTweets = 5;
          dataSort = initData.sources[0].events;

          //parse the date to be able to sort by date
          this.parseTwitterDate = function (tweetdate) {
            //running regex to grab everything after the time
            var newdate = tweetdate.replace(/(\d{1,2}[:]\d{2}[:]\d{2}) (.*)/, '$2 $1');
            //moving the time code to the end
            newdate = newdate.replace(/(\+\S+) (.*)/, '$2 $1');

            return new Date(Date.parse(newdate));
          }
          //sort by date from Ambra
          this.sort_tweets_by_date = function (a, b) {
            var aDt = isNaN(a.event.created_at) ? this.parseTwitterDate(a.event.created_at) : a.event.created_at;
            var bDt = isNaN(b.event.created_at) ? this.parseTwitterDate(b.event.created_at) : b.event.created_at;

            return (new Date(bDt).getTime()) - (new Date(aDt).getTime());
          }
          //pull the data & run the sort function
          dataSort = dataSort.sort(jQuery.proxy(this.sort_tweets_by_date, this));
          // only show 5, so cut the json results to 5
          if (dataSort.length > maxDisplayTweets) {
            dataSort = dataSort.slice(0, 5);
          } else { }

          $.each(dataSort, function (index) {
            dataPrefix = dataSort[index].event;
            datePrefix = dataSort[index];
            //run through dataPass to get all the data
            dataPass(dataPrefix, datePrefix);
            //show only 2 and then 5
            if (index < minDisplayTweets) {
              wholeTweet = '<li>' + listBody + '</li>';
            } else {
              wholeTweet = '<li class="more-tweets">' + listBody + '</li>';
            }

            $('#tweetList').append(wholeTweet);
            checkAvatar(listBody);

          });

          // and finally, display the tweets
          if (totalTweets > 0) {
            $('.twitter-container').css('display', 'block');
            // display the more tweets if there are any.
            if (totalTweets > minDisplayTweets) {
              var show_link = more_tweets();

            } else {
              // do nothing
            }
          } else {
            // do nothing
          }

        }
      }).fail(function () {
        $('.twitter-container').css('display', 'block');
        $('#tweetList').append(errorText);
      });

    };

    function checkAvatar(listappend) {
      var checkImg = $(listappend).find('.imgLoad');
      $(checkImg).on('error', changeAvatar);
    }

    function changeAvatar(event) {
      if (event) {
        var newthing = $(this).attr('src', tweetPlaceholder);
        return $('.imgholder').html(newthing);
      }
    }

    function dateFiddle(tweetDate) {
      var dateraw, dateoptions, prettydate, iedate, ugh, months, toNum;
      if (!document.all) {
        dateraw = new Date(tweetDate);
        dateoptions = {day: "numeric", month: "short", year: "numeric"};
        prettydate = dateraw.toLocaleString("en-GB", dateoptions);

      } else {  //alert(tweetDate.indexOf(","));
        iedate = tweetDate.toString();
        ugh = iedate.split(',');
        months = new Array();
        months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        toNum = ugh[1];

        prettydate = ugh[2] + ' ' + months[toNum] + ' ' + ugh[0];
      }
      return prettydate;
    }

    function dataPass(dataPrefix) {

      tweetDateOther = dataPrefix.created_at;//dateParse(dataPrefix.created_at, false, true, "en-GB");
      tweetDate = datePrefix.event_csl.issued['date-parts'];
      tweetAvatar = dataPrefix.user_profile_image;
      tweetUserName = dataPrefix.user_name;
      tweetHandle = dataPrefix.user;
      tweetText = linkify(dataPrefix.text);
      tweetId = dataPrefix.id;

      tweetDate = dateFiddle(tweetDate);
      //change twitter avatar url if an old one ("a0") is stored
      tweetAvatarParse = tweetAvatar.slice(7, 9);
      if (tweetAvatarParse === "a0") {
        tweetAvatar = "http://pbs" + tweetAvatar.slice(9);
      }
      // user photo, date of post, user names
      tweetPlaceholder = 'resource/img/icon.avatar.placeholder.png';
      // TODO: put in placeholder conditional
      tweetInfo = '<a href="http://twitter.com/' + tweetHandle + '"' + '>' + '<span class="imgholder"><img class="imgLoad" src="' + tweetAvatar + '"/></span>' + '<div class="tweetDate">' + tweetDate + '</div>' + '<div class="tweetUser"><strong>' + tweetUserName + ' </strong><span>@' + tweetHandle + '</span></div></a>';

      //twitter reply/retweet/favorite links
      tweetActionLink = 'https://twitter.com/intent/';
      replyLink = tweetActionLink + 'tweet?in_reply_to' + tweetId + '&text=@' + tweetHandle;
      retweetLink = tweetActionLink + 'retweet?tweet_id=' + tweetId;
      favoriteLink = tweetActionLink + 'favorite?tweet_id=' + tweetId;

      tweetActions = '<a class="tweet-reply" href="' + replyLink + '"><div>&nbsp;</div>Reply</a>' + '<a class="tweet-retweet" href="' + retweetLink + '"><div>&nbsp;</div>Retweet</a>' + '<a class="tweet-favorite" href="' + favoriteLink + '"><div>&nbsp;</div>Favorite</a>';

      return listBody = '<div class="tweet-info">' + tweetInfo + '</div><div class="tweetText">' + tweetText + '</div>' + '<div id="tweetActions">' + tweetActions + '</div>';

    }

    //show 'load more' and 'view all' links if necessary
    var more_tweets = function () {
      $('.load-more').css('display', 'block').on('click', function () {
        $('.more-tweets').css('display', 'block');
        $(this).css('display', 'none');
        if (totalTweets > maxDisplayTweets) {
          return $('.view-all').css('display', 'block');
        }
      });
    }

    // linkify is from Ambra; parse twitter body to add anchor tags and such.
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

  };
})(jQuery);