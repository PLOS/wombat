<#macro buildInfo infoObj desc>
<#-- The element below is intended to be an HTML comment (!), not a FreeMarker comment (#).
     We want it to be visible in production if you inspect the page source. -->
<!--
  Build properties for ${desc}:
    version: ${infoObj.version}
    date:    ${infoObj.date}
    user:    ${infoObj.user}
  -->
</#macro>
<#if localBuildInfo??>
  <@buildInfo localBuildInfo 'display component ("Wombat")' />
</#if>
<#if serviceBuildInfo??>
  <@buildInfo serviceBuildInfo 'service component ("Rhino")' />
</#if>
