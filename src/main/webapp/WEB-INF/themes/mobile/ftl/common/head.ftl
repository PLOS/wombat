<#include "../macro/pathUp.ftl" />
<#include "../macro/removeTags.ftl" />
<#include "title/titleFormat.ftl" />

<head>
  <meta charset="utf-8">
  <meta name="description" content="">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style type='text/css'>
    @-ms-viewport {
      width: device-width;
    }

    @-o-viewport {
      width: device-width;
    }

    @viewport {
      width: device-width;
    }
  </style>

  <title><@titleFormat removeTags(title) /></title>
<@cssLink target="resource/css/base.css" />
<@cssLink target="resource/css/interface.css" />
<@cssLink target="resource/css/mobile.css" />
<#include "../cssLinks.ftl" />

  <script src="${pathUp(depth!0 "resource/js/vendor/vendor.min.js")}"></script>
<@renderCssLinks />
</head>