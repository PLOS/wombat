<nav>
  <ul class="article-buttons">
  <#if article.figures?? && article.figures?size &gt; 0 >
    <li class="menuitem">
      <a class="btn-lg" href="<@siteLink handlerName="figuresPage" queryParameters={"id": article.doi} />">
        <span class="arrow">View</span>
        Figures (${article.figures?size})
      </a>
    </li>
  </#if>
  <#if article.commentCount.root &gt; 0>
    <li class="menuitem">
      <a class="btn-lg" href="<@siteLink handlerName="articleComments" queryParameters={"id": article.doi} />">
        <span class="arrow">View</span>
        Reader Comments (${article.commentCount.root})
      </a>
    </li>
  </#if>
    <li class="menuitem">
      <a class="btn-lg" href="<@siteLink handlerName="articleAuthors" queryParameters={"id": article.doi} />">
        <span class="arrow">View</span>
        About the Authors
      </a>
    </li>
      <li class="menuitem">
        <a class="btn-lg" href="<@siteLink handlerName="articleMetrics" queryParameters={"id": article.doi} />">
          <span class="arrow">View</span>
          Metrics
        </a>
      </li>
  <li class="menuitem">
    <a class="btn-lg" href="<@siteLink handlerName="articleRelatedContent" queryParameters={"id": article.doi} />">
      <span class="arrow">View</span>
      Related Content
    </a>
  </li>
  </ul>
</nav><#--end article buttons-->

<nav class="article-save-options">

<#macro articleSaveButton address>
  <div class="button-row">
    <a class="rounded coloration-white-on-color full" href="${address}">
      <#nested/>
    </a>
  </div>
</#macro>

<@siteLink handlerName="asset" queryParameters={"id": "${article.doi}.PDF"} ; href>
  <@articleSaveButton href>Download Article (PDF)</@articleSaveButton>
</@siteLink>

<@siteLink handlerName="citationDownloadPage" queryParameters={"id": article.doi} ; href>
  <@articleSaveButton href>Download Citation</@articleSaveButton>
</@siteLink>

</nav><#--end article-save-options-->
</article>

<#include "../common/bottomMenu/bottomMenu.ftl" />
</div><#--end content-->

<#include "../common/footer/footer.ftl" />

<section id="article-info-window" class="modal-info-window">

  <div class="modal-header clearfix">
    <a class="close coloration-text-color">v</a>
  </div>

  <div class="modal-content">

  </div>

  <a href="#" class="modal-search square-full coloration-white-on-color">search for this author</a>

</section><#--end model info window-->

<div id="container-main-overlay"></div>

</div><#--end container main-->

<#include "../common/siteMenu/siteMenu.ftl" />

<#include "../common/bodyJs.ftl" />
