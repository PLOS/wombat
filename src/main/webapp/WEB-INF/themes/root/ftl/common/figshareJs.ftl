<@themeConfig map="article" value="showFigShare" ; showFigShare>
    <#if (showFigShare?? && showFigShare)>
        <@js src="resource/js/components/figshare.js" />
    </#if>
</@themeConfig>