<script type="text/javascript">
    var WombatConfig = WombatConfig || {};
    WombatConfig.resourcePath = "<@siteLink handlerName="staticResource" wildcardValues=[""]/>";
    WombatConfig.imgPath = "<@siteLink handlerName="staticResource" wildcardValues=["img/"]/>";
    <#if journalKey??>WombatConfig.journalKey = <@themeConfig map="journal" value="journalKey" ; journalKey>"${journalKey}"</@themeConfig>;</#if>
    WombatConfig.figurePath = "<@siteLink handlerName="assetFile" />";
</script>
<#include "journalConfigJS.ftl"/>
