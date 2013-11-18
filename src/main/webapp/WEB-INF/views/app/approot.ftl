<!DOCTYPE html>
<html>
<head>
  <title>Wombat Application Root</title>
</head>
<body>

<p>This is the root page for a Wombat application deployment.</p>

<p>This page is for development/debugging purposes only. On a live site, it should not be visible to end users.</p>

<p>Sites:</p>
<ul>
<#list siteKeys as site>
  <li><a href="${site}/">${site}</a></li>
</#list>
</ul>

</body>
</html>
