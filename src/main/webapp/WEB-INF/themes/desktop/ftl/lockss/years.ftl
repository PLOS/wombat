<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      class="no-js">

  <body>
    <h1>Publication Years</h1>
    <#if yearRange??>
      <ul>
        <#list yearRange[0]..yearRange[1] as year>
          <li><a href="<@siteLink path="lockss-manifest/vol_${year?c}"/>">${year?c}</a></li>
        </#list>
      </ul>
    </#if>
    <#include "permission.ftl" />
  </body>
</html>