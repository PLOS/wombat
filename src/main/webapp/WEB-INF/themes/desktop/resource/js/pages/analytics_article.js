/**
 * Created by pgrinbaum on 12/11/14.
 * Some of this code is taken from plos-themes/desktop/Plos/resource/js/ga.js we might want to remove this file
 */
(function ($) {

  var sharelink = '.share-article ul li a', printlink = '#printArticle #printBrowser';

// shareLink
  $(sharelink).on('click', function (event) {    //TODO: abstract this out
        var title = $(event.target).attr('title');
        ga('send', 'event', 'Article', 'Share', title);
      });

  $(printlink).on('click', function (event) {    //TODO: abstract this out
    ga('send', 'event', 'Article', 'Print', 'Click');
  });

  $('a').each(function (e) {
    var el = $(this);
    var href = (typeof(el.attr('href')) != 'undefined' ) ? el.attr('href') : "";
    var curOnClick = '' + el.attr('onclick');
    var elClass = el.attr('class');
    var elEv = [];
    elEv.delay = (el.attr('target') == undefined || el.attr('target').toLowerCase() != '_blank') ? true : false;

    if (!href.match(/^javascript:/i)) {
      var trace = true,
          filetypes = /\.(zip|exe|dmg|pdf|doc*|xls*|ppt*|mp3|slxb|pps*|vsd|vxd|txt|rar|wma|mov|avi|wmv|flv|wav|xml)$/i,
          taxonomyLink = "subject",
          taxonomyLocator = 'taxo-term';

      //TODO - add filetypes?
      var baseHref = '';
      //mailto
      if (href.match(/^mailto\:/i)) {
        elEv.category = "email";
        elEv.action = "click";
        elEv.label = href.replace(/^mailto\:/i, '');
        elEv.value = undefined;
        elEv.non_i = false;
      }
      //file downloads
      else if (href.match(filetypes)) {
        var extension = (/[.]/.exec(href)) ? /[^.]+$/.exec(href) : undefined;
        elEv.category = "download";
        elEv.action = "click-" + extension;
        elEv.label = href;
        elEv.value = undefined;
        elEv.non_i = false;
        elEv.loc = baseHref + href;
      }
      //taxonomy terms
      else if (elClass === taxonomyLocator ) {
        var stringAction = href.split('subject', 2);
        var terms = (decodeURIComponent(stringAction[1]));
        var test= elClass;
        elEv.category = "taxonomyTerms";
        elEv.action = "click";
        elEv.label = terms;
        elEv.value = undefined;
        elEv.non_i = false;
        elEv.loc = baseHref + href;
      } else if (this.host !== location.host) {
        elEv.category = "external";
        elEv.action = "click";
        elEv.label = href;
        elEv.value = undefined;
        elEv.non_i = true;
        elEv.loc = baseHref + href;
      } else trace = false;
      // perform _trackEvent
      el.click(function () {
//Leaving this debugging in here commented  for now because I want to keep debugging info in here until we are sure we have hit all the items we need to
//       alert('__________test:' + test +'__________elEv.category:' + elEv.category +':' + ' __________elEv.action:' + elEv.action +':'  + ' __________elEv.label:' + elEv.label +':'  + ' __________elEv.value:' + elEv.value +':'  + ' __________elEv.non_i:' + elEv.non_i );
       if (trace) {
        ga('send', 'event', elEv.category, elEv.action, elEv.label, elEv.value, elEv.non_i);
       }
      });
    } // end if javascript or Redirect
  }); // end a.each

})(jQuery);
