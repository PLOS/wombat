<#setting url_escaping_charset="UTF-8">
<#include "../../macro/doiResolverLink.ftl" />
<#assign urlParameter = doiResolverLink(article.doi)?url,
titleFix = article.title?replace("<[^>]*>", "", "r"), journalTitle = article.journal.title />

<#-- There are some old articles that have titles saved in the DB with long stretches
     of spaces and/or newlines.  This appears to be due to an old ingestion bug
     (that has been fixed).                                                  -->
<#assign titleFix = titleFix?replace("\\n", " ", "r") />
<#assign titleFix = titleFix?replace("\\s{2,}", " ", "r") />


<#-- reddit, but modified to not use JS for encoding -->
<li><a href="https://www.reddit.com/submit?url=${urlParameter}" id="shareReddit" target="_blank" title="Submit to Reddit"><img src="<@siteLink path="/resource/img/icon.reddit.16.png"/>" width="16" height="16" alt="Reddit">Reddit</a></li>

<#-- facebook, as per previous implementation which uses the now
deprecated share.php (which redirects to /sharer/sharer.php) -->
<li><a href="https://www.facebook.com/share.php?u=${urlParameter}&t=${titleFix}" id="shareFacebook" target="_blank" title="Share on Facebook"><img src="<@siteLink path="/resource/img/icon.fb.16.png"/>" width="16" height="16" alt="Facebook">Facebook</a></li>
