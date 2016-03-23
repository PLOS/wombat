var ErrorFactory;

(function ($) {
  ErrorFactory = function (errorName, errorDescription) {
    var err = new Error(errorDescription);
    err.name = errorName;
    return err;
  };
})(jQuery);