
  google.load("feeds", "1");

  // Our callback function, for when a feed is loaded.

  function feedLoaded(result) {
    if (!result.error) {
      var container = document.getElementById("blogrss"),
        html = "", entry, getdate, pubDate, blogImg, postQty,
        postTitle, docTitle, blogDiv, postDescription, tempDiv;

        docTitle = document.title.slice(5,8),
        blogDiv = container.parentNode;
         
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
          postTitle = entry.title,
          postDescription = entry.content;

        tempDiv = document.createElement('div');
        tempDiv.innerHTML = postDescription;
        blogImg = tempDiv.firstChild.src;
        if (blogImg == null) {
          blogImg = "resource/img/generic_blogfeed.png";
        }
        if (postTitle.length > 75) {
          postTitle = postTitle.slice(0, 70) + "&hellip;";
        }

        html += '<div><img class="postimg" src="'+blogImg+'" /><p class="postdate">Posted '+ pubDate +'</p>' +
          '<p class="posttitle"><a href="'+entry.link+'">'+postTitle+'</a></p>' +
          '<p class="postauthor">'+entry.author+'</p></div>';

        }
      container.innerHTML = html;
      }
    }


