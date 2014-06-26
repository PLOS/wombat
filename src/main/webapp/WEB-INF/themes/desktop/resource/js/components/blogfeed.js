
  google.load("feeds", "1");

  // Our callback function, for when a feed is loaded.
  function feedLoaded(result) {
    if (!result.error) {
      var container = document.getElementById("blogrss"),
        html = "", entry, getdate, pubDate, blogImg;
      //container.innerHTML = '';

      for (var i = 0; i < 2; i++) {
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


