/**
 * Created by ddowell on 8/14/14.
 */

var dateParse = function(dateRaw){
  var dateOptions, findDay, findMonth,
    parseDate = new Date(dateRaw);

  // IE10 & lower don't support options in toLocaleString, so use verbose method to display the date
  if (document.all) {
    var monthNames = new Array();
    monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    findMonth = parseDate.getMonth();
    findDay = parseDate.getDay();
    postPubDate = monthNames[findMonth] + " " + findDay;

  } else {
    dateOptions = {month: "long", day: "numeric"};
    postPubDate = parseDate.toLocaleString("en-US", dateOptions);
  }
  return postPubDate;
}