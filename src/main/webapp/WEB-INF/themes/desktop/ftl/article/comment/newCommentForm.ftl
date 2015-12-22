<#macro newCommentForm submit=''>
<div class="reply_content">
  <#include "newCommentPreamble.ftl" />
</div>

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

    <span class="btn flt-l primary" onclick="${submit}">
      post</span>
    <a href="<@siteLink handlerName="articleCommentTree"  queryParameters={"id": article.doi} />">
      <span class="btn flt-l btn_cancel">cancel</span>
    </a>
  </fieldset>
</form>
</#macro>
