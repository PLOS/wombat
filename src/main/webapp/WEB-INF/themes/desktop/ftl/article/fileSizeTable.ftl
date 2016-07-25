<#-- Print a table of file sizes for all figures, as a JavaScript/JSON constant. -->
{
<#list articleItems?keys as itemDoi>
"${itemDoi?js_string}": {
  <#list articleItems[itemDoi].files?keys as fileType>
  "${fileType?js_string}": ${0?c}<#-- TODO: Populate with actual fize size --><#if fileType_has_next>,</#if>
  </#list>
}<#if itemDoi_has_next>,</#if>
</#list>
}
