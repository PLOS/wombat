<#-- The form for posting a comment. Wired by commentSubmissionJs.ftl. -->
<#macro newCommentForm isStandalone>
<div class="reply_content">
  <#include "newCommentPreamble.ftl" />
</div>

<div id="responseSubmitMsg" class="error" style="display:none"></div>

<form class="cf" onsubmit="return false;">
  <fieldset>

    <input type="text" name="comment_title" placeholder="Enter your comment title..." id="comment_title">
    <textarea name="comment" placeholder="Enter your comment..." id="comment"></textarea>

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
      <textarea name="competing_interests" class="competing_interests"
                id="competing_interests"
                disabled="disabled"
                placeholder="Enter your competing interests..."></textarea>
    </div>

    <span class="btn flt-l btn_submit primary"
      <#if isStandalone>
          onclick="comments.submitDiscussion('${article.doi?js_string}')"
      </#if>
        >
      post
    </span>
    <#if isStandalone>
    <a href="<@siteLink handlerName="articleComments" queryParameters={"id": article.doi} />">
    </#if>
    <span class="btn flt-l btn_cancel">cancel</span>
    <#if isStandalone></a></#if>
  </fieldset>
</form>
</#macro>
