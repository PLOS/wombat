/*
 * $HeadURL:: http://ambraproject.org/svn/plos/templates/branches/figure-improvements/jo#$
 * $Id: init_twitter.js 10044 2012-05-17 23:39:03Z mbaehr $
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

$.fn.twitter = function () {
  this.displayTwitterCites = function (doi) {
    var alm = new $.fn.alm();
    alm.getCitesTwitterOnly(doi, jQuery.proxy(this.setTweets, this), jQuery.proxy(this.setError, this));
  }

  this.setError = function (xOptions, textStatus) {
    $("#twitterError").text("Our system is having a bad day. We are working on it. Please check back later.");
    $("#twitterError").show("blind", 1000);
  };

  this.setTweets = function (json, textStatus, xOptions) {
    window.tweetsResponse = json;

    $(".spinner").fadeOut(1000);

    $("#tweets").css("display", "none");

    this.showTweetsPage(0);

    $("#tweets").show("blind", 1000);
  };

  this.showTweetsPage = function (currentPage) {
    //console.log("Currentpage:" + currentPage);
    var json = window.tweetsResponse;
    var numTweets = 0;

    //assuming here the request was properly formatted to get only twitter in sources[0]
    var twitterResponse = json[0].sources[0];

    var pageSize = 50;

    $("#tweets").empty();

    if (twitterResponse && twitterResponse.metrics.total > 0) {

      var events = twitterResponse.events;
      events = events.sort(jQuery.proxy(this.sort_tweets_by_date, this));

      numTweets = events.length;

      // example: if currentPage is 2 and numTweets is 420
      // totalPages is 9 == ceil of 420/50
      // startIndex is 100, endIndex is 149.
      var totalPages = Math.ceil(numTweets / pageSize);
      var startIndex = currentPage * pageSize;

      // if last page has less than 50 tweets, use remaining tweets.
      var endIndex = Math.min(numTweets, startIndex + pageSize);

      var ol = $('<ol></ol>');

      for (var i = startIndex; i < endIndex; i++) {
        var tweet = events[i].event;
        var tweet_url = events[i].event_url;

        var created_dt = isNaN(Date.parse(tweet.created_at)) ?
            $.datepicker.formatDate("M d, yy", this.parseTwitterDate(tweet.created_at)) :
            $.datepicker.formatDate("M d, yy", new Date(tweet.created_at));

        ol.append("<li><div><img src=\"" + tweet.user_profile_image
            + "\"/><span class=\"text\"><a href=\"https://twitter.com/#!/" + tweet.user
            + "\">" + tweet.user + "</a> " + this.linkify(tweet.text) + "</span><br/><a target='_blank' href=\""
            + tweet_url + "\"><span class=\"text\">" + created_dt + "</span></a></div></li>");

      }

      ol.attr("start", currentPage * pageSize + 1);
      ol.css("counter-reset", "item " + (currentPage * pageSize));

      var pagination = this.paging(totalPages, currentPage);
      $("#tweets").append(pagination.clone(), ol, pagination);
    }

    var statusMsg = "";
    if (numTweets < 1) {
      statusMsg = "No tweets found";
    } else {
      var pluralization = "";
      if (numTweets > 1) { // This page should never be displayed if less than 1 citation.
        pluralization = "s";
      }

      statusMsg = numTweets + " tweet" + pluralization
          + " as recorded by Twitter.  Article published "
          + $.datepicker.formatDate("M d, yy", new Date(json[0].publication_date))
          + ". Tweets updated "
          + $.datepicker.formatDate("M d, yy", new Date(twitterResponse.update_date)) + ".";
    }

    $("#tweets").prepend(statusMsg);
  }

  this.parseTwitterDate = function (tweetdate) {
    //running regex to grab everything after the time
    var newdate = tweetdate.replace(/(\d{1,2}[:]\d{2}[:]\d{2}) (.*)/, '$2 $1');
    //moving the time code to the end
    newdate = newdate.replace(/(\+\S+) (.*)/, '$2 $1')

    return new Date(Date.parse(newdate));
  }

  this.sort_tweets_by_date = function (a, b) {
    var aDt = isNaN(a.event.created_at) ? this.parseTwitterDate(a.event.created_at) : a.event.created_at;
    var bDt = isNaN(b.event.created_at) ? this.parseTwitterDate(b.event.created_at) : b.event.created_at;

    return (new Date(bDt).getTime()) - (new Date(aDt).getTime());
  }

  this.linkify = function (tweetText) {
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

  this.paging = function (totalPages, currentPage) {
    var pagination = $("<div></div>");

    // no pagination if only one page
    if (totalPages > 1) {
      // otherwise adding pagination

      //the logic is same as in templates/journals/PLoSDefault/webapp/search/searchResults.ftl

      /*
       It supports the following use cases and will output the following:
       current page is zero based.
       page number = current page + 1.

       if current page is the start or end
       if current page is 0:
       < 1 2 3  ... 10 >
       if current page is 9:
       < 1 ...8 9 10 >

       if current page is 4:
       (Current page is greater then 2 pages away from start or end)
       < 1 ... 4 5 6 ... 10 >

       if current page is less then 2 pages away from start or end:
       current page is 7:
       < 1 ...7 8 9 10 >
       current page is 2:
       < 1 2 3 4 ... 10 >
       */

      pagination.attr("class", "pagination");

      var ellipsis = '<span>...</span>';
      var prev = '<span class="prev">&lt;</span>';
      var next = '<span class="next">&gt;</span>';

      if (totalPages < 4) {
        // if less than 4 pages, do not put "..."
        // put < with or without link depending on whether this is first page.
        if (currentPage > 0) {
          pagination.append(this.pagingAnchor((currentPage - 1), "&lt;", "prev"));
        }
        else {
          pagination.append(prev);
        }

        // put page number for all pages
        // do not put link for current page.
        for (var pageNumber = 0; pageNumber < totalPages; ++pageNumber) {
          if (pageNumber == currentPage) {
            pagination.append("<strong>" + (currentPage + 1) + "</strong>");
          }
          else {
            pagination.append(this.pagingAnchor(pageNumber, pageNumber + 1));
          }
        }

        // put > at the end with or without link depending on whether it is the last page.
        if (currentPage < (totalPages - 1)) {
          pagination.append(this.pagingAnchor(currentPage + 1, "&gt;", "next"));
        }
        else {
          pagination.append(next);
        }
      } else {
        // if >=4 pages then need to put "..."
        // put < and first page number always.
        // The link is present if this is not the first page.
        if (currentPage > 0) {
          pagination.append(this.pagingAnchor((currentPage - 1), "&lt;", "prev"));
          pagination.append(this.pagingAnchor(0, 1));
        }
        else {
          pagination.append(prev + '<strong>1</strong>');
        }
        // put the first "..." if this is more than 2 pages away from start.
        if (currentPage > 2) {
          pagination.append(ellipsis);
        }
        // put the three page numbers -- one before, current and one after
        for (var pageNumber = Math.min(currentPage, 0); pageNumber <= Math.max(3, currentPage + 2); ++pageNumber) {
          if ((pageNumber > 1 && pageNumber < totalPages && pageNumber > (currentPage - 1)
              || ((pageNumber == (totalPages - 2)) && (pageNumber > (currentPage - 2))))) {
            if ((currentPage + 1) == pageNumber) {
              pagination.append("<strong>" + pageNumber + "</strong>");
            }
            else {
              pagination.append(this.pagingAnchor(pageNumber - 1, pageNumber));
            }
          }
        }
        // if this is more than 2 pages away from last page, put last "..."
        if (currentPage < (totalPages - 3)) {
          pagination.append(ellipsis);
        }
        // put the last page number and >.
        // The link depends of whether this is the last page.
        if (currentPage < (totalPages - 1)) {
          pagination.append(this.pagingAnchor(totalPages - 1, totalPages));
          pagination.append(this.pagingAnchor(currentPage + 1, "&gt;", "next"));
        }
        else {
          pagination.append('<strong>' + totalPages + '</strong>' + next);
        }
      }
    }

    return pagination;
  };

  this.pagingAnchor = function (pageNumber, pagingText, className) {
    var anchor = $('<a></a>').attr({
      href: "#",
      title: "(" + (pageNumber + 1) + ")",
      onclick: "twitter.showTweetsPage(" + pageNumber + "); return false;"
    }).html(pagingText);

    if (className) {
      anchor.attr("class", className);
    }

    return anchor;
  };

  this.displayTweetsArticleSidebar = function (doi) {
    var alm = new $.fn.alm();
    alm.getCitesTwitterOnly(doi, jQuery.proxy(this.showTweetsArticleSidebar, this), jQuery.proxy(this.setError, this));
  }

  this.showTweetsArticleSidebar = function (json) {
    var twitterResponse, events, totalEventCount, minDisplayEventCount, maxDisplayEventCount, ol, i,
        tweet, created_dt, li, tweetText, div, ul, replyLink, reTweetLink, favoriteLink, tweetPostTime,
        linkToTweet;

    minDisplayEventCount = 2;
    maxDisplayEventCount = 5;

    //assuming here the request was properly formatted to get only twitter in sources[0]
    twitterResponse = json[0].sources[0];

    if (twitterResponse && twitterResponse.metrics.total > 0) {
      // just display one
      events = twitterResponse.events;
      events = events.sort(jQuery.proxy(this.sort_tweets_by_date, this));

      totalEventCount = (events.length > maxDisplayEventCount) ? maxDisplayEventCount : events.length;

      ol = $('<ol></ol>');

      for (i = 0; i < totalEventCount; i++) {
        tweet = events[i].event;

        created_dt = isNaN(Date.parse(tweet.created_at)) ?
            $.datepicker.formatDate("M d, yy", this.parseTwitterDate(tweet.created_at)) :
            $.datepicker.formatDate("M d, yy", new Date(tweet.created_at));

        if (i < minDisplayEventCount) {
          li = $("<li></li>").addClass("tweet-entry display");
        } else {
          li = $("<li></li>").addClass("tweet-entry hide");
        }

        li.hover(
            function () {
              // in
              $(this).find("ul.tweet-actions").css("visibility", "visible");
            },
            function () {
              // out
              $(this).find("ul.tweet-actions").css("visibility", "hidden");
            });

        tweetPostTime = this.getTweetTimeDisplay(tweet.created_at);
        linkToTweet = "<a href=\"https://twitter.com/" + tweet.user + "/statuses/" + tweet.id + "\">" + tweetPostTime + "</a>";
        // TODO add the twitter user full name once we have it
        div = $("<div></div>")
            .addClass("tweet-user")
            .append($("<div></div>")
                .addClass("twitter-posttime")
                .html(linkToTweet))
            .append($("<a></a>")
                .attr("href", "https://twitter.com/" + tweet.user)
                .append($("<img>")
                    .attr("src", tweet.user_profile_image))
                .append($("<div></div>")
                    .append($("<span></span>")
                        .addClass("twitter-fullname")
                        .html(tweet.user_name))
                    .append($("<span></span>")
                        .addClass("twitter-username")
                        .html("@" + tweet.user))));
        li.append(div);

        tweetText = $("<div></div>")
            .addClass("tweet-text")
            .html(this.linkify(tweet.text));
        li.append(tweetText);

        replyLink = $("<a></a>")
            .attr("href", "https://twitter.com/intent/favorite?in_reply_to=" + tweet.id)
            .addClass("tweet-reply-action")
            .attr("title", "Reply")
            .html("<span></span>Reply");

        reTweetLink = $("<a></a>")
            .attr("href", "https://twitter.com/intent/retweet?tweet_id=" + tweet.id)
            .addClass("tweet-retweet-action")
            .attr("title", "Retweet")
            .html("<span></span>Retweet");

        favoriteLink = $("<a></a>")
            .attr("href", "https://twitter.com/intent/favorite?tweet_id=" + tweet.id)
            .addClass("tweet-favorite-action")
            .attr("title", "Favorite")
            .html("<span></span>Favorite");

        ul = $("<ul></ul>").addClass("tweet-actions")
            .append($("<li></li>").append(replyLink))
            .append($("<li></li>").append(reTweetLink))
            .append($("<li></li>").append(favoriteLink));

        div = $("<div></div>").append(ul);
        li.append(div);

        ol.append(li);
      }

      $("#twitter-alm-timeline").append(ol);

      var doi = encodeURI($('meta[name=citation_doi]').attr("content"));
      if (events.length > minDisplayEventCount) {
        $("#twitter-alm-timeline").append(
            $("<button></button>")
                .html("Load More")
                .click(function () {
                  $("#twitter-alm-timeline li.tweet-entry.hide").removeClass("hide").addClass("display");
                  var a = $("<a></a>").attr("href", '/article/twitter/info:doi/' + doi).html("View all tweets");
                  $(this).html("").append(a);
                  $(this).css("background-image", "none")
                }
            )
        );
      } else {
        $("#twitter-alm-timeline").append(
            $("<button></button>").append($("<a></a>").attr("href", '/article/twitter/info:doi/' + doi).html("View all tweets")).css("background-image", "none")
        );
      }

      $("#twitter-alm-timeline").prepend(
          $("<div></div>")
              .addClass("tweet-header")
              .append($("<img>").addClass("tweet-header-logo").attr("src", "/images/tweet_bird_blue_32.png"))
              .append($("<b></b>").html("Archived Tweets"))
      );
    }
  }

  this.getTweetTimeDisplay = function (dateString) {
    var tweetDate = isNaN(Date.parse(dateString)) ? this.parseTwitterDate(dateString) : new Date(dateString);
    var now = new Date().getTime();
    // difference in hours
    var timeDiff = Math.round((now - tweetDate) / 1000 / 60 / 60);
    var output = "";
    var yearInHours = 365 * 24;

    if (timeDiff > yearInHours) {
      output = $.datepicker.formatDate("d M y", tweetDate);
    } else if (timeDiff >= 24.0) {
      // day month
      output = $.datepicker.formatDate("d M", tweetDate);
    } else {
      if (timeDiff >= 1.0) {
        // hour
        output = Math.round(timeDiff) + "h";
      } else {
        // minute
        timeDiff = (now - tweetDate) / 1000 / 60;
        if (timeDiff >= 1.0) {
          output = Math.round(timeDiff) + "m";
        } else {
          // second
          timeDiff = (now - tweetDate) / 1000;
          output = Math.round(timeDiff) + "s";
        }
      }
    }

    return output;
  }
};