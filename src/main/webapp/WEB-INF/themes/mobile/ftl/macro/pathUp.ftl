<#--

  Return a relative path starting with a variable number of steps "up" into parent directories.
  The 'steps' argument should be a nonnegative integer.
  The 'path' argument should be a file path *not* starting with a slash.

  Examples:

    <link rel="stylesheet" href="${pathUp(2 "static/css/base.css")}">
      resolves to
    <link rel="stylesheet" href="../../static/css/base.css">

    <link rel="stylesheet" href="${pathUp(0 "static/css/interface.css")}">
      resolves to
    <link rel="stylesheet" href="static/css/interface.css">

    <@cssLink target=pathUp(1 "static/css/base.css") />
      is equivalent to
    <@cssLink target="../static/css/base.css" />

  -->
<#function pathUp steps path>
  <#if steps == 0 ><#-- Without this check, 1..steps is [1, 0], not an empty list. -->
    <#return path />
  <#else>
    <#assign retval = path />
    <#list 1..steps as i>
      <#assign retval = "../" + retval />
    </#list>
    <#return retval />
  </#if>
</#function>
