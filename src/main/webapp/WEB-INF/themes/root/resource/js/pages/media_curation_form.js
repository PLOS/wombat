

/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, t any person obtaining a
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

var mediaCurationCoverage;

(function ($) {

  mediaCurationCoverage = Class.extend({

    $mediaCoverageEl: $('#media-coverage-data'),

    modalFormEl: '#media-coverage-modal',
    modalErrorCloseTimeout: 3000,
    modalSuccessCloseTimeout: this.modalErrorCloseTimeout/2,

    init: function () {


      var that = this;


      this.modalFormBindings();
    },


    modalFormBindings: function () {
      var that = this;

      $('form input#mcform-publishedOn').fdatepicker({
        format: 'yyyy-mm-dd',
        disableDblClickSelection: true
      });

      $(this.modalFormEl).on('click', '.cancel', function () {
        that.modalFormDismiss(0);
      });

      $(this.modalFormEl+' #media-coverage-form').on('submit', 'form', function (e) {
        e.preventDefault();
        e.stopPropagation();

        that.modalFormSubmit();
      });

      $(document).on('open.fndtn.reveal', '[data-reveal]', function () {
        $('input#mcform-publishedOn').fdatepicker({
          initialDate: '02-12-1989',
          format: 'mm-dd-yyyy',
          disableDblClickSelection: true
        });
        that.modalFormReset();
      });

    },

    modalFormReset: function () {
      $("#media-coverage-form :input + span.form-error, #mcform-error").text("");

      $('#media-coverage-success').hide();
      $('#media-coverage-failure').hide();
      $('#media-coverage-form').show();

      // clear input field values
      $('#media-coverage-form :input').not("#mcform-name, #mcform-email").val('');
    },

    modalFormDatePicker: function () {

    },

    modalFormSubmit: function () {
      var that = this;
      var doi = $('#media-coverage-form').find('form').data('doi');
      var data = {
        doi: doi,
        name: $('#mcform-name').val(),
        email: $('#mcform-email').val(),
        link: $('#mcform-link').val(),
        title: $('#mcform-title').val(),
        publishedOn: $('#mcform-publishedOn').val(),
        comment: $('#mcform-comment').val(),

      };

      $("#media-coverage-form :input + span.form-error").text("");

      var formEndpoint = siteUrlPrefix + "article/submitMediaCurationRequest";

      $.ajax(formEndpoint, {
        type: "POST",
        dataType:"json",
        data: data,
        dataFilter:function (data, type) {
          // Remove block comment from around JSON, if present
          return data.replace(/(^\s*\/\*\s*)|(\s*\*\/\s*$)/g, '');
        },
        success:function (data, textStatus, jqXHR) {
          $("#mcform-error").text("");
          if (data.isValid == false) {
            if (data.formError) {
              $("#mcform-error").text(data.formError);
            }

            if (data.linkError) {
              $("#mcform-link").next().text(data.linkError);
            }

            if (data.nameError) {
              $("#mcform-name").next().text(data.nameError);
            }

            if (data.emailError) {
              $("#mcform-email").next().text(data.emailError);
            }

            if (data.titleError) {
              $("#mcform-title").next().text(data.titleError);
            }

            if (data.publishedOnError) {
              $("#mcform-publishedOn").next().text(data.publishedOnError);
            }
          }
          else {
            // display success message and close the form
            $('#media-coverage-form').hide();
            $('#media-coverage-success').show();

            that.modalFormDismiss(that.modalSuccessCloseTimeout);
          }
        },
        error:function (jqXHR, textStatus, errorThrown) {
          // display the error message and close the form
          $('#media-coverage-form').hide();
          $('#media-coverage-failure').show();

          that.modalFormDismiss(that.modalErrorCloseTimeout);
        }
      });
    },

    modalFormDismiss: function (timeToWait) {
      var that = this;
      setTimeout(function () {
        $(that.modalFormEl).foundation('reveal', 'close');
      }, timeToWait);
    }

  });

  new mediaCurationCoverage();

})(jQuery);
