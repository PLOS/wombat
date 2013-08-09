<#include "../macro/pathUp.ftl" />

<#-- Custom FreeMarker directives  -->
<#assign formatJsonDate = "org.ambraproject.wombat.util.Iso8601DateDirective"?new()>

<head>
  <meta charset="utf-8">
  <meta name="description" content="">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style type='text/css'>
    @-ms-viewport { width: device-width; }
    @-o-viewport { width: device-width; }
    @viewport { width: device-width; }
  </style>

  <title>${title}</title>
  <link rel="stylesheet" href=<@pathUp depth!0 "static/css/base.css" />>
  <link rel="stylesheet" href=<@pathUp depth!0 "static/css/interface.css" />>
  <link rel="stylesheet" href=<@pathUp depth!0 "static/css/mobile.css" />>
<#include "../cssLinks.ftl" />

  <script src=<@pathUp depth!0 "static/js/vendor/modernizr.custom.25437.js" />></script>
  <script src=<@pathUp depth!0 "static/js/vendor/respond.min.js" />></script>
</head>