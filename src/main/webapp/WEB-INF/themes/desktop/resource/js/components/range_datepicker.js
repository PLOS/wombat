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