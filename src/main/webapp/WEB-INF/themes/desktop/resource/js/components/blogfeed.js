
  google.load("feeds", "1");

  // Our callback function, for when a feed is loaded.
  function feedLoaded(result) {
    if (!result.error) {
      var container = document.getElementById("blogrss"),
        html = "", entry, getdate, pubDate, blogImg, postQty;
      var getBlog = document.getElementById('blogs').firstChild.nextSibling.textContent,
        docTitle = document.title.slice(5,8),
        blogDiv = container.parentNode;
         console.log(blogDiv);
      if (docTitle === 'Bio') {
        postQty = 4;
        blogDiv.style.height = "425px";
      } else {
        postQty = 2
      }
      for (var i = 0; i < postQty; i++) {

        entry = result.feed.entries[i],
        getdate = new Date(entry.publishedDate),
          options = {
            month: "long",
            day: "numeric"
          },
          pubDate = getdate.toLocaleString("en-US", options),
          blogImg = "resource/img/generic_blogfeed.png";

        html += '<div><img class="postimg" src="'+blogImg+'" /><p class="postdate">Posted '+ pubDate +'</p>' +
          '<p class="posttitle"><a href="'+entry.link+'">'+entry.title+'</a></p>' +
          '<p class="postauthor">'+entry.author+'</p></div>';

        }
      container.innerHTML = html;
      }
    }


