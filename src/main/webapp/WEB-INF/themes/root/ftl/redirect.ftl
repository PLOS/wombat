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
    for (var anchor_regex in redirects) {
        if (RegExp(anchor_regex).test("${sourcePage}" + current_anchor)) {
            target = redirects[anchor_regex];
            break;
        }
    }

    window.location.replace("<@siteLink path='" + target + "'/>");

</script>