<#include "../../common/hashTag.ftl" />
<#setting url_escaping_charset="UTF-8">
<#assign urlFix = article.url?url, titleFix = article.title?replace("<[^>]*>", "", "r")/>

<div class="share-article" id="shareArticle" data-js-tooltip-hover="trigger">
  Share
  <ul data-js-tooltip-hover="target" class="share-options">

  <#-- reddit, as per <http://www.reddit.com/buttons/> but modified to not use JS for encoding -->
    <li><a href="http://www.reddit.com/submit?url=${urlFix}" id="shareReddit" target="_blank" title="Submit to Reddit"><img src="<@siteLink path="/resource/img/icon.reddit.16.png"/>" width="16" height="16" alt="Reddit">Reddit</a></li>

  <#-- google plus, as per <https://developers.google.com/+/plugins/share/#sharelink>  -->
    <li><a href="https://plus.google.com/share?url=${urlFix}"  id="shareGoogle" target="_blank" title="Share on Google+"><img src="<@siteLink path="/resource/img/icon.gplus.16.png"/>" width="16" height="16" alt="Google+">Google+</a></li>

  <#-- stumbleupon, as per previous implementation. no current public
  documentation can be found on their site or elsewhere. -->
    <li><a href="http://www.stumbleupon.com/submit?url=${urlFix}"  id="shareStumble" target="_blank" title="Add to StumbleUpon"><img src="<@siteLink path="/resource/img/icon.stumble.16.png"/>" width="16" height="16" alt="StumbleUpon">StumbleUpon</a></li>

  <#-- facebook, as per previous implementation which uses the now
  deprecated share.php (which redirects to /sharer/sharer.php) -->
    <li><a href="http://www.facebook.com/share.php?u=${urlFix}&amp;t=${titleFix}" id="shareFacebook" target="_blank" title="Share on Facebook"><img src="<@siteLink path="/resource/img/icon.fb.16.png"/>" width="16" height="16" alt="Facebook">Facebook</a></li>

  <#-- linkedin. copy / pasted implementation from another site -->
    <li><a href="http://www.linkedin.com/shareArticle?url=${urlFix}&title=${titleFix}&summary=${"Checkout this article I found at PLOS"}"  id="shareLinkedIn" target="_blank" title="Add to LinkedIn"><img src="<@siteLink path="/resource/img/icon.linkedin.16.png"/>" width="16" height="16" alt="LinkedIn">LinkedIn</a></li>

  <#-- citeulike, as per <http://www.citeulike.org/bookmarklets.adp>
  and <http://wiki.citeulike.org/index.php/Organizing_your_library#Any_other_posting_tricks.3F> -->
    <li><a href="http://www.citeulike.org/posturl?url=${urlFix}&amp;title=${titleFix}"  id="shareCiteULike" target="_blank" title="Add to CiteULike"><img src="<@siteLink path="/resource/img/icon.cul.16.png"/>" width="16" height="16" alt="CiteULike">CiteULike</a></li>

  <#-- mendeley, as per previous implementation. no current public
  documentation can be found on their site or elsewhere. -->
    <li><a href="http://www.mendeley.com/import/?url=${urlFix}"  id="shareMendeley" target="_blank" title="Add to Mendeley"><img src="<@siteLink path="/resource/img/icon.mendeley.16.png"/>" width="16" height="16" alt="Mendeley">Mendeley</a></li>

  <#-- PubChase, as per <https://developer.plos.org/jira/browse/AMEC-1999>,
   no public documentation can be found on their site or elsewhere -->
    <li><a href="https://www.pubchase.com/library?add_aid=${articleDoi}&amp;source=plos"  id="sharePubChase" target="_blank" title="Add to PubChase"><img src="<@siteLink path="/resource/img/icon.pc.16.png"/>" width="16" height="16" alt="PubChase">PubChase</a></li>

    <li><a href="http://twitter.com/intent/tweet?text=${hashTag?url + ': ' + titleFix?url + ' ' + article.url}" id="shareTwitter" target="_blank" title="Share on Twitter" id="twitter-share-link"><img src="<@siteLink path="/resource/img/icon.twtr.16.png"/>" width="16" height="16" alt="Twitter">Twitter</a></li>

    <li><a href="${legacyUrlPrefix + "article/email" + articleId?url}" id="shareEmail" title="Email this article"><img src="<@siteLink path="/resource/img/icon.email.16.png"/>" width="16" height="16" alt="Email">Email</a></li>

  </ul>
</div>
<@js src="resource/js/components/twitter140.js"/>