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

<#--
  Simple form input field.

  The key is both the form parameter name, and the name for prior input (if we are displaying validation errors) in the
  FreeMarker model. It is assumed that the same key will be used for both values.
  -->
<#macro formInput key>
  <input type="text" name="${key}" value="${key?eval!''}"/>
</#macro>

<#--
  Display the nested validation error message if the given key is present in the model.

  The errorKey must be a constant string, which refers to a FreeMarker variable name. I apologize for the sketchy use
  of eval. If the value is absent, the FreeMarker engine will throw an error when the macro is invoked, making it
  impossible to do something simpler like
  <#macro formValidation error>
    <#if error??> ...message... </#if>
  </#macro>
  -->
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
        <@formLabel>Name:</@formLabel>
        <@formInput "name" />
        <@formValidation "nameError">This field is required.</@formValidation>
        </li>
        <li>
        <@formLabel>E-mail Address:</@formLabel>
        <@formInput "fromEmailAddress" />
        <@formValidation "emailAddressMissingError">This field is required.</@formValidation>
        <@formValidation "emailAddressInvalidError">Invalid e-mail address</@formValidation>
        </li>
        <li>
        <@formLabel>Subject:</@formLabel>
        <@formInput "subject"/>
        <@formValidation "subjectError">This field is required.</@formValidation>
        </li>
        <li>
        <@formLabel>Message:</@formLabel>
          <textarea name="note" cols="70" rows="5">${note!''}</textarea>
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
