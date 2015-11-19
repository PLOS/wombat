
(function ($) {
    $.fn.doOnce = function (func) {
        this.length && func.apply(this);
        return this;
    };

    $.fn.journalArchive = function (options) {
        defaults = {
            navID:'',
            slidesContainer:'',
            initialTab:0
        };
        var options = $.extend(defaults, options);
        var $navContainer = $(options.navID);
        var $slidesContainer = $(options.slidesContainer);
        init = function () {
            $navContainer.find('li').eq(options.initialTab).addClass('selected');
            var initial_slide = $slidesContainer.find('li.slide').eq(options.initialTab);
            var aheight = initial_slide.height();
            $slidesContainer.css('height', aheight);
            initial_slide.addClass('selected').fadeIn();
        };
        $navContainer.find('li a').on('click', function (e) {
            e.preventDefault();
            $this = $(this);
            var target = $this.attr('href');
            $navContainer.find('li.selected').removeClass('selected');
            $slidesContainer.find('li.slide.selected').removeClass('selected').fadeOut();
            $this.parent('li').addClass('selected');
            var targetElement = $slidesContainer.find('li' + target);
            targetElement.addClass('selected').fadeIn();
            $slidesContainer.animate({'height':targetElement.height()});
        });
        init();
    };
})(jQuery);


//Browse / issue page functions
// on window load
$(window).load(function () {
    $('.journal_issues').doOnce(function () {
        this.journalArchive({
            navID:'#journal_years',
            slidesContainer:'#journal_slides',
            initialTab:0
        });
    });
});
