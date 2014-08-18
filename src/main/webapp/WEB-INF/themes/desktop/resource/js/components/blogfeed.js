// *** requires dateparse.js

if (typeof google=='undefined') {
  document.getElementById("blogrss").innerHTML = "Please click on the link above to see the blog posts."
} else {
  google.load("feeds", "1");
}
function feedLoaded() {

  var whichBlog = document.getElementById('blogtitle').innerHTML;
  whichBlog = whichBlog.slice(5,8);
  if (typeof google=='undefined') {
    document.getElementById("blogrss").innerHTML = "Something went wrong. Please click on the link above to see the blog posts."
  } else {
    if (whichBlog === 'Bio') {
      var feed = new google.feeds.Feed("http://feeds.plos.org/plos/blogs/biologue");

    } else if (whichBlog === 'Spe') {
      var feed = new google.feeds.Feed("http://feeds.plos.org/plos/MedicineBlog");

    }
  }

  feed.load(
    function (result) {
      var container = document.getElementById("blogrss");
      if (!result.error) {
        var html = "", docTitle, blogDiv, postQty, entry, postTitle, postDescription,
          postPubDate, tempDiv, blogImg;

        blogDiv = container.parentNode;
        docTitle = document.title.slice(5, 8);
        if (docTitle === 'Bio') {
          postQty = 4;
          blogDiv.style.height = "425px";
        } else {
          postQty = 2
        }
        for (var i = 0; i < postQty; i++) {

          entry = result.feed.entries[i];
          postTitle = entry.title;
          postDescription = entry.content;

          postPubDate = dateParse(entry.publishedDate);

          // add ellipsis to titles that are cut off
          if (postTitle.length > 75) {
            postTitle = postTitle.slice(0, 70) + "&hellip;";
          }

          // create temporary div to traverse the description content to extract image src
          tempDiv = document.createElement('div');
          tempDiv.innerHTML = postDescription;
          blogImg = tempDiv.firstChild.src;
          if (blogImg == null) {
            blogImg = "resource/img/generic_blogfeed.png";
          }

          html += '<div><img class="postimg" src="' + blogImg + '" /><p class="postdate">Posted ' + postPubDate + '</p>' +
            '<p class="posttitle"><a href="' + entry.link + '">' + postTitle + '</a></p>' +
            '<p class="postauthor">' + entry.author + '</p></div>';

        }
        container.innerHTML = html;

      } else {
        container.innerHTML = "An error occurred while loading the blog posts.";
      }
    }
  );
}

google.setOnLoadCallback(function(){
  feedLoaded();
});
