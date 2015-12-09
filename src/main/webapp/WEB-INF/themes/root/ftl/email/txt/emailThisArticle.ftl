${senderName} has sent you an open-access article from ${journalName}.

The sender added this:
${note}

Read the open-access, full-text article here:
${articleUri}

<#if title?? || description??>
===================================================
  <#if title?? && title?trim?length gt 0>

  ${title}

  </#if>
  <#if description?? && description?trim?length gt 0>
  Abstract:

  ${description}
  </#if>
</#if>
