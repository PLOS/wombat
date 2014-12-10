<#--//This file gets called in the head.ftl because it should sit above other js files- the actual Js gets printed in the bottom of the body. -->
<@js src="resource/js/vendor/jquery-1.11.0.js" />
<#--//
fast click is required for mobile interactions for foundation.
-->
<@js src="resource/js/vendor/fastclick/lib/fastclick.js"/>
<@js src="resource/js/vendor/foundation/foundation.js"/>

<#--//include foundation js widgets here *
* unless you are absolutely sure they will only be in one scenario - then you
should include it in the ftl file that needs it. -->
<@js src="resource/js/vendor/foundation/foundation.tooltip.js"/>
<@js src="resource/js/vendor/foundation/foundation.tab.js" />
<@js src="resource/js/vendor/foundation/foundation.reveal.js"/>
<@js src="resource/js/vendor/foundation/foundation.slider.js"/>
<@js src="resource/js/vendor/imagesLoaded.js" />

<#-- // include components used universally here -->
<@js src="resource/js/components/toggle.js"/>
<@js src="resource/js/components/truncate_elem.js"/>




