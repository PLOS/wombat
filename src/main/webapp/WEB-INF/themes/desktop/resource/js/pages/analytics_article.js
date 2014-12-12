/**
 * Created by pgrinbaum on 12/11/14.
 */
(function ($) {

 //   function handleSocialClick(event) {
 //     alert($(e.target).attr('title'));
//      ga('send', 'event', 'Article', 'Share', $(event.target).attr('title') 4);
//    };

  var sharelink = '.share-article ul li a',
      printlink = '#printArticle #printBrowser';

//  var ga_track = function ( ga_category, ga_action, ga_label) {
//    ga('send', 'event', ga_category, ga_action, ga_label);
//  };

// shareLink
  $(sharelink).on('click', function(event) {    //TODO: abstract this out
        var title = $(event.target).attr('title');
        ga('send', 'event', 'Article', 'Share', title);
      }
  );

$(printlink).on('click', function(event) {    //TODO: abstract this out
  ga('send', 'event','Article', 'Print', 'Click');
});


  $('a').each(function (e) {
    var el = $(this);
    var href = (typeof(el.attr('href')) != 'undefined' ) ? el.attr('href') : "";
    var curOnClick = '' + el.attr('onclick');

//    var linkServer = (href.match(/^https?\:/i)) ? href.match(/^(([a-z]+:)?(\/\/)?[^\/]+).*$/)[1] : "";
//    var linkDomain = (linkServer != "") ? _bamGA.getDomain(linkServer) : "";
//    var inDomains = (jQuery.inArray(linkDomain, bamGAcrossDomains) >= 0) ? true : false;
//    var isThisDomain = (_bamGA.thisDomain == linkDomain) ? true : false;

    var elEv = [];
    elEv.delay = (el.attr('target') == undefined || el.attr('target').toLowerCase() != '_blank') ? true : false;

    if (!href.match(/^javascript:/i)

//        && !curOnClick.match(/_bamGA.track.*Redirect/)
        )
    {
      var trace = true;

      var filetypes = /\.(zip|exe|dmg|pdf|doc*|xls*|ppt*|mp3|slxb|pps*|vsd|vxd|txt|rar|wma|mov|avi|wmv|flv|wav)$/i;
      var baseHref = '';
//      if (linkDomain != "" && inDomains && !isThisDomain) {
//        _gaq.push(function () {
//          var t = _gat._getTrackerByName();
//          el.attr('href', t._getLinkerUrl(el.attr('href')));
//        });
//      }

      //outbound - non-interaction event
//      if (href.match(/^https?\:/i) && !isThisDomain && !inDomains) {
//        elEv.category = "external";
//        elEv.action = "click";
//        elEv.label = href.replace(/^https?\:\/\//i, '');
//        elEv.value = undefined;
//        elEv.non_i = true;
//        elEv.loc = href;
//      }
      //redirect script tracking
//      else if (typeof(bamGAreDirect) != 'undefined' && href.match(bamGAreDirect)) {
//        elEv.category = "external";
//        elEv.action = "redirect";
//        elEv.label = _bamGA.getParameterInURL(href, bamGAreDirectPar).replace(/^https?\:\/\//i, '');
//        elEv.value = undefined;
//        elEv.non_i = true;
//        elEv.loc = href;
//      }
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
         alert(extension);
        elEv.category = "download";
        elEv.action = "click-" + extension;
        elEv.label = href;
        elEv.value = undefined;
        elEv.non_i = false;
        elEv.loc = baseHref + href;
      }
      else if (this.host !== location.host) {
        elEv.category = "external";
        elEv.action = "click";
        elEv.label = href;
//        elEv.value = undefined;
//        elEv.non_i = true;
        elEv.loc = baseHref + href;
       }
//      //track link clicks on any page that has bamGATrackPageClicks set to true
//      else if (typeof(bamGATrackPageClicks) != 'undefined' && bamGATrackPageClicks == true) {
//        elEv.category = "click-tracking";
//        elEv.action = document.location.hostname + document.location.pathname;
//        elEv.label = href;
//        elEv.value = undefined;
//        elEv.non_i = true;
//        elEv.loc = baseHref + href;
//      }
      else trace = false;
      // perform _trackEvent
      el.click(function () {
        alert('elEv.category:' + elEv.category +':' + ' elEv.action:' + elEv.action +':'  + ' elEv.label:' + elEv.label +':'  + ' elEv.value:' + elEv.value +':'  + ' elEv.non_i:' + elEv.non_i );
//        if (trace) {
        ga('send', 'event',elEv.category, elEv.action, elEv.label, elEv.value, elEv.non_i);

//        _gaq.push(['_trackEvent', elEv.category, elEv.action, elEv.label, elEv.value, elEv.non_i]);
//          if (elEv.loc && elEv.delay) {
//            setTimeout(function () {
//              location.href = elEv.loc;
//            }, _bamGA.delay);
//            return false;
//          }

      });
    } // end if javascript or Redirect
  }); // end a.each

})(jQuery);
