/*from Ambra global.js*/
function tableOpen(tableId, type) {
  try {
    var table = $('div.table-wrap[name="' + tableId + '"]')
    if (type == "HTML") {
      var w = window.open();
      w.document.open();
      w.document.writeln('<html><head><link rel="stylesheet" type="text/css" href="/css/global.css"></head>');
      w.document.writeln('<body style="background-color: #ffffff;">');
      w.document.writeln('<div class="table-wrap">' + table.html() + '</div>');
      w.document.writeln('</body></html>')
      w.document.close();
    }
    else if (type == "CSV") {
      //http://stackoverflow.com/questions/7161113/how-do-i-export-html-table-data-as-csv-file
      function row2CSV(tmpRow) {
        var tmp = tmpRow.join('') // to remove any blank rows
        if (tmpRow.length > 0 && tmp != '') {
          var mystr = tmpRow.join(',');
          csvData[csvData.length] = mystr;
        }
      }
      function formatData(input) {
        // replace " with “
        var regexp = new RegExp(/["]/g);
        var output = input.replace(regexp, "“");
        //HTML
        var regexp = new RegExp(/\<[^\<]+\>/g);
        var output = output.replace(regexp, "");
        if (output == "") return '';
        return '"' + output + '"';
      }
      var csvData = [];
      var headerArr = [];
      var tmpRow = [];
      $(table).find('thead td').each(function() {
        tmpRow[tmpRow.length] = formatData($(this).html());
      });
      row2CSV(tmpRow);
      $(table).find('tbody tr').each(function() {
        var tmpRow = [];
        $(this).find('td').each(function() {
          tmpRow[tmpRow.length] = formatData($(this).html());
        });
        row2CSV(tmpRow);
      });
      var mydata = csvData.join('\n');
      var dataurl = 'data:text/csv;base64,' + $.base64.encode($.base64.utf8_encode(mydata));
      if ($.browser && ($.browser.chrome)) {
        // you can specify a file name in <a ...> tag on chrome.
        // http://stackoverflow.com/questions/283956/is-there-any-way-to-specify-a-suggested-filename-when-using-data-uri
        function downloadWithName(uri, name) {
          function eventFire(el, etype){
            if (el.fireEvent) {
              (el.fireEvent('on' + etype));
            } else {
              var evObj = document.createEvent('Events');
              evObj.initEvent(etype, true, false);
              el.dispatchEvent(evObj);
            }
          }
          var link = document.createElement("a");
          link.download = name;
          link.href = uri;
          eventFire(link, "click");
        }
        downloadWithName(dataurl, tableId + ".csv");
      }
      else {
        window.location = dataurl;
      }
    }
  }
  catch (e) {
   // console.log(e);
  }
  return false;
}
