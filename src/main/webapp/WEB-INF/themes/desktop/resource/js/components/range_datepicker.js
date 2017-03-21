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

var RangeDatepicker = {};
(function ($) {
  RangeDatepicker.options = {
    format: 'yyyy-mm-dd',
    min: null,
    max: null
  };

  /* AdvancedSearch methods */
  RangeDatepicker.init = function (fromInput, toInput, options) {

    //Start Date max date is the entered End Date. End Date min date is the entered Start Date.
    //Both Start and End Dates have a strict maximum of the current day
    options = $.extend(RangeDatepicker.options, $(fromInput).data());
    var startDate = $(fromInput).fdatepicker({
      format: options.format,
      onRender: function (date) {
        if (options.min instanceof Date && date.valueOf() < options.min.valueOf()) {
          return 'disabled';
        }
        if (options.max instanceof Date && date.valueOf() > options.max.valueOf()) {
          return 'disabled';
        }
      }
    }).on('changeDate', function () {
      endDate.update(new Date()); // Add default date of Now()
    }).data('datepicker');
    options = $.extend(RangeDatepicker.options, $(toInput).data());
    var endDate = $(toInput).fdatepicker({
      format: options.format,
      onRender: function (date) {
        if (options.min instanceof Date && date.valueOf() < options.min.valueOf()) {
          return 'disabled';
        }
        if (options.max instanceof Date && date.valueOf() > options.max.valueOf()) {
          return 'disabled';
        }
        return date.valueOf() < startDate.date.valueOf() ? 'disabled' : '';
      }
    }).data('datepicker');
  };


})(jQuery);