
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



      var events, totalTweets, minDisplayEventCount, maxDisplayEventCount, ol, i,
        tweet, created_dt, li, tweetText, div, ul, replyLink, reTweetLink, favoriteLink,        linkToTweet,
      tweetImg,
        tweetImgSwitch,
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
        tweetAvatarSrc;

      minDisplayEventCount = 2;
      maxDisplayEventCount = 5;

      totalTweets = almTweetData.metrics.total;
      tweetDate = dateParse(almTweetData.events[0].event.created_at, true);
      tweetAvatar = almTweetData.events[0].event.user_profile_image;
      tweetPlaceholder = 'resource/img/icon.avatar.placeholder.png';
      /*var checkAvatar = function (){
        var image = new Image();
       // if (image.onerror()===true){console.log('error');}
        image.onload = function () {
          tweetAvatarSrc = tweetAvatar;
          console.log(tweetAvatarSrc);
         //return tweetAvatarSrc;
        }
        image.onerror = function () {
          console.log(this);
         tweetAvatarSrc = tweetPlaceholder;
          console.log(tweetAvatarSrc);
          return tweetAvatarSrc
        }
        image.src = tweetAvatar;
      }
       checkAvatar();*/
      /*  $(tweetAvatar).on('load',function(){
          tweetAvatarSrc = tweetAvatar;
        }).on('error', function(){
          tweetAvatarSrc = tweetPlaceholder;
          console.log(tweetAvatarSrc);
        })*/
      // user photo, date of post, user names
      tweetInfo = '<a href="http://twitter.com/'+almTweetData.events[0].event.user+'"'+'>' +
      '<img src="'+tweetPlaceholder+'"/>' +
      '<div class="tweetDate">'+tweetDate+'</div>' +
      '<div class="tweetUser"><span>'+almTweetData.events[0].event.user_name+'</span> ' +
        almTweetData.events[0].event.user+'</div></a>';

      // actual tweet content
      tweetText = linkify(almTweetData.events[0].event.text);

      //twitter reply/retweet/favorite links
      tweetActionLink = 'https://twitter.com/intent/';
      tweetId = almTweetData.events[0].event.id;
      replyLink   = tweetActionLink + 'favorite?in_reply_to&tweet_id=' + tweetId;
      retweetLink = tweetActionLink + 'retweet?tweet_id='+ tweetId;
      favoriteLink =  tweetActionLink + 'favorite?tweet_id=' + tweetId;

      tweetActions = '<a class="tweet-reply" href="'+ replyLink + '"><div>&nbsp;</div>Reply</a>' +
        '<a class="tweet-retweet" href="' + retweetLink + '"><div>&nbsp;</div>Retweet</a>'+
        '<a class="tweet-favorite" href="' + favoriteLink + '"><div>&nbsp;</div>Favorite</a>';

    // tweetUserLink = '<a href="http://twitter.com/'+almTweetData.events[0].event.user+'"';
      tweet =
        '<ul class="tweetList"><li>' +
        '<div class="tweet-info">'+tweetInfo+'</div>' +
        '<div class="tweetText">'+tweetText+'</div>' +
          '<div id="tweetActions">' + tweetActions + '</div>'+
          '</li></ul>';

      if (almTweetData && almTweetData.metrics.total > 0) {
        $('#twitter-aside').append(tweet);
      }
     /* $('.tweetList #tweetActions').hover(function(){
         this.addClass('active');
        this.removeClass('active');
      });*/

      /*<li class="tweet-entry display"><div class="tweet-user"><div class="twitter-posttime"><a href="https://twitter.com/Secular_Respons/statuses/512069241311592448">16 Sep</a></div><a href="https://twitter.com/Secular_Respons"><img src="http://pbs.twimg.com/profile_images/484591619232903170/szpq-wiD_normal.jpeg"><div><span class="twitter-fullname">Secular Response</span><span class="twitter-username">@Secular_Respons</span></div></a></div><div class="tweet-text">Mentalizing Deficits Constrain Belief in a Personal #God. Higher <a href="https://twitter.com/#!/search/%23autism">#autism</a> scores predictive of lower belief.
       <a href="http://t.co/ihbbVVT7nu">http://t.co/ihbbVVT7nu</a> </div><div><ul class="tweet-actions">
       <li><a href="https://twitter.com/intent/favorite?in_reply_to=512069241311592448" class="tweet-reply-action" title="Reply"><span></span>Reply</a></li>
       <li><a href="https://twitter.com/intent/retweet?tweet_id=512069241311592448" class="tweet-retweet-action" title="Retweet"><span></span>Retweet</a></li>
       <li><a href="https://twitter.com/intent/favorite?tweet_id=512069241311592448" class="tweet-favorite-action" title="Favorite"><span></span>Favorite</a></li></ul></div></li>*/
      /*  events = twitterResponse.events;
        events = events.sort(jQuery.proxy(this.sort_tweets_by_date, this));

        totalEventCount = (events.length > maxDisplayEventCount) ? maxDisplayEventCount : events.length;

        ol = $('<ol></ol>');

        for (i = 0; i < totalEventCount; i++) {
          tweet = events[i].event;

          created_dt = isNaN(Date.parse(tweet.created_at)) ?
            $.datepicker.formatDate('M d, yy', this.parseTwitterDate(tweet.created_at)) :
            $.datepicker.formatDate('M d, yy', new Date(tweet.created_at));

          if (i < minDisplayEventCount) {
            li = $('<li></li>').addClass('tweet-entry display');
          } else {
            li = $('<li></li>').addClass('tweet-entry hide');
          }

          li.hover(
            function () {
              // in
              $(this).find('ul.tweet-actions').css('visibility', 'visible');
            },
            function () {
              // out
              $(this).find('ul.tweet-actions').css('visibility', 'hidden');
            });

          tweetPostTime = this.getTweetTimeDisplay(tweet.created_at);
          linkToTweet = "<a href=\"https://twitter.com/" + tweet.user + "/statuses/" + tweet.id + "\">" + tweetPostTime + "</a>";

          div = $('<div></div>')
            .addClass('tweet-user')
            .append($('<div></div>')
              .addClass('twitter-posttime')
              .html(linkToTweet))
            .append($('<a></a>')
              .attr('href', 'https://twitter.com/' + tweet.user)
              .append($('<img>')
                .attr('src', tweet.user_profile_image))
              .append($('<div></div>')
                .append($('<span></span>')
                  .addClass('twitter-fullname')
                  .html(tweet.user_name))
                .append($('<span></span>')
                  .addClass('twitter-username')
                  .html('@' + tweet.user))));
          li.append(div);

          tweetText = $('<div></div>')
            .addClass('tweet-text')
            .html(this.linkify(tweet.text));
          li.append(tweetText);

          replyLink = $('<a></a>')
            .attr('href', 'https://twitter.com/intent/favorite?in_reply_to=' + tweet.id)
            .addClass('tweet-reply-action')
            .attr('title', 'Reply')
            .html('<span></span>Reply');

          reTweetLink = $('<a></a>')
            .attr('href', 'https://twitter.com/intent/retweet?tweet_id=' + tweet.id)
            .addClass('tweet-retweet-action')
            .attr('title', 'Retweet')
            .html('<span></span>Retweet');

          favoriteLink = $('<a></a>')
            .attr('href', 'https://twitter.com/intent/favorite?tweet_id=' + tweet.id)
            .addClass('tweet-favorite-action')
            .attr('title', 'Favorite')
            .html('<span></span>Favorite');

          ul = $('<ul></ul>').addClass('tweet-actions')
            .append($('<li></li>').append(replyLink))
            .append($('<li></li>').append(reTweetLink))
            .append($('<li></li>').append(favoriteLink));

          div = $('<div></div>').append(ul);
          li.append(div);

          ol.append(li);
        }

        $('#twitter-alm-timeline').append(ol);

       // var doi = encodeURI($('meta[name=dc.identifier]').attr('content'));
        if (events.length > minDisplayEventCount) {
          $('#twitter-alm-timeline').append(
            $('<button></button>')
              .html('Load More')
              .click(function () {
                $('#twitter-alm-timeline li.tweet-entry.hide').removeClass('hide').addClass('display');
                var a = $('<a></a>').attr('href', '/article/twitter/info:doi/' + doi).html('View all tweets');
                $(this).html('').append(a);
                $(this).css('background-image', 'none');
              }
            )
          );
        } else {
          $('#twitter-alm-timeline').append(
            $('<button></button>').append($('<a></a>').attr('href', '/article/twitter/info:doi/' + doi).html('View all tweets')).css('background-image', 'none')
          );
        }


      }*/
    }


 /*   this.getTweetTimeDisplay = function (dateString) {
      var tweetDate = isNaN(Date.parse(dateString)) ? this.parseTwitterDate(dateString) : new Date(dateString);
      var now = new Date().getTime();
      // difference in hours
      var timeDiff = Math.round((now - tweetDate) / 1000 / 60 / 60);
      var output = '';
      var yearInHours = 365 * 24;

      if (timeDiff > yearInHours) {
        output = $.datepicker.formatDate('d M y', tweetDate);
      } else
        if (timeDiff >= 24.0) {
          // day month
          output = $.datepicker.formatDate('d M', tweetDate);
        } else {
          if (timeDiff >= 1.0) {
            // hour
            output = Math.round(timeDiff) + 'h';
          } else {
            // minute
            timeDiff = (now - tweetDate) / 1000 / 60;
            if (timeDiff >= 1.0) {
              output = Math.round(timeDiff) + 'm';
            } else {
              // second
              timeDiff = (now - tweetDate) / 1000;
              output = Math.round(timeDiff) + 's';
            }
          }
        }

      return output;
    }*/

  };
})(jQuery);