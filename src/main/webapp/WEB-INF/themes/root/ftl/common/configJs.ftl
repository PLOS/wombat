<script type="text/javascript">
    var WombatConfig = WombatConfig || {};
    WombatConfig.resourcePath = "<@siteLink handlerName="staticResource" wildcardValues=[""]/>";
    WombatConfig.imgPath = "<@siteLink handlerName="staticResource" wildcardValues=["img/"]/>";
    <#if journalKey??>
    WombatConfig.journalKey = <@themeConfig map="journal" value="journalKey" ; journalKey>"${journalKey}"</@themeConfig>;
    </#if>
    <@siteLink handlerName="assetFile" failQuietly=true ; assetFilePath>
        <#if assetFilePath??>
        WombatConfig.figurePath = "${assetFilePath}";
        </#if>
    </@siteLink>
</script>
<#include "journalConfigJS.ftl"/>
