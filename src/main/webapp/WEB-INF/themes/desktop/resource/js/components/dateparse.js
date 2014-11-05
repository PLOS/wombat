/**
 * Created by ddowell on 8/14/14.
 */

// Pass 'true' for yearBoolean if you want to include the year. Leave empty if no year (as in the blogs module on the home page.)
var dateParse = function(dateRaw, yearBoolean){
  var dateOptions, findDay, findMonth, findYear,
    parseDate = new Date(dateRaw);

  // IE10 & lower don't support options in toLocaleString, so use verbose method to display the date
  if (document.all) {
    var monthNames = new Array();
    monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    findMonth = parseDate.getMonth();
    findDay = parseDate.getDay();
    findYear = parseDate.getFullYear();
    postPubDate = monthNames[findMonth] + " " + findDay;
    if (yearBoolean) postPubDate += postPubDate + " " + findYear;
  } else {
    if (yearBoolean) {
      dateOptions = {month: "long", day: "numeric", year: "numeric"};
    } else {
      dateOptions = {month: "long", day: "numeric"};
    }
    postPubDate = parseDate.toLocaleString("en-US", dateOptions);

  }
  return postPubDate;
}