<#-- Specific css file will be added by head.ftl template -->
<#assign cssFile = "issues.css" />

<#include "../common/headContent.ftl" />
<#include "../macro/doiResolverLink.ftl" />

  <div id="feedback-form-container">
  <#include "issuesBody.ftl"/>
  </div>
</div><#-- end home-content -->

<#include "../common/footContent.ftl" />