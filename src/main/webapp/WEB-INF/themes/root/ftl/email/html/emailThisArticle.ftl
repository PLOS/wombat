${senderName} has sent you an open-access article from ${journalName}.<br/>
<br/>
The sender added this:<br/>
<br/>
${note}<br/>
<br/>
Read the open-access, full-text article here:<br/>
<a href="${articleUri}">${articleUri}</a>
<#if title?? || description??>
<br/>
<br/>
===================================================
<br/>
  <#if title?? && title?trim?length gt 0>
  ${title} <br/>
  </#if>
  <#if description?? && description?trim?length gt 0>
  <br/>
  Abstract: ${description}
  </#if>
</#if>
