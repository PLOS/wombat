<html xmlns="http://www.w3.org/1999/html">
<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />
<#assign cssFile = 'site-content.css'/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />
<#include "../macro/doiResolverLink.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">

  <article class="ambra-form">
    <h1>E-mail this Article</h1>
    <p class="source">
      <span>Article Source:</span>
      <a href="<@siteLink handlerName="article" queryParameters={'id': article.doi} />" title="Back to original article">
        ${article.title}
      </a>
    </p>
    <p>Fields marked with an <span class="required">*</span> are required. </p>
    <form id="emailThisArticleForm" class="form-default" name="emailThisArticle" action="<@siteLink path='article/email'/>" method="post">
      <input type="hidden" name="id" value="${article.doi}"/>
      <input type="hidden" name="articleUri" value="${doiResolverLink(article.doi)}"/>
      <fieldset>
        <ol>
          <li>
            <label id="emailToLabel" class="form-label" for="emailToAddresses">
              Recipients' E-mail addresses (one per line, max ${maxEmails})
              <span class="required">*</span>
            </label>
            <#if emailToAddressesMissing??><p class="error">This field is required.</p></#if>
            <#if emailToAddressesInvalid??><p class="error">Invalid email address</p></#if>
            <#if tooManyEmailToAddresses??><p class="error">Too many email addresses.</p></#if>
            <textarea id="emailToAddresses"  class="form-label" rows="${maxEmails}" name="emailToAddresses" cols="40">
              <#t><#if emailToAddresses??>${emailToAddresses}</#if>
            </textarea><#t>
          </li>
          <li>
            <label class="form-label" id="emailAddressLabel" for="emailAddress">
              Your E-mail address <span class="required">*</span>
            </label>
            <#if emailFromMissing??><p class="error">This field is required.</p></#if>
            <#if emailFromInvalid??><p class="error">Invalid email address.</p></#if>

            <input id="emailAddress" type="text" name="emailFrom" size="40" <#if emailFrom??>value="${emailFrom}"</#if>/>
          </li>
          <li>
            <label class="form-label" id="senderNameLabel" for="senderName">
              Your name <span class="required">*</span>
            </label>
            <#if senderNameMissing??><p class="error">This field is required.</p></#if>
            <input type="text" id="senderName" name="senderName" size="40" <#if senderName??>value="${senderName}"</#if>/>
          </li>
          <li>
            <label class="form-label" id="noteLabel" for="note">
              Your comments to add to the E-mail
            </label>
            <textarea id="note" name="note" rows="5" cols="40">
              <#t><#if note??>${note}<#else>I thought you would find this article interesting.</#if>
            </textarea><#t>
          </li>
          <li>
            <label class="form-label">Text verification:</label>
            <#if captchaError??><p class="error">Verification is incorrect. Please try again.</p></#if>
            ${captchaHTML}
          </li>
          <li>
            <input class="btn" id="sendButton" type="submit" value="Send">
          </li>
        </ol>
      </fieldset>
    </form>
    <br/>
    <br/>
    <a href="${legacyUrlPrefix}static/privacy">Privacy Policy</a>
  </article>
</div>

<#include "../common/footer/footer.ftl" />

<@renderJs />

</body>
</html>
