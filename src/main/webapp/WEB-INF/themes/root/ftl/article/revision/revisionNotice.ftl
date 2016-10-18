<#include "stage.ftl" />
<#include "laterExists.ftl" />

<#if showStageNotice() || hasLaterVersion()>
<div class="amendment amendment-uncorrected-proof">
  <#if showStageNotice()>
    <@stageNotice/>
  </#if>

  <#if hasLaterVersion()>
  <@laterExistsNotice/>
</#if>

</div>
</#if>
