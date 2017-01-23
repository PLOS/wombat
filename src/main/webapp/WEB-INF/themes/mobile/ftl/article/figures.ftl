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

<#include "../baseTemplates/articleSection.ftl" />
<#assign title = article.title />
<#assign bodyId = 'page-figures' />
<#assign mainId = "figures-content" />
<#assign mainClass = "content" />

<@page_header />
<#list figures as figure>
  <#assign hasTitle = figure.title?? && figure.title?length gt 0 />
<figure class="figure-small">
  <figcaption>
    <#if hasTitle>
    ${figure.title}.
    </#if>
    <div class="figure-description">
    ${figure.descriptionHtml}
    </div>
    <a href="figure?id=${figure.doi}">More »</a>
  </figcaption>

  <a class="figure-link" href="figure?id=${figure.doi}">
    <@siteLink handlerName="figureImage" queryParameters={"size": "medium", "id": figure.doi} ; src>
      <img class="figure-image" src="${src}"
           alt="<#if hasTitle>${figure.title}</#if>"> <#-- Print alt="" if no title -->
      <span class="figure-expand">Expand</span>
    </@siteLink>
  </a>

</figure>
</#list>
<#include "../common/bottomMenu/bottomMenu.ftl" />
<@page_footer />
