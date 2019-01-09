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

var HomeSubscription;

(function ($) {
  HomeSubscription = Class.extend({
    $container: $('.home-email-subscription'),
    exactTargetMID: "7207856",
    exactTargetEndpoint: "https://cl.exct.net/subscribe.aspx?Source=WebCollect",
    emailErrorMessage: "",
    consentErrorMessage: "",

    init: function () {
      this.$form = this.$container.find('form');
      this.$title = this.$form.find('h2');
      this.$emailInput = this.$form.find('input[name="email"]');
      this.$consentCheckbox = this.$form.find('input[type="checkbox"][name="consent"]');
      this.$journalKeyInput = this.$form.find('input[name="journalKey"]');
      this.$successMessage = this.$container.find('.success-message');
      this.$errorMessage = this.$container.find('.error-message');
      this.$errorLabel = this.$form.find('label.error');
      this.$errorConsent = this.$form.find('label.error_consent');

      this.bindFormEvents();
      this.fixTitlePosition();
      this.showForm();
    },

    bindFormEvents: function () {
      var that = this;
      that.$form.on('submit', function (e) {
        e.preventDefault();
        e.stopPropagation();
        if(that.validateEmail() && that.validateConsent()) {
          that.submitForm();
        }
      });

      var retryButton = that.$errorMessage.find('.retry');
      retryButton.on('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        that.showForm();
      });
    },

    fixTitlePosition: function () {
      var title = this.$title;
      if(title.height() > 30) {
        title.addClass('multiline');
      }
    },

    isEmailValid: function () {
      var email = this.$emailInput.val();
      var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      return re.test(email);
    },

    validateEmail: function () {
      var valid = true;
      var email = this.$emailInput.val();

      if(email == "") {
        valid = false;
        this.emailErrorMessage = "Please enter an email.";
        this.showValidationError();
        return valid;
      }

      if(!this.isEmailValid()) {
        valid = false;
        this.emailErrorMessage = "The email you entered is invalid.";
        this.showValidationError();
        return valid;
      }

      this.hideValidationError();
      return valid;
    },

    showValidationError: function () {
      var errorLabel = this.$errorLabel;
      errorLabel.text(this.emailErrorMessage).show()
    },

    hideValidationError: function () {
      var errorLabel = this.$errorLabel;
      errorLabel.hide();
    },

    validateConsent: function () {
      if (!this.$consentCheckbox.is(":checked")) {
        this.consentErrorMessage = "Please check the box if you agree.";
        this.showConsentError();
        return false;
      }

      this.hideConsentError();
      return true;
    },

    showConsentError: function () {
        this.$errorConsent.text(this.consentErrorMessage).show()
    },

    hideConsentError: function () {
        this.$errorConsent.hide();
    },



    submitForm: function () {
      var that = this;
      var data = {
        "MID": that.exactTargetMID,
        "lid": that.getJournalListId(),
        "Email Address": that.$emailInput.val()
      };

      $.post(that.exactTargetEndpoint, data, function (response) {
        that.showSubmitSuccess();
      }).fail(function (error) {
        if (error.status == 0) {
          that.showSubmitSuccess();
        }
        else {
          that.showSubmitError();
        }
      });
    },

    showSubmitSuccess: function () {
      this.$container.addClass('success');
    },

    showSubmitError: function () {
      this.$container.addClass('error');
    },

    showForm: function () {
      this.$container.removeClass('error');
      this.$container.removeClass('success');
    },

    getJournalListId: function () {
      var journalKey = this.$journalKeyInput.val();
      // These are the weekly mailing list id inside Exact Target, we need to send them by the API endpoint based on the journal key. Each journal has it own key defined by Exact Target system.
      var journalListIds = {
        'PLoSONE': "634",
        'PLoSMedicine': "772",
        'PLoSCompBiol': "623",
        'PLoSBiology': "587",
        'PLoSNTD': "633",
        'PLoSPathogens': "636",
        'PLoSGenetics': "627"
      };

      return journalListIds[journalKey];
    }
  });

  new HomeSubscription();
})(jQuery);