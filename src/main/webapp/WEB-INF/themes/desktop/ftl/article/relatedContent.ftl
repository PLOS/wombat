<html>
<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />
<#assign cssFile="related-content.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<#include "analyticsArticleJS.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">
<#include "articleHeader.ftl" />
  <section class="article-body">

  <#include "tabs.ftl" />
    <div class="article-container">
      <#--<#include "nav.ftl" />-->
    </div>



  <input type="hidden" name="reCaptcha-info" id="reCaptcha-info" value='${recaptchaPublicKey}'/>

  <div id="media_coverage">
    <h2>Media Coverage of this Article <#--a href="" class="ir" title="More information">info</a--></h2>
    <div id="media_coverage_addition">Found additional news media or blog coverage for the article? <a id="media-coverage-form-link">Please let us know.</a></div>

    <div id="media-coverage-data"></div>

    <div id="media-coverage-modal" style="display: none;">

      <div id="media-coverage-form" class="cf">
        <form onsubmit="return false;" data-doi="${article.doi}" class="form standard">
          <div id="mcform-error"></div>
          <ul>
            <li class="small cf">
              <div>
                <label for="mcform-name">Your name:</label>
                <input type="text" name="name" id="mcform-name" <#if user??>value="${user.displayName}"</#if>>
                <span class="form-error"></span>
              </div>
              <div class="last">
                <label for="mcform-email">Your email address:</label>
                <input type="text" name="email" id="mcform-email" <#if user??>value="${user.email}"</#if>>
                <span class="form-error"></span>
              </div>
            </li>
            <li class="cf">
              <label for="mcform-link">Media link URL (website)</label>
              <input type="text" name="link" id="mcform-link" placeholder="e.g. http://www...." maxlength="1000">
              <span class="form-error"></span>
            </li>
            <li><label for="mcform-comment">Comments:</label>
              <textarea rows="4" cols="50" name="comment" id="mcform-comment" maxlength="1000"></textarea>
            </li>
            <li><label>Security Check:</label>

              <p class="note">This question is to determine if you're a human visitor in order to prevent automated spam
                submissions. </p>

              <div id="mcform-captcha"></div>
              <span class="form-error"></span>
            </li>
            <li>

              <span class="button primary">submit</span>
              <span class="button cancel">cancel</span>

            </li>
          </ul>
        </form>
      </div>

      <div id="media-coverage-success">
        <h3>Thank you!</h3>

        <p>Your submission will be reviewed by our staff.</p>
      </div>

      <div id="media-coverage-failure">
        <p>There was a problem with your submission. Please try again later.</p>
      </div>
    </div>
  </div>

  <#include "recommendedArticles.ftl" />
  </section>
  <aside class="article-aside">
  <#include "aside/sidebar.ftl" />
  </aside>
</div>

<#include "../common/footer/footer.ftl" />
<#include "../common/articleJs.ftl" />

<@js src="resource/js/pages/related_content.js"/>

<@js src="resource/js/components/table_open.js"/>
<@js src="resource/js/components/figshare.js"/>

<@js src="resource/js/pages/article.js"/>
<@renderJs />

<script type="text/javascript" src="http://www.google.com/recaptcha/api/js/recaptcha_ajax.js"></script>

<#include "aside/crossmarkIframe.ftl" />

</body>
</html>
