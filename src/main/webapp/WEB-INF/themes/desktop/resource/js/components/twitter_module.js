
(function ($) {
  $.fn.twitter = function (doi) {

    function validateDOI(doi) {
      if (doi == null) {
        throw new Error('DOI is null.');
      }

      doi = encodeURI(doi);

      return doi.replace(new RegExp('/', 'g'), '%2F').replace(new RegExp(':', 'g'), '%3A');
    }


   /* function getCitesTwitterOnly(doi) {
      doi = validateDOI(doi);

      var request = doi + '&source=twitter&info=event';

      var config = ALM_CONFIG;

      var requestUrl = 'http://alm.plos.org/api/v3/articles?api_key=' + config.apiKey + '&ids=' + request;

      $.ajax({
          url: requestUrl,
          json: 'callback',
          dataType: 'json',
          success: function(responseText) {
            console.log(responseText.data[0].sources[0]);
            whata = responseText.data[0].sources[0]
            return whata; //responseText.data[0].sources[0];
          },
        failure: function () {
          console.log('ALM request failed: ' + errorText+ " "+ url);
          $('#twitter-alm-timeline').append(errorText);
        }
      });
    }*/


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
          $('#twitter-alm-timeline').append(errorText);
        }
      });

    }




    var showTweetsArticleSidebar = function (stuff) {



      var twitterResponse, events, totalEventCount, minDisplayEventCount, maxDisplayEventCount, ol, i,
        tweet, created_dt, li, tweetText, div, ul, replyLink, reTweetLink, favoriteLink, tweetPostTime,
        linkToTweet;

      minDisplayEventCount = 2;
      maxDisplayEventCount = 5;

      //assuming here the request was properly formatted to get only twitter in sources[0]
      twitterResponse = stuff;

      if (twitterResponse && twitterResponse.metrics.total > 0) {
        $('#twitter-alm-timeline').append(twitterResponse.events[0].event.text);
      }

      /*<li class="tweet-entry display"><div class="tweet-user"><div class="twitter-posttime"><a href="https://twitter.com/Secular_Respons/statuses/512069241311592448">16 Sep</a></div><a href="https://twitter.com/Secular_Respons"><img src="http://pbs.twimg.com/profile_images/484591619232903170/szpq-wiD_normal.jpeg"><div><span class="twitter-fullname">Secular Response</span><span class="twitter-username">@Secular_Respons</span></div></a></div><div class="tweet-text">Mentalizing Deficits Constrain Belief in a Personal #God. Higher <a href="https://twitter.com/#!/search/%23autism">#autism</a> scores predictive of lower belief.
       <a href="http://t.co/ihbbVVT7nu">http://t.co/ihbbVVT7nu</a> </div><div><ul class="tweet-actions"><li><a href="https://twitter.com/intent/favorite?in_reply_to=512069241311592448" class="tweet-reply-action" title="Reply"><span></span>Reply</a></li><li><a href="https://twitter.com/intent/retweet?tweet_id=512069241311592448" class="tweet-retweet-action" title="Retweet"><span></span>Retweet</a></li><li><a href="https://twitter.com/intent/favorite?tweet_id=512069241311592448" class="tweet-favorite-action" title="Favorite"><span></span>Favorite</a></li></ul></div></li>*/
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