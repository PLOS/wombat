var HomeSubscription;

(function ($) {
  HomeSubscription = Class.extend({
    $container: $('.home-email-subscription'),
    exactTargetMID: "7207856",
    exactTargetEndpoint: "https://cl.exct.net/subscribe.aspx?Source=WebCollect",
    emailErrorMessage: "",

    init: function () {
      this.$form = this.$container.find('form');
      this.$title = this.$form.find('h2');
      this.$emailInput = this.$form.find('input[name="email"]');
      this.$journalKeyInput = this.$form.find('input[name="journalKey"]');
      this.$successMessage = this.$container.find('.success-message');
      this.$errorMessage = this.$container.find('.error-message');
      this.$errorLabel = this.$form.find('label.error');

      this.bindFormEvents();
      this.fixTitlePosition();
      this.showForm();
    },

    bindFormEvents: function () {
      var that = this;
      that.$form.on('submit', function (e) {
        e.preventDefault();
        e.stopPropagation();
        if(that.validateEmail()) {
          that.hideValidationError();
          that.submitForm();
        }
        else {
          that.showValidationError();
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
      if(title.height() > 25) {
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
        this.emailErrorMessage = "You need to type an email.";
        return valid;
      }

      if(!this.isEmailValid()) {
        valid = false;
        this.emailErrorMessage = "The email is invalid.";
        return valid;
      }

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
      var journalListId = null;

      switch (journalKey){
        case 'PLoSONE':
          journalListId = "634";
          break;
        case 'PLoSMedicine':
          journalListId = "772";
          break;
        case 'PLoSCompBiol':
          journalListId = "623";
          break;
        case 'PLoSBiology':
          journalListId = "587";
          break;
        case 'PLoSNTD':
          journalListId = "633";
          break;
        case 'PLoSPathogens':
          journalListId = "636";
          break;
        case 'PLoSGenetics':
          journalListId = "627";
          break;
      }

      return journalListId;
    }
  });

  new HomeSubscription();
})(jQuery);