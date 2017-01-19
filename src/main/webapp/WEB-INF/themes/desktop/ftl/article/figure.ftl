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

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = article.title, articleDoi = article.doi />
<#assign tabPage="Article"/>

<#assign adPage="Article"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="article ${journalStyle}">
<#assign title = figure.title />

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">

  <section class="body-section">

    <p><a class="back" href="<@siteLink handlerName="article" queryParameters={"id":article.doi} />" >< Back to Article</a></p>

    <h1 id="artTitle"><@xform xml=article.title/></h1>

    <h2>${figure.title}</h2>

    <p>
    ${descriptionHtml}
    </p>

    <div>
      <img class="figure-img"
           src="<@siteLink handlerName="assetFile" queryParameters=(figurePtr + {'type': 'large'}) />"
           alt="${figure.title}">
    </div>
    <#include "../macro/doiAsLink.ftl" />
    <p class="article-id">
     doi:  <@doiAsLink figure.doi />
    </p>


</div>

</section>
<#include "../common/footer/footer.ftl" />


<@renderJs />

<#--
TODO: move reveal mode & fig-viewer divs to global location when the new lightbox is implemented sitewide
-->
</body>
</html>
