<div class="article-block">
<@siteLink handlerName="article" queryParameters={"id":article.doi} ; articleUrl>
      <#if (article.strkImgURI?? && article.strkImgURI?length > 0) >
          <a id="articleURL" href="${articleUrl}">
              <@siteLink handlerName="figureImage" queryParameters={"id" : article.strkImgURI, "size": "inline"} ; href>
                  <img class="grayscale" src="${href}"/>
              </@siteLink>
          </a>
      <#else>
        <a id="articleURL" href="${articleUrl}">
          <@siteLink handlerName="staticResource" wildcardValues=["img/generic-article-image.png"] ; href>
              <img src="${href}"/>
          </@siteLink>
        </a>
      </#if>
    </@siteLink>

    <div class="details">
    <h2 class="title">
        <#include "../article/articleTruncateTitle.ftl" />
        <@siteLink handlerName="article" queryParameters={"id":article.doi} ; href>
            <a href="${href}" title="${article.title}"><@truncateTitle article.title></@></a>
        </@siteLink>
    </h2>
        <#include "../article/authorListHomepage.ftl" />
        <@authorList article.authors></@authorList>
    </div><!-- /.details -->
  <ul class="actions">
      <#--@TODO: Remove alerts and redirect to lightbox-->
    <li><a data-doi="${article.doi}" class="abstract" onclick="alert('Should redirect to lightbox.')">Abstract</a></li>
    <li><a data-doi="${article.doi}" class="figures" onclick="alert('Should redirect to lightbox.')">Figures</a></li>
    <@siteLink handlerName="article" queryParameters={"id":article.doi} ; href>
      <li class="last"><a href="${href}" onclick="alert('Should redirect to lightbox.')">Full Text</a></li>
    </@siteLink>
  </ul>
</div>