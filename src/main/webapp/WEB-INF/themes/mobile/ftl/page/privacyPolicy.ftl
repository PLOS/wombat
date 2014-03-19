<#include "../macro/pathUp.ftl" />
<#include "../common/htmlTag.ftl" />

<head>
  <title>Privacy Policy</title>

<@cssLink target=pathUp(3, "static/css/base.css") />
<@cssLink target="../static/css/interface.css" />
<@cssLink target="../static/css/mobile.css" />
<#include "../cssLinks.ftl" /><#-- TODO: Debug (this is at the wrong relative path level) -->

  <script src="../static/js/vendor/modernizr.custom.25437.js"></script>
  <script src="../static/js/vendor/respond.min.js"></script>

</head>
<body>
<#assign depth = 1 />
<#include "../common/header.ftl" />
<#include "privacyPolicyText.ftl" />
</body>
</html>
