<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = article.title />
<#assign depth = 0 />

<#include "../common/head.ftl" />

  <body class="article ${journalKey?lower_case}">

  <input type="hidden" id="rawPubDate" value="${article.date}" />

  <#include "../common/header/header.ftl" />
  <div class="plos-row">
    <section class="article-body">
      <div class="classifications">
        <p class="license-short" id="licenseShort"><span class="icon"></span>Open Access</p>

        <#if article.articleType=="Research Article">
        <p class="peer-reviewed" id="peerReviewed"><span class="icon"></span>Peer-reviewed</p>
        </#if>

        <div class="article-type" id="artType">${article.articleType!""}</div>
      </div>

      <div class="article-text" id="artText">

        <h1 id="artTitle"> ${article.title} </h1>

        <#if article.authors??>
        <div id="artAuthors" class="article-authors">

              <#list authors as author>
                <div class="article-author">
                  ${author.fullName}
                  <#list author.affiliations as affiliation>
                    <p>${affiliation}</p>
                  </#list>
                </div>
              </#list>
          </div>
          </#if>

        <ul class="date-doi">
          <li id="artPubDate">Published:  </li>
          <li id="artDoi">DOI: ${article.doi}</li>
        </ul>
      <#-- developing the tabs here; will move to include when complete -->
        <ul class="article-tabs" data-tab>
          <li class="tab-title active"><a href="#tabArticle" class="tab-article" id="aTabArticle">Article</a></li>
        <#if article.authors??  && article.authors?size gt 0>
          <li class="tab-title"><a href="#tabAuthors" class="tab-authors" id="aTabAuthors">About the Authors</a></li>
        </#if>
          <li class="tab-title"><a href="#tabMetrics" class="tab-metrics"
                                   id="aTabMetrics">Metrics</a></li>
          <li class="tab-title"><a href="#tabComments" class="tab-comments" id="aTabComments">Comments</a></li>
          <li class="tab-title"><a href="#tabRelated" class="tab-related" id="aTabRelated">Related Content</a></li>
        </ul>
        <div class="tabs-content">
          <div class="content active" id="tabArticle">
            <p>article</p>
          </div>
          <div class="content" id="tabAuthors">
            <p>authors</p>
          </div>
          <div class="content" id="tabMetrics">
            <p>metrics</p>
          </div>
          <div class="content" id="tabComments">
            <p>comments</p>
          </div>
          <div class="content" id="tabRelated">
            <p>related content</p>
          </div>
        </div>
          ${articleText}

        </div>

      </div>
    </section>
    <aside class="stub article-column">

    </aside>
  </div>
  <#include "../common/footer/footer.ftl" />

  <@renderJs />
  <script src="<@siteLink path="resource/js/components/dateparse.js"/>"></script>
  <script src="<@siteLink path="resource/js/pages/article.js"/>"></script>

  </body>
</html>
