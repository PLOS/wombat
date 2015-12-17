<#macro truncateTitle title titleLength = 120>
<#if title?length gte titleLength>
  <#assign abrsfx = "..." />
  <#assign truncatedTitle = title[0..(titleLength - abrsfx?length)] />
  <#--
    After freemarker upgrade to 2.3.22. This is better:
    <#assign title = title?keep_before_last(" ") />
  -->
  <#assign truncateAt = truncatedTitle?last_index_of(" ") - 1/>
  <#assign truncatedTitle = truncatedTitle[0..truncateAt] + abrsfx />

  <#if truncatedTitle?length lte 0>
    <#assign truncatedTitle = title[0..(titleLength - abrsfx?length - 1)] + abrsfx/>
  </#if>
<#else>
  <#assign truncatedTitle = title/>
</#if>
${truncatedTitle}
</#macro>
