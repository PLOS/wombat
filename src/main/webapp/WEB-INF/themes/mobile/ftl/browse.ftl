<#include "common/htmlTag.ftl" />

<#assign title = "PLOS - Browse" />
<#assign depth = 0 />
<#include "common/head.ftl" />

<body id="page-browse" class="plosone">

<div id="container-main">
<#include "common/header/header.ftl" />

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
      <a href="search?subject=__TAXONOMY_TERM_ESCAPED__" class="browse-link browse-left">__TAXONOMY_TERM_LEAF__</a>
      <a class="__CHILD_LINK_STYLE__" data-term="__TAXONOMY_TERM_FULL_PATH__">
        View Subcategories
        <span class="arrow">arrow</span>
      </a>
    </li>
  </div>

<#include "common/footer/footer.ftl" />

</div><#-- end container-main -->

<#include "common/siteMenu/siteMenu.ftl" />

<#include "common/bodyJs.ftl" />
</body>
</html>
