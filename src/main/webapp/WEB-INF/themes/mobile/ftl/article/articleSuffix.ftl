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
  <#if commentCount.root &gt; 0>
    <li class="menuitem">
      <a class="btn-lg" href="<@siteLink handlerName="articleComments" queryParameters={"id": article.doi} />">
        <span class="arrow">View</span>
        Reader Comments (${commentCount.root})
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

<#if articleItems[article.doi].files?keys?seq_contains("printable")>
  <@siteLink handlerName="assetFile" queryParameters=(articlePtr + {"type": "printable"}) ; href>
    <@articleSaveButton href>Download Article (PDF)</@articleSaveButton>
  </@siteLink>
</#if>

<@siteLink handlerName="citationDownloadPage" queryParameters={"id": article.doi} ; href>
  <@articleSaveButton href>Download Citation</@articleSaveButton>
</@siteLink>

<@siteLink handlerName="email" queryParameters={"id": article.doi} ; href>
  <@articleSaveButton href>Email this Article</@articleSaveButton>
</@siteLink>

</nav><#--end article-save-options-->
</article>
