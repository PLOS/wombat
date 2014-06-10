/**
 * Created by ddowell on 6/9/14.
 */

function searchCheck(){
  $("form[name='searchForm']").submit(function() { return false; });
// if html5 forms required nor Modernizr aren't supported, do nothing as before.
  if (!Modernizr.input.required) {

    $("form[name='searchForm']").submit(function() {
      var required = $("#search").val();
      if (required == "") {
        return false;
      }
    });
  }
});

/*
$(document).ready(function() {
var hecka = searchCheck();
}*/
