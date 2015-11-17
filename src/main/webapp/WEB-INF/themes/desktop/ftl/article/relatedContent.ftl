<html>
<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />

<@js src="resource/js/pages/related_content.js"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<#include "analyticsArticleJS.ftl" />

<style>
  /*
The CSS for dialog widget,
(from: "jquery-ui-1.9.2.custom.css", which is not included yet)
*/
  .ui-dialog {
    position: absolute;
    top: 0;
    left: 0;
    padding: .2em;
    width: 300px;
    overflow: hidden;
  }

  .ui-dialog .ui-dialog-titlebar {
    padding: .4em 1em;
    position: relative;
  }

  .ui-dialog .ui-dialog-title {
    float: left;
    margin: .1em 16px .1em 0;
  }

  .ui-dialog .ui-dialog-titlebar-close {
    position: absolute;
    right: .3em;
    top: 50%;
    width: 19px;
    margin: -10px 0 0 0;
    padding: 1px;
    height: 18px;
  }

  .ui-dialog .ui-dialog-titlebar-close span {
    display: block;
  }

  .ui-dialog .ui-dialog-titlebar-close:hover, .ui-dialog .ui-dialog-titlebar-close:focus {
    padding: 0;
  }


  .ui-dialog .ui-dialog-content {
    position: relative;
    border: 0;
    padding: .5em 1em;
    background: none;
    overflow: auto;
    zoom: 1;
  }

  .ui-dialog .ui-dialog-buttonpane {
    text-align: left;
    border-width:0 0 0 0;
    background-image: none;
    margin: .5em 0 0 0;
    padding: .3em 1em .5em .4em;

  }

  .ui-dialog .ui-dialog-buttonpane .ui-dialog-buttonset {
    float: right;
  }

  .ui-dialog .ui-dialog-buttonpane button {
    margin: .5em 5px .5em 0;
    cursor: pointer;
  }

  .ui-dialog .ui-resizable-se {
    width: 14px;
    height: 14px;
    right: 3px;
    bottom: 3px;
  }


  .ui-draggable .ui-dialog-titlebar {
    cursor: move;
  }


  /* Customized css for the FigShare dialog */

  /* jqueryUI overrides*/
  .ui-dialog.tooltip-like .ui-dialog-titlebar-close.ui-state-hover,
  .ui-dialog.default-modal .ui-dialog-titlebar-close.ui-state-hover{
    border: none /*{borderColorHover}*/;
    background: none;
    font-weight: normal /*{fwDefault}*/;
  }

  .ui-dialog.tooltip-like {
    border-radius: 0px;
    -webkit-border-radius: 0px;
  }

  .ui-dialog.figure-table .tile_mini {
    min-width: 100px;
  }

  .ui-dialog .ui-dialog-content {
    padding: 0;
  }

  .ui-dialog.tooltip-like .ui-widget-header {
    background: none;
    padding: 0;
    border: none;
  }

  .tooltip-like + .ui-widget-overlay {
    background: none;
  }

  #popup .tile_mini a {
    color: #3C63AF;
  }


  /* css for the close button on the dialog */

  .ui-dialog.tooltip-like .ui-dialog-titlebar-close{
    position: absolute;
    right: 2px;
    top: 12px;
    margin: -10px 0 0 0;
    padding: 0px;
    background-color: #F8AF2D;
    text-indent: -9999px;
    border-radius: 2px;
    display: block;
  }

  .ui-dialog.tooltip-like .ui-dialog-titlebar-close span{
    background-image: url('../images/btn.sprite.close.small.png');
    background-position: 2px 1px;
    width: 17px;
    height: 17px;
  }

  .ui-dialog.tooltip-like .ui-dialog-titlebar-close:hover{
    background-color: #3C63AF;
  }

  .ui-dialog.tooltip-like .ui-dialog-titlebar-close:hover span{
    background-position: 2px -13px;
  }

  .default-modal .ui-dialog-titlebar-close {
    display: none;
  }

</style>

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">
<#include "articleHeader.ftl" />
  <section class="article-body">

  <#include "tabs.ftl" />
    <div class="article-container">
      <#--<#include "nav.ftl" />-->
    </div>

  </section>
  <aside class="article-aside">
  <#include "aside/sidebar.ftl" />
  </aside>

  <div id="media_coverage">
    <h2>Media Coverage of this Article <#--a href="" class="ir" title="More information">info</a--></h2>
    <div id="media_coverage_addition">Found additional news media or blog coverage for the article? <a id="media-coverage-form-link">Please let us know.</a></div>

    <div id="media-coverage-data"></div>

    <div id="media-coverage-modal" style="display: none;">

      <div id="media-coverage-form" class="cf">
        <form onsubmit="return false;" class="form standard">
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

</div>

<#include "../common/footer/footer.ftl" />

<@js src="resource/js/components/table_open.js"/>
<@js src="resource/js/components/figshare.js"/>

<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/vendor/moment.js"/>

<@js src="resource/js/components/twitter_module.js"/>
<@js src="resource/js/components/signposts.js"/>
<@js src="resource/js/vendor/spin.js"/>


<@js src="resource/js/pages/article.js"/>
<@js src="resource/js/pages/article_sidebar.js"/>
<@renderJs />


<script type="text/javascript" async src="//platform.twitter.com/widgets.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>
<script type="text/javascript" src="http://crossmark.crossref.org/javascripts/v1.4/crossmark.min.js"></script>

<#include "aside/crossmarkIframe.ftl" />

</body>
</html>
