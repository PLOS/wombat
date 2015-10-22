<!DOCTYPE html>
<html>
<head>
  <title>Wombat Application Root</title>
</head>
<body>

<p>This is the root page for a Wombat application deployment.</p>

<p>This page is for development/debugging purposes only. On a live site, it should not be visible to end users.</p>

<h1>Sites</h1>
<ul>
<#list siteKeys as site>
  <li><a href="${site}/">${site}</a></li>
</#list>
</ul>

<h1>Builds</h1>
<#macro buildInfoDisplay component>
<ul>
  <li>Version: <@buildInfo component=component field='version' /></li>
  <li>Date: <@buildInfo component=component field='date' /></li>
  <li>User: <code><@buildInfo component=component field='user' /></code></li>
  <li>Commit: <code><@buildInfo component=component field='commitIdAbbrev' /></code></li>
</ul>
</#macro>

<h2>Wombat</h2>
<@buildInfoDisplay 'webapp' />

<h3>Enabled Dev Features</h3>
<@buildInfo component='webapp' field='enabledDevFeatures' ; enabledDevFeatures>
  <#if enabledDevFeatures?has_content>
  <ul>
    <#list enabledDevFeatures as featureFlag>
      <li><code>${featureFlag}</code></li>
    </#list>
  </ul>
  <#else>
  <strong>None</strong>
  </#if>
</@buildInfo>

<h2>Rhino</h2>
<@buildInfoDisplay 'service' />

<h1>Mappings</h1>
<table>
  <tr>
    <th>Pattern</th>
  <#list siteKeys as site>
    <th>${site}</th>
  </#list>
  </tr>

<#list mappingTable as rowObj>
  <tr>
    <td><code>${rowObj.pattern}</code></td>
    <#list rowObj.row as handlerName>
      <td>
        <#if handlerName?has_content>
          <code>${handlerName}</code>
        </#if>
      </td>
    </#list>
  </tr>
</#list>
</table>

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
