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

<header class="title-block">

<#include "sectionLinkSettings.ftl" />
<#if isSectionLinkDisplayed("Metrics")>
  <#include "signposts.ftl" />
</#if>

    <div class="article-meta">
    <#include "articleClassifications.ftl" />
    </div>
    <div class="article-title-etc">

    <#include "articleTitle.ftl" />

      <ul class="date-doi">
      <#if revisionMenu.revisions?size gt 1>
          <li class="revisionList">
            <#include "revision/revisionMenu.ftl" />
          </li>
        </#if>
        <li id="artPubDate">Published: <@formatJsonDate date="${article.publicationDate}" format="MMMM d, yyyy" /></li>
        <li id="artDoi">
              <#include "../macro/doiAsLink.ftl" />
              <@doiAsLink article.doi />
        </li>
        <#if article.preprintDoi?has_content>
          <li class="published">
            <a href="http://dx.doi.org/${article.preprintDoi}">&gt;&gt; See the published version</a>
          </li>
        </#if>
      </ul>

    </div>
  <div>

  </div>
</header>

