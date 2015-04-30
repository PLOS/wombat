This page has moved. Please click <a href="<@siteLink path='${defaultTarget}'/>">here</a> if you are not redirected automatically.

<script type="text/javascript">

    var redirects = JSON.parse("${redirects?js_string}");
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