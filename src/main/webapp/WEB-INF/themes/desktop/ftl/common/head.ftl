<#include "../macro/removeTags.ftl" />
<#include "title/titleFormat.ftl" />

<@themeConfig map="journal" value="journalKey" ; journalKey>
  <#assign journalKey = journalKey />
</@themeConfig>

<head prefix="og: http://ogp.me/ns#">
  <title><@titleFormat removeTags(title) /></title>

  <link rel="stylesheet" type="text/css" href="<@siteLink path="resource/css/screen.css" />"/>

  <script type="text/javascript" src="resource/js/vendor/modernizr-v2.7.1.js"></script>

  <link rel="shortcut icon" href="resource/img/favicon.ico" type="image/x-icon"/>
  <!--[if lt IE 9]>
  <!--the following scripts enable the use of foundation's dropdowns to work in IE8 -->
  <script src="//cdnjs.cloudflare.com/ajax/libs/html5shiv/3.6.2/html5shiv.js"></script>
  <script src="//cdnjs.cloudflare.com/ajax/libs/respond.js/1.1.0/respond.min.js"></script>
  <![endif]-->

  <#include "analytics.ftl" />

</head>
