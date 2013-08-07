<#--

  Create a relative path starting with a variable number of steps "up" into parent directories.
  The output path is enclosed in double quotation marks, for use as an href or similar attribute.
  Some junk whitespace may appear before and after the output.
  The 'steps' argument should be a nonnegative integer.
  The 'path' argument should be a file path *not* starting with a slash.

  Examples:

    <link rel="stylesheet" href=<@pathUp 2 "static/css/base.css" />>
      resolves to
    <link rel="stylesheet" href="../../static/css/base.css">

    <link rel="stylesheet" href=<@pathUp 0 "static/css/interface.css" />>
      resolves to
    <link rel="stylesheet" href="static/css/interface.css">

  -->
<#macro pathUp steps path>
  <#if steps == 0 > <#-- Without this check, 1..steps is [1, 0], not an empty list. -->
  "${path}"
  <#else>
  "<#list 1..steps as i>../</#list>${path}" <#-- Don't insert whitespace -->
  </#if>
</#macro>
