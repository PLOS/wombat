var ArticleRelatedContent;

(function ($) {

  ArticleRelatedContent = Class.extend({

    $mediaCoverageEl: $('#media-coverage-data'),
    mediaCoverageData: null,
    mediaCoverageSections: [
      {
        name: "News",
        title: "News Media Coverage",
        eventTypes: ['News']
      },
      {
        name: "Blog",
        title: "Blog Coverage",
        eventTypes: ['Blog']
      },
      {
        name: "Other",
        title: "Related Resources",
        eventTypes: []
      },
    ],
    modalFormEl: '#media-coverage-modal',
    modalErrorCloseTimeout: 3000,
    modalSuccessCloseTimeout: this.modalErrorCloseTimeout/2,

    init: function () {


      var query = new AlmQuery();
      var that = this;

      query.getArticleDetail(ArticleData.doi)
        .then(function (articleData) {
          var data = articleData[0];
          var mediaCoverageSource = _.findWhere(data.sources, {name: 'articlecoveragecurated'});

          if(mediaCoverageSource && mediaCoverageSource.events.length) {
            that.mediaCoverageData = _.map(mediaCoverageSource.events, function (item) { return item.event; });
          }
          else {
            throw new ErrorFactory('NoRelatedContentError', '[ArticleRelatedContent::init] - The article has no related content.');
          }

          that.loadMediaCoverage();
        })
        .fail(function (error) {
          console.log(error);
        });

      this.modalFormBindings();
    },

    loadMediaCoverage: function () {
      var that = this;
      var usedTypes = [];
      var renderedSections = 0;
      var sectionTemplate = _.template($('#articleRelatedContentSectionTemplate').html());
      _.each(this.mediaCoverageSections, function (section) {
        usedTypes = usedTypes.concat(section.eventTypes);

        var items = _.filter(that.mediaCoverageData, function (item) {
          var typeValidation = (_.indexOf(section.eventTypes, item.type) >= 0);
          if(!section.eventTypes.length) {
            typeValidation = (_.indexOf(usedTypes, item.type) < 0);
          }
          return typeValidation &&
            (item.link_state = "APPROVED") &&
            !_.isEmpty(item.title) &&
            !_.isEmpty(item.publication) &&
            !_.isEmpty(item.published_on);
        });

        if(items.length) {
          that.$mediaCoverageEl.append(sectionTemplate({ section: section, items: items }));
          renderedSections++;
        }
      });

      if(!renderedSections) {
        that.$mediaCoverageEl.append('<p><br>No media coverage found for this article.</p>');
      }
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
      $("#media-coverage-form :input + span.form-error, #mcform-captcha + span.form-error, #mcform-error").text("");

      this.modalFormShowRecaptcha('mcform-captcha');

      $('#media-coverage-success').hide();
      $('#media-coverage-failure').hide();
      $('#media-coverage-form').show();

      // clear input field values
      $('#media-coverage-form :input').not("#mcform-name, #mcform-email").val('');
    },

    modalFormShowRecaptcha: function (element) {
      Recaptcha.create($('#reCaptcha-info').val(), element, {
        theme: "white",
        callback: Recaptcha.focus_response_field});
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

        recaptcha_challenge_field: $('#recaptcha_challenge_field').val(),
        recaptcha_response_field: $('#recaptcha_response_field').val()
      };

      $("#media-coverage-form :input + span.form-error, #mcform-captcha + span.form-error").text("");

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

            if (data.captchaError) {
              $("#mcform-captcha").next().text(data.captchaError);
            }
            Recaptcha.reload();
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

  new ArticleRelatedContent();

})(jQuery);