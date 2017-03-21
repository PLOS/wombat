
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
