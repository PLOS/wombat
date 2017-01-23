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

<div class="article-block">
<@siteLink handlerName="article" queryParameters={"id":article.doi} ; articleUrl>
      <#if (article.strkImgURI?? && article.strkImgURI?length > 0) >
          <a class="article-url"  href="${articleUrl}">
              <@siteLink handlerName="figureImage" queryParameters={"id" : article.strkImgURI, "size": "inline"} ; href>
                  <img class="grayscale" src="${href}"/>
              </@siteLink>
          </a>
      <#else>
        <a class="article-url" href="${articleUrl}">
          <@siteLink handlerName="staticResource" wildcardValues=["img/generic-article-image.png"] ; href>
              <img src="${href}"/>
          </@siteLink>
        </a>
      </#if>
    </@siteLink>

    <div class="details">
    <h2 class="title">
        <@siteLink handlerName="article" queryParameters={"id":article.doi} ; href>
            <a href="${href}" title="${article.title}">${article.title}</a>
        </@siteLink>
    </h2>
        <#include "../article/authorListHomepage.ftl" />
        <@authorList article.authors></@authorList>
    </div><!-- /.details -->
  <ul class="actions">
  <#--@TODO: When able to launch lightbox from here, uncomment, remove alerts, and redirect to lightbox
    <li><a data-doi="${article.doi}" class="abstract" onclick="alert('Should redirect to lightbox.')">Abstract</a></li>
    <li><a data-doi="${article.doi}" class="figures" onclick="alert('Should redirect to lightbox.')">Figures</a></li>
    <@siteLink handlerName="article" queryParameters={"id":article.doi} ; href>
      <li class="last"><a class="full-text" href="${href}" onclick="alert('Should redirect to lightbox.')">Full Text</a></li>
    </@siteLink>
  -->
  </ul>
</div>
<@js src="resource/js/vendor/jquery.dotdotdot.js" />
<@js src="resource/js/components/article_card.js" />
