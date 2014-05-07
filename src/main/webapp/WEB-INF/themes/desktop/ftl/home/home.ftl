<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#include "../common/head.ftl" />
<body class="home">

<!-- for LEMUR: block begins here: -->

<#include "blocks/stub-data.ftl" />
<#include "../common/header/header.ftl" />
<!-- for LEMUR: block begins here: -->
<div id="home-content">
  <div class="row hero">
    <#include "blocks/hero.ftl" />
    <#include "blocks/banner.ftl" />
  </div>
  <div class="row one">
    <#include "blocks/editorialBlockMedium.ftl" />
    <#include "blocks/articleList.ftl" />
    <#include "blocks/currentIssue.ftl" />
  </div>

  <div class="row two">
    <#include "blocks/editorialBlockLarge.ftl" />
    <#include "blocks/editorialBlockLarge.ftl" />
  </div>

  <div class="row three">

    <div class="column left">
     <#include "blocks/editorialBlockLarge.ftl" />
     <#include "blocks/editorialBlockLarge.ftl" />
     <#include "blocks/editorialBlockMedium.ftl" />
     <#include "blocks/editorialBlockMedium.ftl" />
     <#include "blocks/editorialBlockMedium.ftl" />
     <#include "blocks/editorialBlockMedium.ftl" />
    </div>
    <div class="column right">
      <#include "blocks/submissionLinks.ftl" />
      <#include "blocks/socialLinks.ftl" />
      <#include "blocks/twitter.ftl" />
      <#include "blocks/blogs.ftl" />
    </div>

  </div>

</div>
<!-- for LEMUR: block ends here -->
<div class="spotlight"></div>

<#include "../common/footer/footer.ftl" />

<script type="text/javascript" src="resource/js/vendor/jquery-1.11.0.js"></script>

</body>
</html>
