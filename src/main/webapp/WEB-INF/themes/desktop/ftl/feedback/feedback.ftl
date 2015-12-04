<html>
<#assign title = "" /> <#-- use default -->
<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="${journalStyle}">
<#include "../common/header/headerContainer.ftl" />

<div>
  <h1>Feedback</h1>

<#macro formLabel>
<#-- Placeholder for styling on form labels. -->
  <#nested/>
</#macro>

  <form name="feedbackForm" method="post" title="Feedback"
        action="<@siteLink handlerName="feedbackPost" />">
    <fieldset>
    <#include "preamble.ftl" />
      <input type="text" name="userId" style="visibility: hidden" value="<#--TODO: Add userId if present-->"/>
      <ol>
        <li><@formLabel>Name:</@formLabel><input type="text" name="name"/></li>
        <li><@formLabel>E-mail Address:</@formLabel><input type="text" name="fromEmailAddress"/></li>
        <li><@formLabel>Subject:</@formLabel><input type="text" name="subject"/></li>
        <li><@formLabel>Message:</@formLabel><textarea name="note" cols="70" rows="5"></textarea></li>
        <li>
          <label><strong>Security Check:</strong></label>

          <p>This question is to determine if you are a human visitor in order to prevent automated spam
            submissions.</p>
        ${captchaHtml}
        </li>
      </ol>
      <input type="submit" value="Submit Feedback" class="btn"/>
    </fieldset>
  </form>

</div>

<#include "../common/footer/footer.ftl" />
</body>
</html>
