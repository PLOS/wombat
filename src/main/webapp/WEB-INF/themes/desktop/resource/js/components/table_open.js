/*from Ambra global.js but modified a bit */
function tableOpen(tableId, type, tabled) {//console.log($(this));
  try {
    var table = tabled.find(".table");
    if (type == "HTML") {
      var w = window.open();
      w.document.open();
      w.document.writeln('<html><head><link rel="stylesheet" type="text/css" href="resource/css/screen.css"></head>');
      w.document.writeln('<body style="background-color: #ffffff;">');
      w.document.writeln('<div class="table-wrap">' + table.html() + '</div>');
      w.document.writeln('</body></html>');
      w.document.close();
    }
    else if (type == "CSV") {
     /* var table = $("table tbody");

      table.find('tr').each(function (i) {
        var $tds = $(this).find('td'),
          productId = $tds.eq(0).text(),
          product = $tds.eq(1).text(),
          Quantity = $tds.eq(2).text();
        // do something with productId, product, Quantity
        alert('Row ' + (i + 1) + ':\nId: ' + productId
          + '\nProduct: ' + product
          + '\nQuantity: ' + Quantity);
      });*/


      //http://stackoverflow.com/questions/7161113/how-do-i-export-html-table-data-as-csv-file
      function row2CSV(tmpRow) {
        var tmp = tmpRow.join('') // to remove any blank rows
         console.log(tmp);
        if (tmpRow.length > 0 && tmp != '') {
          var mystr = tmpRow.join(',');   console.log(mystr);
          csvData[csvData.length] = mystr;
          console.log(csvData[0]);
        }
      }
      /*function formatData(input) {
        // replace " with “
        var regexp = new RegExp(/["]/g);
        var output = input.replace(regexp, "“");//
        //HTML
        var regexp = new RegExp(/\<[^\<]+\>/g);
        var output = input.replace(regexp, "");console.log(output);
        if (output == "") return '';
        return '"' + output + '"';
      }*/
      var csvData = [];
      var headerArr = [];
      var tmpRow = [];
      var hds = table.find('th').children('strong');

      hds.each(function() { //console.log(hds.text());
        tmpRow[tmpRow.length] = this.textContent;
      });
      row2CSV(tmpRow);
     /* var rows = table.find('tbody').children('tr');

      rows.each(function() {
        var tmpRow = [];
        $(this).find('td').each(function() {console.log(this);
          tmpRow[tmpRow.length] = this;
        });
        row2CSV(tmpRow);
      });*/
      var mydata = csvData.join('\n');
      console.log(mydata);
      $("#btnExport").click(function () {
        var uri = $("#account_table").btechco_excelexport({
          containerid: "account_table",
          datatype: $datatype.Table,
          returnUri: true
        });

        $(this).attr('download', 'ExportToExcel.xls') // set file name (you want to put formatted date here)
          .attr('href', uri)                     // data to download
        ;
      });
     // var dataurl = 'data:text/csv;base64,' + $.base64.encode($.base64.utf8_encode(mydata));
     // console.log(dataurl);
     /* if ($.browser && ($.browser.chrome)) {  console.log('brwoser');
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
      else {*/
        window.location = mydata;
     /* }*/
    }
  }
  catch (e) {
    console.log(e);
  }
  return false;
}
