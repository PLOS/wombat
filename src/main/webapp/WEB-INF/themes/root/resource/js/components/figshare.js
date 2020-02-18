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

if (window.figshare) {
  window.figshare.load(window.WombatConfig.figShareInstitutionString, function (Widget) {

    // Select all tags defined in your page. In  these tags we will place the widget.
    var containers = document.querySelectorAll(".figshare_widget");
    var loadedWidgets = [];

    for (var i = 0, n = containers.length; i < n; i += 1) {

      var doi = containers[i].getAttribute("doi");
      var groupStringId = doi.split(".")[2];

      var theme = {
        pbio: "green", pcbi: "green", pgen: "green",
        pmed: "purple", ppat: "purple", pntd: "purple",
        pone: "yellow"
      }[groupStringId] || "yellow";

      var widget = new Widget({
        // doi: doi,
        // extraClass: groupStringId

        version: "3",
        theme: theme, // one of "yellow", "green", "purple",
        mathJax: true, // true or false
        width: 650,
        height: 450,
        breakPoint: 300,
        showStats: false,
        showPageInfo: true,
        showShareButton: false,
        showFileDetails: true,
        collection: null,
        item: {
          doi: doi
        }
      });


      changeAsideToDiv(containers[i]);
      widget.initialize(); // initialize the widget
      widget.mount(containers[i]); // mount it in a tag that's on your page

      loadedWidgets.push(widget);
    }

    // this will save the widget on the global scope for later use from
    // your JS scripts. This line is optional.
    window.loadedWidgets = loadedWidgets;

  });


  // doubleclickad gets added to <aside> element of this widget. so rename it to <div>
  function changeAsideToDiv(container) {
    var state = {timer: null, count: 0};
    var handler = function () {
      var aside = container.querySelector('aside');
      if (aside) {
        clearTimeout(state.timer);
        console.log('changed <aside> to <div>');
        var div = document.createElement('div');
        div.className = aside.className;
        div.innerHTML = aside.innerHTML;
        aside.parentNode.replaceChild(div, aside);
      } else {
        state.count += 1;
        if (state.count * 100 > 30000) { // stop after 30s
          clearTimeout(state.timer);
        }
      }
    };
    state.timer = setInterval(handler, 100);
  }

}