function OnLoad() {
  var getBlog = document.getElementById('blogs').firstChild.nextSibling.textContent,
    findTitle = getBlog.trim().slice(5,8);

  if (findTitle === 'Bio') {
    var feed = new google.feeds.Feed("http://feeds.plos.org/plos/blogs/biologue");
    feed.load(feedLoaded);
  } else if (findTitle === 'Spe') {
    var feed = new google.feeds.Feed("http://feeds.plos.org/plos/MedicineBlog");
    feed.load(feedLoaded);
  }
}

google.setOnLoadCallback(OnLoad);