<#include "stage.ftl" />
<#include "laterExists.ftl" />

<#if showStageNotice() || showLaterExistsNotice()>
<div class="amendment amendment-uncorrected-proof">
  <#if showStageNotice()>
    <@stageNotice/>
  </#if>

  <#if showLaterExistsNotice()>
  <@laterExistsNotice/>
</#if>

</div>
</#if>
