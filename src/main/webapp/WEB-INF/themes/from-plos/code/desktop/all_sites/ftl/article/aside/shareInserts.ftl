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

<#include "../../common/twitterConfig.ftl"/>

<li><a href="https://www.linkedin.com/shareArticle?url=${urlParameter}&title=${titleFix}&summary=${"Checkout this article I found at PLOS"}"  id="shareLinkedIn" target="_blank" title="Add to LinkedIn"><img src="<@siteLink path="/resource/img/icon.linkedin.16.png"/>" width="16" height="16" alt="LinkedIn">LinkedIn</a></li>

<#-- mendeley, as per previous implementation. no current public
documentation can be found on their site or elsewhere. -->
<li><a href="https://www.mendeley.com/import/?url=${urlParameter}"  id="shareMendeley" target="_blank" title="Add to Mendeley"><img src="<@siteLink path="/resource/img/icon.mendeley.16.png"/>" width="16" height="16" alt="Mendeley">Mendeley</a></li>

<li><a href="https://www.pubchase.com/library?add_aid=${articleDoi}&source=plos"  id="sharePubChase" target="_blank" title="Add to PubChase"><img src="<@siteLink path="/resource/img/icon.pc.16.png"/>" width="16" height="16" alt="PubChase">PubChase</a></li>

<#if twitterHashTag?has_content>
  <li><a href="https://twitter.com/intent/tweet?url=${urlParameter}&text=${(twitterHashTag + ': ')?url + titleFix}" target="_blank" title="share on Twitter" id="twitter-share-link"><img src="<@siteLink path="/resource/img/icon.twtr.16.png"/>" width="16" height="16" alt="Twitter">Twitter</a></li>
</#if>

<li><a href="mailto:?subject=${titleFix}&body=I%20thought%20you%20would%20find%20this%20article%20interesting.%20From%20${journalTitle}:%20${urlParameter}"  id="shareEmail" rel="noreferrer" aria-label="Email"><img src="<@siteLink path="/resource/img/icon.email.16.png"/>" width="16" height="16" alt="Email">Email</a></li>
<@js src="resource/js/components/tweet140.js"/>
