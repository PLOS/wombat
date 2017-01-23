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

<#-- The form for posting a comment. Wired by commentSubmissionJs.ftl. -->

<#macro newCommentForm isStandalone>
<div class="reply_content">
  <#include "newCommentPreamble.ftl" />
</div>

  <#include "errorMessages.ftl" />
  <@commentErrorMessageBlock>
  <#-- Messages that can be revealed by JavaScript. -->
    <@commentErrorMessage "missingTitle">A title is required.</@commentErrorMessage>
    <@commentErrorMessage "missingBody">You must say something in your comment.</@commentErrorMessage>
    <@commentErrorMessage "missingCi">You must say something in your competing interest statement.</@commentErrorMessage>

    <@commentErrorMessage "titleLength">
    Your title is {length} characters long; it cannot be longer than {maxLength} characters.
    </@commentErrorMessage>
    <@commentErrorMessage "bodyLength">
    Your comment is {length} characters long; it cannot be longer than {maxLength} characters.
    </@commentErrorMessage>
    <@commentErrorMessage "ciLength">
    Your competing interest statement is {length} characters long; it cannot be longer than {maxLength}
    characters.
    </@commentErrorMessage>

  <#-- Censored words are provided in the error key, but we don't display them. -->
    <@commentErrorMessage "censoredTitle">
    Your comment triggered a profanity filter. Please reword your comment.
    </@commentErrorMessage>
    <@commentErrorMessage "censoredBody">
    Your comment triggered a profanity filter. Please reword your comment.
    </@commentErrorMessage>
    <@commentErrorMessage "censoredCi">
    Your comment triggered a profanity filter. Please reword your comment.
    </@commentErrorMessage>

    <@commentErrorMessage "captchaValidationFailure">
    Verification is incorrect. Please try again.
    </@commentErrorMessage>
  </@commentErrorMessageBlock>

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

    <div class="captchaContainer">
      <#if isStandalone>
    ${captchaHtml}
      </#if>
      <#--
        Else, count on JavaScript to insert it into the captchaContainer.
        We don't want captchaHtml appearing twice on the page, because that breaks it.
        -->
    </div>

    <span class="btn flt-l btn_submit primary"
      <#if isStandalone>
          onclick="comments.submitDiscussion('${article.doi?js_string}',this)"
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
