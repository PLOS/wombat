<html>
<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<#include "analyticsArticleJS.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">

  <h1>E-mail this Article</h1>
  <div class="source">
    <span>Article Source:</span>
    <a href="<@siteLink handlerName="article" queryParameters={'id': article.doi} />" title="Back to original article">
      ${article.title}
    </a>
  </div>

  <p>Fields marked with an <span>*</span> are required. </p>
  <form name="emailThisArticle" id="emailThisArticleForm" action="<@siteLink path='article/email'/>" method="post">
    <input type="hidden" name="id" value="${article.doi}"/>
    <input type="hidden" name="articleUri" value="<@siteLink handlerName="article" queryParameters={'id': article.doi} />"/>
    <fieldset>
      <legend>Complete this form</legend>
      <label id="emailToLabel" for="emailToAddresses">
        Recipients' E-mail addresses (one per line, max ${maxEmails})
        <span>*</span>
      </label>
      <#if emailToAddressesMissing??><span class="error">This field is required.</span></#if>
      <#if emailToAddressesInvalid??><span class="error">Invalid email address</span></#if>
      <#if tooManyEmailToAddresses??><span class="error">Too many email addresses.</span></#if>
      <br/>
      <textarea id="emailToAddresses" rows="${maxEmails}" name="emailToAddresses"
                cols="40"><#if emailToAddresses??>${emailToAddresses}</#if></textarea>
      <br/>
      <label id="emailAddressLabel" for="emailAddress">
        Your E-mail address <span>*</span>
      </label>
      <#if emailFromMissing??><span class="error">This field is required.</span></#if>
      <#if emailFromInvalid??><span class="error">Invalid email address.</span></#if>
      <br/>
      <input id="emailAddress" type="text" name="emailFrom" size="40" <#if emailFrom??>value="${emailFrom}"</#if>/>
      <br/>
      <label id="senderNameLabel" for="senderName">
        Your name <span>*</span>
      </label>
      <#if senderNameMissing??><span class="error">This field is required.</span></#if>
      <br/>
      <input type="text" id="senderName" name="senderName" size="40" <#if senderName??>value="${senderName}"</#if>/>
      <br/>
      <label id="noteLabel" for="note">
        Your comments to add to the E-mail
      </label>
      <br/>
      <textarea id="note" name="note" rows="5"
                cols="40"><#if note??>${note}<#else>I thought you would find this article interesting.</#if></textarea>
      <br/>
      <label>Text verification:</label>
      <#if captchaError??><span class="error">Verification is incorrect. Please try again.</span></#if>
      ${captchaHTML}
      <input id="sendButton" type="submit" value="Send">
    </fieldset>
  </form>
  <br/>
  <br/>
  <a href="${legacyUrlPrefix}static/privacy">Privacy Policy</a>

</div>

<#include "../common/footer/footer.ftl" />

<@renderJs />

</body>
</html>
