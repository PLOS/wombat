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

<#if imageCode??>
<hr/>
<div>
<#-- Must embed the image data this way instead of having it at an href.
     There's no URL at which we could put the image without colliding with the site namespace.
  -->
  <img src="data:image/jpg;base64,${imageCode}"/>
</div>
<div style="font-size: small">
  Image:
  <a href="http://commons.wikimedia.org/wiki/File:Wombat_1.jpg">"Southern Hairy-nosed Wombat (<i>Lasiorhinus
    latifrons</i>)"</a>
  by <a href="http://www.flickr.com/photos/stygiangloom/220992362/">Stygiangloom</a>.
  Available under the
  <a href="http://creativecommons.org/licenses/by/2.0/deed.en">Creative Commons Attribution 2.0 Generic License.</a>
</div>
</#if>

</body>
</html>
