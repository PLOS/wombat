var RangeDatepicker = {};
(function ($) {
  /* AdvancedSearch attributes */
  RangeDatepicker = {
    format: 'yyyy-mm-dd'
  };

  /* AdvancedSearch methods */
  RangeDatepicker.init = function (fromInput, toInput) {
    //Start Date max date is the entered End Date. End Date min date is the entered Start Date.
    //Both Start and End Dates have a strict maximum of the current day
    var now = new Date();
    var startDate = $(fromInput).fdatepicker({
      format: this.format,
      onRender: function (date) {
        return date.valueOf() > now.valueOf() ? 'disabled' : '';
      }
    }).on('changeDate', function () {
      endDate.update(now); // Add default date of Now()
    }).data('datepicker');
    var endDate = $(toInput).fdatepicker({
      format: this.format,
      onRender: function (date) {
        return date.valueOf() < startDate.date.valueOf() ? 'disabled' : '';
      }
    }).data('datepicker');
  };


})(jQuery);