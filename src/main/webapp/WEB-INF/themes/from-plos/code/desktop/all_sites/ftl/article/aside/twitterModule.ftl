<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<div class="twitter-container">
  <h3>Archived Tweets</h3>
  <ul id="tweetList">

  </ul>
  <div class="load-more">Load more <span></span></div>
  <div class="view-all"><a href="https://alm.plos.org/works/doi.org/${article.doi}?source_id=twitter">View all tweets</a>
  </div>
</div>

<script type="text/template" id="twitterModuleItemTemplate">
  <% _.each(items, function(item) { %>
    <li>
      <div class="tweet-info">
        <a href="https://twitter.com/<%= item.user %>">
          <span class="imgholder">
            <img class="imgLoad" src="<%= item.user_profile_image %>">
          </span>
          <div class="tweetDate"><%= item.created_at %></div>
          <div class="tweetUser">
            <strong><%= item.user_name %></strong>
            <span>@<%= item.user %></span>
          </div>
        </a>
      </div>
      <div class="tweetText">
        <%= item.text %>
      </div>
      <div id="tweetActions">
        <a class="tweet-reply" href="https://twitter.com/intent/tweet?in_reply_to<%= item.id %>&amp;text=@<%= item.user %>">
          <div>&nbsp;</div> Reply
        </a>
        <a class="tweet-retweet" href="https://twitter.com/intent/retweet?tweet_id=<%= item.id %>">
          <div>&nbsp;</div> Retweet
        </a>
        <a class="tweet-favorite" href="https://twitter.com/intent/favorite?tweet_id=<%= item.id %>">
          <div>&nbsp;</div> Favorite
        </a>
      </div>
    </li>
  <% }); %>
</script>
