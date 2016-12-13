<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      class="no-js">

  <body>
    <h1>Publication Years</h1>
    <#if yearRange??>
      <#assign min = yearRange["min"] />
      <#assign max = yearRange["max"] />
      <ul>
        <#list min[0..3]?number..max[0..3]?number as year>
          <li><a href="<@siteLink path="lockss-manifest/vol_${year?c}"/>">${year?c}</a></li>
        </#list>
      </ul>
    </#if>
    <#include "permission.ftl" />
  </body>
</html>