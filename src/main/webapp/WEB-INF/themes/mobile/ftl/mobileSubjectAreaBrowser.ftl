<#include 'baseTemplates/base.ftl'>

<@themeConfig map="journal" value="journalKey" ; journalKey>
  <#assign journalKey =journalKey/>
</@themeConfig>

<#macro page_content>
<div id="browse-content" class="content">
  <div id="browse-container"></div>
</div>

<div id="subject-list-template" style="display: none;">
  <nav class="browse-level active">
    <ul class="browse-list">__TAXONOMY_LINKS__</ul>
  </nav>
</div>

<div id="subject-term-template" style="display: none;">
  <li>
    <a href="browse/__TAXONOMY_TERM_ESCAPED__?filterJournals=${journalKey}" class="browse-link
      browse-left">__TAXONOMY_TERM_LEAF__</a>
    <a class="__CHILD_LINK_STYLE__" data-term="__TAXONOMY_TERM_FULL_PATH__">
      View Subcategories
      <span class="arrow">arrow</span>
    </a>
  </li>
</div>
  <#include "common/configJs.ftl" />
</#macro>

<@render_page '' 'PLOS - Browse' 'page-browse' />
