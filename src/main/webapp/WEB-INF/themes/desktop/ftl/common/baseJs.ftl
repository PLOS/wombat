<#include "configJs.ftl" />
<#--//This file gets called in the head.ftl because it should sit above other js files- the actual Js gets printed in the bottom of the body. -->
<script src="//code.jquery.com/jquery-2.1.4.min.js" ></script>
<script>window.jQuery || document.write('<script src="<@siteLink path="resource/js/vendor/jquery-2.1.4.min.js" />""><\/script>')</script>

<#if article??>
    <@themeConfig map="article" value="showFigShare" ; showFigShare>
        <#if (showFigShare?? && showFigShare)>
          <script type="text/javascript" src="https://widgets.figshare.com/static/figshare.js"></script>
        </#if>
    </@themeConfig>
</#if>

<#--//
fast click is required for mobile interactions for foundation.
-->
<@js src="resource/js/vendor/fastclick/lib/fastclick.js"/>
<@js src="resource/js/vendor/foundation/foundation.js"/>
<@js src="resource/js/vendor/underscore-min.js"/>
<@js src="resource/js/vendor/underscore.string.min.js"/>
<@js src="resource/js/vendor/moment.js" />


<#--//
The jQuery UI effects core is needed for some components
-->
<@js src="resource/js/vendor/jquery-ui-effects.min.js" />

<#--//include foundation js widgets here *
* unless you are absolutely sure they will only be in one scenario - then you
should include it in the ftl file that needs it. -->
<@js src="resource/js/vendor/foundation/foundation.tooltip.js"/>
<@js src="resource/js/vendor/foundation/foundation.dropdown.js"/>
<@js src="resource/js/vendor/foundation/foundation.tab.js" />
<@js src="resource/js/vendor/foundation/foundation.reveal.js"/>
<@js src="resource/js/vendor/foundation/foundation.slider.js"/>
<@js src="resource/js/vendor/imagesLoaded.js" />

<#-- // include components used universally here -->
<@js src="resource/js/components/toggle.js"/>
<@js src="resource/js/components/truncate_elem.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>




