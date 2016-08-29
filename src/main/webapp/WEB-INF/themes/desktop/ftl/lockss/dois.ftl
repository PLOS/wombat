<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      class="no-js">

  <body>
    <h1>${month} ${year}</h1>
    <#if searchResult??>
      <#assign docs = searchResult["docs"] />
      <ol>
        <#list docs as doc>
          <#assign doi = doc["id"] />
          <li style="b">
            <a href="<@siteLink urlDecodeLink=true handlerName="article" queryParameters={"id": doi} />">
              ${doi}
            </a>
          </li>
        </#list>
      </ol>
    </#if>
    <#include "permission.ftl" />
  </body>
</html>