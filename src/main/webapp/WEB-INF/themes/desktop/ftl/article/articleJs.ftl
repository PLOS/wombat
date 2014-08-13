<@js src="resource/js/global.js" />

<#--The rem  polyfill rewrites the rems in to pixels. I don't think we can call this using the asset manager. -->
<!--[if IE 8]>
<script src="<@siteLink path="resource/js/vendor/rem.min.js"/>"></script>
<![endif]-->


<@renderJs />