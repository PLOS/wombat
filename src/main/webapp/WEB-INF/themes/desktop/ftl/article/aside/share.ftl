<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<#setting url_escaping_charset="UTF-8">
<#include "../../macro/doiResolverLink.ftl" />
<#assign urlParameter = doiResolverLink(article.doi)?url,
titleFix = article.title?replace("<[^>]*>", "", "r") />

<#-- There are some old articles that have titles saved in the DB with long stretches
     of spaces and/or newlines.  This appears to be due to an old ingestion bug
     (that has been fixed).                                                  -->
<#assign titleFix = titleFix?replace("\\n", " ", "r") />
<#assign titleFix = titleFix?replace("\\s{2,}", " ", "r") />

<div class="share-article" id="shareArticle" data-js-tooltip-hover="trigger">
  Share
  <ul data-js-tooltip-hover="target" class="share-options">

  <#-- reddit, as per <http://www.reddit.com/buttons/> but modified to not use JS for encoding -->
    <li><a href="http://www.reddit.com/submit?url=${urlParameter}" id="shareReddit" target="_blank" title="Submit to Reddit"><img src="<@siteLink path="/resource/img/icon.reddit.16.png"/>" width="16" height="16" alt="Reddit">Reddit</a></li>

  <#-- google plus, as per <https://developers.google.com/+/plugins/share/#sharelink>  -->
    <li><a href="https://plus.google.com/share?url=${urlParameter}" id="shareGoogle" target="_blank" title="Share on Google+"><img src="<@siteLink path="/resource/img/icon.gplus.16.png"/>" width="16" height="16" alt="Google+">Google+</a></li>

  <#-- stumbleupon, as per previous implementation. no current public
  documentation can be found on their site or elsewhere. -->
    <li><a href="http://www.stumbleupon.com/submit?url=${urlParameter}"  id="shareStumble" target="_blank" title="Add to StumbleUpon"><img src="<@siteLink path="/resource/img/icon.stumble.16.png"/>" width="16" height="16" alt="StumbleUpon">StumbleUpon</a></li>

  <#-- facebook, as per previous implementation which uses the now
  deprecated share.php (which redirects to /sharer/sharer.php) -->
    <li><a href="http://www.facebook.com/share.php?u=${urlParameter}&t=${titleFix}" id="shareFacebook" target="_blank" title="Share on Facebook"><img src="<@siteLink path="/resource/img/icon.fb.16.png"/>" width="16" height="16" alt="Facebook">Facebook</a></li>

  <#include 'shareInserts.ftl' />
  </ul>
</div>