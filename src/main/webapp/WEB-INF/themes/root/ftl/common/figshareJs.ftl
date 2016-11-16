<@themeConfig map="article" value="showFigShare" ; showFigShare>
    <#if !(showFigShare?? && showFigShare.disabled?? && showFigShare.disabled)>
        <@js src="resource/js/components/figshare.js" />
    </#if>
</@themeConfig>