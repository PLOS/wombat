/**
 * Created by ddowell on 6/9/14.
 */
  // if html5 forms required nor Modernizr aren't supported, do nothing:
function searchCheck(){

  if (!Modernizr.input.required) {
    $("form[name='searchForm']").submit(function() {
      var searchTerm = $("#search").val();
      if (!searchTerm) return false;
    });
  }

};
$(document).ready(function() {

 searchCheck();
});
