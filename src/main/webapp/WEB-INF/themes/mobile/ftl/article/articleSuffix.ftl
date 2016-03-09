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
  <#if article.commentCount &gt; 0>
    <li class="menuitem">
      <a class="btn-lg" href="<@siteLink handlerName="articleComments" queryParameters={"id": article.doi} />">
        <span class="arrow">View</span>
        Reader Comments (${article.commentCount})
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
  <div class="button-row">
    <a class="rounded coloration-white-on-color full"
       href="article/asset?id=${article.doi}.PDF">
      Download Article (PDF)</a>
  </div>

<#-- TODO: Implement non-MVP features.
     Below is how the three buttons for the originally designed features should look together.
     Note that "full" needs to be deleted from the <a> above if it is put inside a div.button-pair.

  <div class="button-row button-pair clearfix">
    <a class="rounded coloration-white-on-color"
       href="article/asset?id=${article.doi}.PDF">
      Download Article (PDF)</a>
    <a href="." class="rounded coloration-white-on-color">Email this Article</a>
  </div>
  <div class="button-row">
    <a href="." class="rounded full save-article" data-list-type="individual"><span class="plus">+</span>
      Add article to my list
    </a>
  </div>
-->

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
