<#include '../baseTemplates/base.ftl' />

<#macro page_content>
<div id="feedback-form-container">
  <#include "feedbackBody.ftl" />
</div>
</#macro>

<@render_page 'feedback.css' />