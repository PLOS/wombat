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

<#macro formInput input>
  <#assign formInputId = "${input}"/>
  <label for="${formInputId}" class="form-label">
    <#nested/>
  </label>
  <input id="${formInputId}" name="${formInputId}" type="text" value="${input?eval!''}"/>
</#macro>

<#macro formValidation errorKey input>
  <#if errorKey?eval??>
    <label for="${input}" class="error">
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
          <label class="form-label" for="emailToAddresses">Recipients' E-mail addresses (one per
            line,
            max ${maxEmails})<span class="required">*</span>
          </label>
          <textarea id="emailToAddresses"  class="form-label" rows="${maxEmails}" name="emailToAddresses"
                    cols="40">
          <#t>${emailToAddresses!''}</textarea>
          <@formValidation "emailToAddressesMissing" "emailToAddresses">This field is required.</@formValidation>
          <@formValidation "emailToAddressesInvalid" "emailToAddresses">Invalid email address.</@formValidation>
          <@formValidation "tooManyEmailToAddresses" "emailToAddresses">Too many email addresses.</@formValidation>
        </li>
        <li>
        <@formInput "emailFrom"> Your E-mail address <span class="required">*</span></@formInput>
        <@formValidation "emailFromMissing" "emailFrom">This field is required.</@formValidation>
        <@formValidation "emailFromInvalid" "emailFrom">Invalid email address.</@formValidation>
        </li>
        <li>
        <@formInput "senderName">Your name <span class="required">*</span></@formInput>
        <@formValidation "senderNameMissing" "senderName">This field is required.</@formValidation>
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
          <label class="form-label">Text verification</label>
        ${captchaHTML}
        <@formValidation "captchaError" "captcha">Verification is incorrect. Please try again
          .</@formValidation>
        </li>
      </ol>
      <input class="btn" id="sendButton" type="submit" value="Send" />
    </fieldset>
  </form>
<#include "emailFooter.ftl" />
    </article>
</div>