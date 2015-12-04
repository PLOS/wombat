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

<#macro formValidation errorKey>
  <#if errorKey?eval??>
    <span class="error">
      <#nested/>
    </span>
  </#if>
</#macro>

  <form name="feedbackForm" method="post" title="Feedback"
        action="<@siteLink handlerName="feedbackPost" />">
    <fieldset>
    <#include "preamble.ftl" />
      <input type="text" name="userId" style="visibility: hidden" value="<#--TODO: Add userId if present-->"/>
      <ol>
        <li>
        <@formLabel>Name:</@formLabel><input type="text" name="name"/>
        <@formValidation "nameError">This field is required.</@formValidation>
        </li>
        <li>
        <@formLabel>E-mail Address:</@formLabel><input type="text" name="fromEmailAddress"/>
        <@formValidation "emailAddressMissingError">This field is required.</@formValidation>
        <@formValidation "emailAddressInvalidError">Invalid e-mail address</@formValidation>
        </li>
        <li>
        <@formLabel>Subject:</@formLabel><input type="text" name="subject"/>
        <@formValidation "subjectError">This field is required.</@formValidation>
        </li>
        <li>
        <@formLabel>Message:</@formLabel><textarea name="note" cols="70" rows="5"></textarea>
        <@formValidation "noteError">This field is required.</@formValidation>
        </li>
        <li>
          <label><strong>Security Check:</strong></label>

          <p>This question is to determine if you are a human visitor in order to prevent automated spam
            submissions.</p>
        ${captchaHtml}
        <@formValidation "captchaError">Verification is incorrect. Please try again.</@formValidation>
        </li>
      </ol>
      <input type="submit" value="Submit Feedback" class="btn"/>
    </fieldset>
  </form>

</div>

<#include "../common/footer/footer.ftl" />
</body>
</html>
