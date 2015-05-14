This page has moved. Please click <a href="<@siteLink path='${defaultTarget}'/>">here</a> if you are not redirected automatically.


<#include "redirectList.ftl"/>

<#include "journalRedirectList.ftl"/>

<script type="text/javascript">

  // override with any journal-specific redirects
  for (var key in journal_redirects) {
    redirects[key] = journal_redirects[key];
  }

  var current_anchor = window.location.hash;
  var target = "${defaultTarget}";
  var source = "${sourcePage}";
  for (var anchor_regex in redirects) {
    if (RegExp(anchor_regex).test(source + current_anchor)) {
      target = redirects[anchor_regex];
      break;
    }
  }

  // Ajax URL should be set as a redirect in Apache. The redirect destination is arbitrary and
  // response is ignored. This request will allow logging of Javascript-based redirects as if
  // they were standard Apache redirects.
  var xmlhttp = new XMLHttpRequest();
  var url = "<@siteLink path=''/>" + 'logRedirect/' + encodeURIComponent(source)
      + '/' + encodeURIComponent(current_anchor.replace('#', ''));
  xmlhttp.open('POST', url, false /*async*/);
  xmlhttp.send();

  window.location.replace("<@siteLink path='" + target + "'/>");

</script>