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
            blogImg = "resource/img/generic_blogfeed.png";
          }

          // TODO - need to move the link to the default image out of the JS.
// Wordpress spits out a string for thumbnail that is set up for use in the "srcSET" attribute. IE doesnt' support this so we break on a space because it outputs the smallest image first.
          blogImgString = blogImg.split(" ");
          blogImgRefined = blogImgString[0];

          html += '<div><img class="postimg" src="' + blogImgRefined + '"  srcset="' + blogImg + '"/><p class="postdate">Posted ' + postPubDate + '</p>' +
            '<p class="posttitle"><a href="' + entry.permalink + '">' + postTitle + '</a></p>' +
            '<p class="postauthor">' + entry.author + '</p></div>';

        container.innerHTML = html;

      }
    }).fail(function(){
        container.innerHTML = "An error occurred while loading the blog posts.";
      });
}


