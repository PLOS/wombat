<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->


<h1>Feedback</h1>

<#--
  Simple form input field with label. The nested element is the label.

  The key is both the form parameter name, and the name for prior input (if we are displaying validation errors) in the
  FreeMarker model. It is assumed that the same key will be used for both values.
  -->
<#macro formInput key>
  <#assign formInputId = "feedbackCreate_${key}" />
  <label for="${formInputId}">
    <#nested/>
  </label>
  <input id="${formInputId}" type="text" name="${key}" value="${key?eval!''}"/>
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

<form class="form-default" id="feedbackCreate" name="feedbackForm" method="post" title="Feedback"
      action="<@siteLink handlerName="feedbackPost" />">
  <fieldset>
  <#include "preamble.ftl" />
    <input type="text" name="userId" style="visibility: hidden" value="<#--TODO: Add userId if present-->"/>
    <ol>
      <li class="form-title"><i>All fields are required</i></li>
      <li class="form-group">
      <@formInput "name">Name:</@formInput>
      <@formValidation "nameError">This field is required.</@formValidation>
      </li>
      <li class="form-group">
      <@formInput "fromEmailAddress">E-mail Address:</@formInput>
      <@formValidation "emailAddressMissingError">This field is required.</@formValidation>
      <@formValidation "emailAddressInvalidError">Invalid e-mail address</@formValidation>
      </li>
      <li class="form-group">
      <@formInput "subject">Subject:</@formInput>
      <@formValidation "subjectError">This field is required.</@formValidation>
      </li>
      <li class="form-group">
        <label for="feedbackCreate_note">Message:</label>
        <textarea id="feedbackCreate_note" name="note" cols="70" rows="5">${note!''}</textarea>
      <@formValidation "noteError">This field is required.</@formValidation>
      </li>
      <li class="form-consent">
        <label for="feedbackCreate_consent">
          <input type="checkbox" id="feedbackCreate_consent" name="consent" value="true">
          I have read and agree to the terms of the <a href="https://www.plos.org/privacy-policy" target="_blank" title="Page opens in new window">PLOS Privacy Policy</a> and hereby consent to send my personal information to PLOS.
        </label>
        <@formValidation "consentError">This field is required.</@formValidation>
      </li>
    </ol>
    <input type="text" name="authorAffiliation" placeholder="Affiliation (optional)..." id="authorAffiliation">
    <input type="text" name="authorPhone" placeholder="Phone ..." id="authorPhone">
    <input id="submit-feedback-btn" type="submit" value="Submit Feedback" class="rounded coloration-white-on-color full"/>
  </fieldset>
</form>