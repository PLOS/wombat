<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">


<#assign title = article.title />
<#assign articleDoi = article.doi />

<#include "../../common/head.ftl" />
<#include "../../common/journalStyle.ftl" />
<#include "../../common/article/articleType.ftl" />

<#include "../analyticsArticleJS.ftl" />

<body class="article ${journalStyle}">

<#include "../../common/header/headerContainer.ftl" />
<div class="set-grid">
<#include "../articleHeader.ftl" />
  <section class="article-body">

  <#include "../tabs.ftl" />
  <@displayTabList 'comments' />

    <div id="thread">
      <h2>Start a Discussion</h2>

      <div class="reply cf" id="respond">
        <h4>Post Your Discussion Comment</h4>

        <div class="reply_content">
        <#include "newCommentPreamble.ftl" />
        </div>
        <!--end reply_content-->

        <div id="responseSubmitMsg" class="error" style="display:none"></div>

        <form class="cf" onsubmit="return false;">
          <fieldset>

            <input type="text" name="comment_title" placeholder="Enter your comment title..." id="comment_title">
                <textarea name="comment" class="expand106-99999" placeholder="Enter your comment..."
                          id="comment"></textarea>

            <div class="help">
              <p>Supported markup tags: ''<em>italic</em>'' '''<strong>bold</strong>''' '''''<strong><em>bold
                italic</em></strong>''''' ^^<sup>superscript</sup>^^ ~~<sub>subscript</sub>~~</p>
            </div>
          </fieldset>

          <fieldset>
            <div class="cf">
              <input type="radio" name="competing" id="no_competing" value="" checked/>
              <label for="no_competing">No, I don't have any competing interests to declare</label>
            </div>
            <div class="cf">
              <input type="radio" name="competing" value="1" id="yes_competing"/>
              <label for="yes_competing">Yes, I have competing interests to declare (enter below):</label>
            </div>
            <div class="competing_text">
                  <textarea name="competing_interests" class="competing_interests expand88-99999"
                            id="competing_interests"
                            disabled="disabled"
                            placeholder="Enter your competing interests..."></textarea>
            </div>

            <span class="btn flt-l primary"
            <#--onclick="comments.submitDiscussion('${article.doi}')" TODO?-->
                >
                  post</span>
            <a href="<@siteLink handlerName="articleCommentTree"  queryParameters={"id": article.doi} />">
              <span class="btn flt-l btn_cancel">cancel</span>
            </a>
          </fieldset>
        </form>

      </div>

    </div>

  </section>
  <aside class="article-aside">
  <#include "../aside/sidebar.ftl" />
  </aside>
</div>

<#include "../../common/footer/footer.ftl" />


<@js src="resource/js/components/show_onscroll.js"/>
<@js src="resource/js/components/table_open.js"/>
<@js src="resource/js/components/figshare.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>

<@js src="resource/js/util/alm_config.js"/>
<@js src="resource/js/util/alm_query.js"/>
<@js src="resource/js/vendor/moment.js"/>
<@js src="resource/js/vendor/jquery.jsonp-2.4.0.js"/>
<@js src="resource/js/vendor/hover-enhanced.js"/>
<@js src="resource/js/highcharts.js"/>

<@js src="resource/js/components/twitter_module.js"/>
<@js src="resource/js/components/signposts.js"/>
<@js src="resource/js/components/nav_builder.js"/>
<@js src="resource/js/components/floating_nav.js"/>

<@js src="resource/js/pages/article.js"/>
<@js src="resource/js/pages/article_sidebar.js"/>
<@renderJs />


<script type="text/javascript" async src="//platform.twitter.com/widgets.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js"></script>
<script type="text/javascript" src="http://crossmark.crossref.org/javascripts/v1.4/crossmark.min.js"></script>

<#include "../aside/crossmarkIframe.ftl" />

</body>
</html>
