<#-- Print a table of file sizes for all figures, as a JavaScript/JSON constant. -->
{
<#list article.figures as figure>
"${figure.doi?js_string}": {
"original": ${figure.original.metadata.size?c},
  <#list figure.thumbnails?keys as thumbnail>
  "${thumbnail?js_string}": ${figure.thumbnails[thumbnail].metadata.size?c}<#if thumbnail_has_next>,</#if>
  </#list>
}<#if figure_has_next>,</#if>
</#list>
}
