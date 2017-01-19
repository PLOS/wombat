/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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