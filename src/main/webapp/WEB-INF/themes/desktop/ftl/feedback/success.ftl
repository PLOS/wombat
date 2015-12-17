<html>
<#assign title = "" /> <#-- use default -->
<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="${journalStyle}">
<#include "../common/header/headerContainer.ftl" />

<div>
  <h1>Thank You</h1>

  <p><strong>
    Thanks for helping us make <@themeConfig map="journal" value="journalName" /> better!
  </strong></p>
</div>

<#include "../common/footer/footer.ftl" />
</body>
</html>
