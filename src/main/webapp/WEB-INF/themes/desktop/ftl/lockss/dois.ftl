<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      class="no-js">

  <body>
    <h1>${month} ${year}</h1>
    <#if dois??>
      <ol>
        <#list dois as doi>
          <li style="b"><a href="<@siteLink path="article/?id=" + doi/>"> ${doi} </a></li>
        </#list>
      </ol>
    </#if>
    <#include "permission.ftl" />
  </body>
</html>