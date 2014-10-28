// Pass 'true' for yearBoolean if you want to include the year. Leave empty if no year (as in the blogs module on the home page.)
var dateParse = function(dateRaw, yearBoolean, shortDate, locale){
  var dateOptions, findDay, findMonth, findYear, postPubDate, shortMonth,
    parseDate = new Date(dateRaw);

  // IE10 & lower don't support options in toLocaleString, so use verbose method to display the date
  if (document.all) {
    var monthNames = new Array();
    monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    findMonth = parseDate.getMonth();
    shortMonth = monthNames[findMonth].substr(0,3);
    findDay = parseDate.getDate();
    findYear = parseDate.getFullYear();
    postPubDate = monthNames[findMonth] + " " + findDay;
    if (yearBoolean === true) {
      postPubDate = monthNames[findMonth] + " " + findDay + " " + findYear;

    } else if (shortDate === true) {


      postPubDate = findDay + " " + shortMonth + " " + findYear;
    } else {
      postPubDate = monthNames[findMonth] + " " + findDay;

    }
  } else {
    if (yearBoolean) {
      dateOptions = {month: "long", day: "numeric", year: "numeric"};
    } else if (shortDate) {
      dateOptions = {day: "numeric", month: "short", year: "numeric"};
    }
    else {
      dateOptions = {month: "long", day: "numeric"};
    }
    postPubDate = parseDate.toLocaleString(locale, dateOptions);

  }
  return postPubDate;
}