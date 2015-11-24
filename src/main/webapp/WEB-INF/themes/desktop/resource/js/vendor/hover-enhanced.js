(function ($) {

    $.fn.hoverEnhanced = function (options) {
    var defaults = {
        trigger:''
    };
    options = $.extend(defaults, options);
    return this.each(function () {
        var $this = $(this);
        $this.hoverIntent(
            function () {
                $this.addClass('reveal');
            },
            function () {
                $this.removeClass('reveal');
            }
        );
        if ($.support.touchEvents) {
            $this.unbind('mouseenter')
                .unbind('mouseleave');
            $this.find(options.trigger).on('click', function () {
                $this.siblings().removeClass('reveal');
                $this.toggleClass('reveal');
            });
        }
    });
};

})(jQuery);