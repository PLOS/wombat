
window.figshare.load("related_content", function(Widget) {
  // Select a tag defined in your page. In  this tag we will place the widget.
  var container = document.getElementById("figshare-related");
  if (!container) {
    return;
  }

  var doi = container.getAttribute("data-doi");
  if (!doi) {
    return;
  }

  var widget = new Widget({
    doi: doi,
    institutionString: window.WombatConfig.figShareInstitutionString,
    title: "Recommendations"
  });

  widget.initialize(); // initialize the widget
  widget.mount(container); // mount it in a tag that's on your page
});