
  function tableOpen(tableId, type, tabled) {
    try {
      var table = tabled.find(".table"),
          caption = tabled.find(".table-caption");
      if (type == "HTML") {    //open table in new browser window
        var w = window.open();
        w.document.open();
        w.document.writeln('<html><head><title>' + caption.text() + '</title><link rel="stylesheet" type="text/css" href="resource/css/screen.css"><link rel="shortcut icon" href="resource/img/favicon.ico" type="image/x-icon"></head>');
        w.document.writeln('<body style="background-color: #ffffff;">');
        w.document.writeln('<div class="table-wrap">' +  table.html() + '<div class="table-caption">'+caption.html() +'</div> </div>');
        w.document.writeln('</body></html>');
        w.document.close();
      }
      else
        if (type == "CSV") {  //download table data in a csv file
           var tmp, tmpRow, csvData, hds, rows, tmpRow2, rowsize, innards, mydata,
             filename, link, blobObject, downloadUrl;
          function row2CSV(tmpRow) {
            tmp = tmpRow.join('') // to remove any blank rows

            if (tmpRow.length > 0 && tmp != '') {
              var mystr = tmpRow.join(',');
              csvData[csvData.length] = mystr;
            }
          }

          csvData = [];
          tmpRow = [];
          hds = table.find('th');

          hds.each(function () {
            tmpRow[tmpRow.length] = this.textContent;
          });
          row2CSV(tmpRow);
          rows = table.find('tbody').children('tr');
          tmpRow2 = [];

          rows.each(function () {
            rowsize = this.cells.length;
            innards = this.cells;
            for (var i = 0; i < rowsize; i++) {
              tmpRow2[i] = innards[i].textContent;
            }
            row2CSV(tmpRow2);

          });
          mydata = csvData.join('\n');

          filename = tableId+'.csv';
          link = document.createElement("a");

          if(window.navigator.msSaveOrOpenBlob) {  //IE10+

            blobObject = new Blob([mydata]);
            window.navigator.msSaveOrOpenBlob(blobObject, filename);
          } else {

            if (link.download !== undefined) {   // Chrome, FF & Safari

              blobObject = new Blob([mydata], {type: 'text/csv;charset=utf-8;'});
              downloadUrl=URL.createObjectURL(blobObject);
              link.setAttribute('href',downloadUrl);
              link.setAttribute('download',filename);
              document.body.appendChild(link);
              link.click();
              document.body.removeChild(link);
            }
            else { //IE8 & 9

              var w = window.open();
              w.document.open();
              w.document.write(mydata);
              w.document.close();

            }
          }
        }
    }
    catch (e) {
      // TODO: implement error messaging;
      var errorText = 'Our system is having a bad day. We are working on it. Please check back later.';
      //console.log(e);

    }
    return false;
  }
