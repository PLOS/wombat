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

<#macro articleTypeLink siteContentTarget>
  <@siteLink handlerName="siteContent" pathVariables={"pageName": siteContentTarget} ; typeDescriptionLink>
  <p>
    <a href="${typeDescriptionLink}<#if articleType.code??>#${articleType.code}</#if>">
    ${articleType.name}
    </a>
  ${articleType.description}
  </p>

  <p><a href="${typeDescriptionLink}" class="type-seeall"><#nested/></a></p>
  </@siteLink>
</#macro>

<#macro setArticleType classType >
<div class="article-type" <#if articleType.description??>data-js-tooltip-hover="trigger"</#if>>
  <p class="${classType}" id="artType">${articleType.name}</p>
<#-- load type descriptions and tooltip container -->
  <#if articleType.description??>
    <div class="type-desc" data-js-tooltip-hover="target">
      <#include "articleTypeLink.ftl" /><#-- should invoke @articleTypeLink macro -->
    </div>
  </#if>
</div>
</#macro>

<div class="classifications">
  <p class="license-short" id="licenseShort">Open Access</p>
<#assign peerReviewedTypes = [
  "Research Article",
  "Meta-Research Article",
  "Short Reports",
  "Methods and Resources",
  "Preregistered Research Article",
  "Discovery Report",
  "Update Article",
  "Profiles",
  "Registered Report Protocol"
]>
<#if peerReviewedTypes?seq_contains(articleType.name)>
  <p class="peer-reviewed" id="peerReviewed">Peer-reviewed</p>
</#if>

<#if articleType.name=="Research Article">
  <@setArticleType classType="type-article" />

<#elseif articleType.name=="Correction">
  <@setArticleType classType="type-correction" />

<#elseif articleType.name=="Expression of Concern" || articleType.name=="Retraction">
  <@setArticleType classType="type-concern-retraction" />
<#else>
  <@setArticleType classType="article-type-tooltip" />

</#if>

</div>


