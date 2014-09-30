<#include "../../common/hashTag.ftl" />
<#setting url_escaping_charset="UTF-8">

<div class="share-article" id="shareArticle" data-js-tooltip-hover="trigger">
    Share
    <ul data-js-tooltip-hover="target" class="share-options">
    <#--
    some notes about social media buttons:

    1) the current document's URL passed to the 'url' query parameter
    of the service needs to be escaped by freemarker via the '?url'
    string method. (the same goes for the title, though titles are
    usually determined by the service, so we (usually) avoid passing
    them.)

    2) we're avoiding using external scripts where possible to reduce
    external dependencies, which directly and negatively affect page
    load time.

    -JSB/DP

    -->

    <#-- reddit, as per <http://www.reddit.com/buttons/> but modified to not use JS for encoding -->
        <li><a href="http://www.reddit.com/submit?url=${article.url?url}" id="shareReddit" target="_blank" title="Submit to Reddit"><img src="<@siteLink path="/resource/img/icon.reddit.16.png"/>" width="16" height="16" alt="Reddit">Reddit</a></li>

    <#-- google plus, as per <https://developers.google.com/+/plugins/share/#sharelink>  -->
        <li><a href="https://plus.google.com/share?url=${article.url?url}"  id="shareGoogle" target="_blank" title="Share on Google+"><img src="<@siteLink path="/resource/img/icon.gplus.16.png"/>" width="16" height="16" alt="Google+">Google+</a></li>

    <#-- stumbleupon, as per previous implementation. no current public
    documentation can be found on their site or elsewhere. -->
        <li><a href="http://www.stumbleupon.com/submit?url=${article.url?url}"  id="shareStumble" target="_blank" title="Add to StumbleUpon"><img src="<@siteLink path="/resource/img/icon.stumble.16.png"/>" width="16" height="16" alt="StumbleUpon">StumbleUpon</a></li>

    <#-- facebook, as per previous implementation which uses the now
    deprecated share.php (which redirects to /sharer/sharer.php) -->
        <li><a href="http://www.facebook.com/share.php?u=${article.url?url}&amp;t=${article.title}"  id="shareFacebook" target="_blank" title="Share on Facebook"><img src="<@siteLink path="/resource/img/icon.fb.16.png"/>" width="16" height="16" alt="Facebook">Facebook</a></li>

    <#-- linkedin. copy / pasted implementation from another site -->
        <li><a href="http://www.linkedin.com/shareArticle?url=${article.url?url}&title=${article.title}&summary=${"Checkout this article I found at PLOS"}"  id="shareLinkedIn" target="_blank" title="Add to LinkedIn"><img src="<@siteLink path="/resource/img/icon.linkedin.16.png"/>" width="16" height="16" alt="LinkedIn">LinkedIn</a></li>

    <#-- citeulike, as per <http://www.citeulike.org/bookmarklets.adp>
    and <http://wiki.citeulike.org/index.php/Organizing_your_library#Any_other_posting_tricks.3F> -->
        <li><a href="http://www.citeulike.org/posturl?url=${article.url?url}&amp;title=${article.title}"  id="shareCiteULike" target="_blank" title="Add to CiteULike"><img src="<@siteLink path="/resource/img/icon.cul.16.png"/>" width="16" height="16" alt="CiteULike">CiteULike</a></li>

    <#-- mendeley, as per previous implementation. no current public
    documentation can be found on their site or elsewhere. -->
        <li><a href="http://www.mendeley.com/import/?url=${article.url?url}"  id="shareMendeley" target="_blank" title="Add to Mendeley"><img src="<@siteLink path="/resource/img/icon.mendeley.16.png"/>" width="16" height="16" alt="Mendeley">Mendeley</a></li>

    <#-- PubChase, as per <https://developer.plos.org/jira/browse/AMEC-1999>,
     no public documentation can be found on their site or elsewhere -->
        <li><a href="https://www.pubchase.com/library?add_aid=${article.doi}&amp;source=plos"  id="sharePubChase" target="_blank" title="Add to PubChase"><img src="<@siteLink path="/resource/img/icon.pc.16.png"/>" width="16" height="16" alt="PubChase">PubChase</a></li>


    <#-- twitter, as per previous implementation <http://www.saschakimmel.com/2009/05/how-to-create-a-dynamic-tweet-this-button-with-javascript/>,
    but slightly modified to work without an (evil) document.write call
    and updated to account for new twitter URL auto-shortening. in
    theory, this could/should be done in freemarker instead of via JS
    but, alas, my freemarker skills are not mad enuff. -JSB/DP -->
        <script type="text/javascript">
            // replace tweet with one that's pre-shortened to 140 chars
            function truncateTweetText() {
                var twtTitle = '${article.title?replace("\'", "\\\'")}';
                var twtUrl = '${article.url?replace("\'", "\\\'")}';
                // all URLs posted to twitter get auto-shortened to 20 chars.
                var maxLength = 140 - (20 + 1);
                // truncate the title to include space for twtTag and ellipsis (here, 10 = tag length + space + ellipsis)
                if (twtTitle.length > maxLength) { twtTitle = twtTitle.substr(0, (maxLength - 10)) + '...'; }
                // set the href to use the shortened tweet
                $('#twitter-share-link').prop('href', 'http://twitter.com/intent/tweet?text=' + encodeURIComponent('${hashTag}: ' + twtTitle + ' ' + twtUrl));
            }
        </script>
        <li><a href="http://twitter.com/intent/tweet?text=${hashTag + ': '?url + article.title?url + ' ' + article.url?url}" onclick="truncateTweetText();"  id="shareTwitter" target="_blank" title="Share on Twitter" id="twitter-share-link"><img src="<@siteLink path="/resource/img/icon.twtr.16.png"/>" width="16" height="16" alt="Twitter">Twitter</a></li>

        <li><a href="${legacyUrlPrefix + "article/email" + articleId?url}" id="shareEmail" title="Email this article"><img src="<@siteLink path="/resource/img/icon.email.16.png"/>" width="16" height="16" alt="Email">Email</a></li>

    </ul>
</div>