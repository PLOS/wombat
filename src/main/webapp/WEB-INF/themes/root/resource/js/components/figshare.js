window.figshare.load(window.WombatConfig.figShareInstitutionString, function(Widget) {

  // Select all tags defined in your page. In  these tags we will place the widget.
  var containers = document.querySelectorAll(".figshare_widget");
  var loadedWidgets = [];

  for(var i = 0, n = containers.length; i < n; i += 1) {

    var doi = containers[i].getAttribute("doi");
    var groupStringId = doi.split(".")[2];

    var widget = new Widget({
      doi: doi,
      extraClass: groupStringId
    });

    widget.initialize(); // initialize the widget
    widget.mount(containers[i]); // mount it in a tag that's on your page
    loadedWidgets.push(widget);
  }

  // this will save the widget on the global scope for later use from
  // your JS scripts. This line is optional.
  window.loadedWidgets = loadedWidgets;

});