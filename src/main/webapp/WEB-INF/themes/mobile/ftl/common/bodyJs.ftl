<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js"></script>
<script>window.jQuery || document.write('<script src=<@pathUp depth!0 "static/js/vendor/jquery-1.9.0.min.js" />><\/script>')</script>
<#assign jsSrc><@pathUp depth!0 "static/js/navigation.js" /></#assign>
<@js src=jsSrc />
<#assign jsSrc><@pathUp depth!0 "static/js/content.js" /></#assign>
<@js src=jsSrc />
<#assign jsSrc><@pathUp depth!0 "static/js/share.js" /></#assign>
<@js src=jsSrc />
<#assign jsSrc><@pathUp depth!0 "static/js/taxonomy.js" /></#assign>
<@js src=jsSrc />
<@renderJs />
