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

// *** requires moment.js

function feedLoaded(blog_feed, blogPostCount, blogContainer) {

  var container = blogContainer

  $.getJSON(blog_feed,
      function (result) {
        var postCount = blogPostCount;

        var html = "", entry, postTitle,
          postPubDate, blogImg;

        for (var i = 0; i < postCount; i++) {

          entry = result[i];
          postTitle = entry.title;
          postPubDate = moment(entry.date).format("MMMM DD");

          // add ellipsis to titles that are cut off
          if (postTitle.length > 75) {
            postTitle = postTitle.slice(0, 70) + "&hellip;";
          }

          blogImg = entry.thumbnail;
          if (blogImg == null) {
            blogImg = "resource/img/logo-plos-imageonly.png";
          }

          // TODO - need to move the link to the default image out of the JS.
// Wordpress spits out a string for thumbnail that is set up for use in the "srcSET" attribute. IE doesnt' support this so we break on a space because it outputs the smallest image first.
          blogImgString = blogImg.split(" ");
          blogImgRefined = blogImgString[0];

          html += '<div><div class="postimg" style="background-image: url('+blogImgRefined+')"></div><p class="postdate">Posted ' + postPubDate + '</p>' +
            '<p class="posttitle"><a href="' + entry.permalink + '">' + postTitle + '</a></p>' +
            '<p class="postauthor">' + entry.author + '</p></div>';

        container.innerHTML = html;

      }
    }).fail(function(){
        container.innerHTML = "An error occurred while loading the blog posts.";
      });
}


