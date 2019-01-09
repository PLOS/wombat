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
$(document).ready(function () {
    $.fn.updateCounters = function() {
        var query = new CounterQuery();
        var validator = new CounterQueryValidator();

        $(".alm-info").addClass("query-pending");

        query
            .setDataValidator(validator)
            .getArticleSummary($('meta[name=citation_doi]').attr('content'))
            .then(function (counterData) {
                $(".alm-info").removeClass("query-pending");
                if (counterData.totals === 0) {
                    $('#counter-pdf, #counter-xml, #counter-views').text(0);
                } else {
                    $('#counter-pdf').text(counterData['get-pdf']);
                    $('#counter-xml').text(counterData['get-xml']);
                    $('#counter-views').text(counterData['get-document']);
                }
            })
            .fail(function (error) {
                $(".alm-info").removeClass("query-pending").addClass("query-error");
                $('#counter-error').text('Counter data is unavailable');

                // // following is for testing only
                // $(".alm-info").removeClass("query-error").addClass("query-pending");
                // setTimeout(function() {
                //     $(".alm-info").removeClass("query-pending");
                //     $('#counter-views').text("30,542");
                //     $('#counter-pdf').text("1,875");
                //     $('#counter-xml').text("207");
                // }, 1000);
            });
    };
});