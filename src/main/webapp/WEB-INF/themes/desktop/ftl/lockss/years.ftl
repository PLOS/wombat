<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      class="no-js">

<body>
<#list yearRange as year>
    <#if year?has_content>
       ${year}
    </#if>
</#list>
</body>
</html>