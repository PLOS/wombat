/**
 * Created by pgrinbaum on 12/16/16.
 */

$( document ).ready(function() {

  $('select[name="revisionLink"]').change(function () {
    var revisionLink = $(this).val();
    window.location.href = revisionLink;
  });

  });