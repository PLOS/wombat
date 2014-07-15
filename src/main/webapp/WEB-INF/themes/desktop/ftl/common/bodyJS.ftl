<@js src="resource/js/vendor/jquery-1.11.0.js"/>
<#--TODO: do we want to use the cdn ? Punt for performance? -->
<#--This polyfill is so that IE8 can use rems. I don't think we can call this using the asset manager. -->


<@js src="resource/js/vendor/jquery.carousel.js"/>
<@js src="resource/js/components/carousel.js"/>
<@js src="resource/js/vendor/jquery.dotdotdot.js"/>

<script type="text/javascript" src="https://www.google.com/jsapi" ></script>
<@js src="resource/js/components/blogfeed.js"/>
<!--TODO: the following need to be on all pages, not just home-->
<!--
TODO: foundation-altered.js is in use for development. Need to
 download a customized foundation.js that includes only what we're using
 (Remove javascript for topbar)
 -->
<@js src="resource/js/vendor/foundation-altered.js"/>
<@js src="resource/js/vendor/jquery.hoverIntent.js"/>

<@js src="resource/js/components/navsearch.js"/>
<#--This polyfill is so that IE8 can use rems. I don't think we can call this using the asset manager. -->

<!--[if IE 8]>
<script src="resource/js/vendor/rem.min.js"></script>
<![endif]-->

<!-- this is where the js gets rendered  -->
<@renderJs />
