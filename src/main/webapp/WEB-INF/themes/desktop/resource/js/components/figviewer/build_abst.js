// build abstract pane
FVBuildAbs = function(doi, abstractText, metadata) {
  $FV.abst_pane = $('<div id="fig-viewer-abst" class="pane" />');
  var lnk_pdf, pdf_href, $abst_info, $abst_content = $('<div class="abstract" />');

  if (abstractText.size() == 0) {
    // There is no abstract. Hide the "view abstract" button created in FVBuildHdr.
    $FV.hdr.find('li.abst').hide();
  }
  else {
    $abst_content.html(abstractText.html());
    $abst_content.find("h2").remove();
    $abst_content.find('a[name="abstract0"]').remove();
  }

  pdf_href = $('#downloadPdf').attr('href');
  lnk_pdf = '<div class="fv-lnk-pdf"><a href="' + pdf_href + '" target="_blank" class="btn">Download: Full Article PDF Version</a></div>';
  $abst_content.append(lnk_pdf);

  $abst_info = $('<div class="info" />');
  $abst_info.html(metadata.html());
  $FV.abst_pane.append($abst_info);
  $FV.abst_pane.append($abst_content);

  $('#panel-abst').append($FV.abst_pane);

};