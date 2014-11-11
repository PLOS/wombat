<#setting url_escaping_charset="UTF-8">
<#assign urlFix = "http://dx.doi.org/${article.doi}"?url,
titleFix = article.title?replace("<[^>]*>", "", "r") />

<div class="share-article" id="shareArticle" data-js-tooltip-hover="trigger">
  Share
  <ul data-js-tooltip-hover="target" class="share-options">

  <#-- reddit, as per <http://www.reddit.com/buttons/> but modified to not use JS for encoding -->
    <li><a href="http://www.reddit.com/submit?url=${urlFix}" id="shareReddit" target="_blank" title="Submit to Reddit"><img src="<@siteLink path="/resource/img/icon.reddit.16.png"/>" width="16" height="16" alt="Reddit">Reddit</a></li>

  <#-- google plus, as per <https://developers.google.com/+/plugins/share/#sharelink>  -->
    <li><a href="https://plus.google.com/share?url=${urlFix}" id="shareGoogle" target="_blank" title="Share on Google+"><img src="<@siteLink path="/resource/img/icon.gplus.16.png"/>" width="16" height="16" alt="Google+">Google+</a></li>

  <#-- stumbleupon, as per previous implementation. no current public
  documentation can be found on their site or elsewhere. -->
    <li><a href="http://www.stumbleupon.com/submit?url=${urlFix}"  id="shareStumble" target="_blank" title="Add to StumbleUpon"><img src="<@siteLink path="/resource/img/icon.stumble.16.png"/>" width="16" height="16" alt="StumbleUpon">StumbleUpon</a></li>

  <#-- facebook, as per previous implementation which uses the now
  deprecated share.php (which redirects to /sharer/sharer.php) -->
    <li><a href="http://www.facebook.com/share.php?u=${urlFix}&t=${titleFix}" id="shareFacebook" target="_blank" title="Share on Facebook"><img src="<@siteLink path="/resource/img/icon.fb.16.png"/>" width="16" height="16" alt="Facebook">Facebook</a></li>

  <#include 'shareInserts.ftl' />
  </ul>
</div>