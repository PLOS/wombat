<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      class="no-js">

  <body>
    <h1>vol_${year}</h1>
    <#if months??>
      <ul>
        <#list months as month>
          <#if month?has_content>
            <li><a href="<@siteLink path="lockss-manifest/vol_" + year + "/" + month />"> ${month} </a></li>
          </#if>
        </#list>
      </ul>
    </#if>
  <#include "permission.ftl" />
  </body>
</html>