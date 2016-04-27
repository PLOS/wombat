<#macro formInput input>
  <#assign formInputId = "${input}"/>
  <label for="${formInputId}" class="form-label">
    <#nested/>
  </label>
  <input id="${formInputId}" name="${formInputId}" type="text" value="${input?eval!''}"/>
</#macro>

<#macro formValidation errorKey>
  <#if errorKey?eval??>
    <label class="error">
        <#nested/>
    </label>
  </#if>
</#macro>

<div id="set-grid">
  <article class="ambra-form">
  <h1>E-mail this Article</h1>
  <p>
    <span class="source">Article Source:</span>
    <a href="<@siteLink handlerName="article" queryParameters={'id': article.doi} />" title="Back to original article">${article.title}
    </a>
  </p>
  <p>Fields marked with an <span class="required">*</span> are required. </p>
  <form id="emailThisArticleForm" class="form-default" name="emailThisArticle" action="<@siteLink path='article/email'/>"
        method="post">
    <input type="hidden" name="id" value="${article.doi}"/>
    <input type="hidden" name="articleUri" value="${doiResolverLink(article.doi)}"/>
    <fieldset>
      <ol>
        <li>
          <label class="form-label" for="toAddresses">Recipients' E-mail addresses (one per
            line,
            max ${maxEmails})<span class="required">*</span>
          </label>
          <textarea id="emailToAddresses"  class="form-label" rows="${maxEmails}" name="emailToAddresses"
                    cols="40">
          <#t>${emailToAddresses!''}</textarea>
          <@formValidation "emailToAddressesMissing">This field is required.</@formValidation>
          <@formValidation "emailToAddressesInvalid">Invalid email address.</@formValidation>
          <@formValidation "tooManyEmailToAddresses">Too many email addresses.</@formValidation>
        </li>
        <li>
        <@formInput "emailFrom"> Your E-mail address <span class="required">*</span></@formInput>
        <@formValidation "emailFromMissing">This field is required.</@formValidation>
        <@formValidation "emailFromInvalid">Invalid email address.</@formValidation>
        </li>
        <li>
        <@formInput "senderName">Your name <span class="required">*</span></@formInput>
        <@formValidation "senderNameMissing">This field is required.</@formValidation>
        </li>
        <li>
          <label class="form-label" for="note">
            Your comments to add to the E-mail
          </label>
          <textarea id="note" name="note" rows="5" cols="40">
            <#t>${note!'I thought you would find this article interesting.'}
          </textarea><#t>
        </li>
        <li>
          <label class="form-label">Text verification:</label>
        ${captchaHTML}
        <@formValidation "captchaError" >Verification is incorrect. Please try again.</@formValidation>
        </li>
      </ol>
      <input class="btn" id="sendButton" type="submit" value="Send" />
    </fieldset>
  </form>
<#include "emailFooter.ftl" />
    </article>
</div>