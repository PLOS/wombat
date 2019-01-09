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

<!-- reused with modifications from from article/comment/newCommentForm.ftl -->

<#include "errorMessages.ftl" />

<#macro newCommentForm>
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
  
  </@commentErrorMessageBlock>

<form class="cf" onsubmit="return false;">

  <fieldset>
    <span class="topic">Please follow our <a href="" target="_blank" title="Comments that do not confirm to our guidelines...">comment guidelines</a></span>

    <input type="text" name="comment_title" placeholder="Subject line here..." id="comment_title">
    <textarea name="comment" placeholder="Enter your comment here..." id="comment"></textarea>

    <div class="help">
      <p>Supported markup tags: italic = "X"; bold = ""X""; bold italic = """X""";
        superscript = ^^X^^; subscript = ~~X~~</p>
    </div>
    <input type="text" name="author_phone" placeholder="Phone ..." id="author_phone">
    <input type="text" name="author_email_address" placeholder="Email (optional)..." id="author_email_address">
    <input type="text" name="author_name" placeholder="Name (optional)..." id="author_name">
    <input type="text" name="author_affiliation" placeholder="Affiliation (optional)..." id="author_affiliation">
  </fieldset>

  <fieldset>
    <span class="topic">Please review our <a href="" target="_blank">competing interests policy</a></span>

    <div class="cf">
      <input type="radio" name="competing" id="no_competing" value="" checked/>
      <label for="no_competing">I have no competing interests.</label>
    </div>
    <div class="cf">
      <input type="radio" name="competing" value="1" id="yes_competing"/>
      <label for="yes_competing">I have competing interests to declare.</label>
    </div>
    <div class="competing_text">
      <textarea name="competing_interests" class="competing_interests"
                id="competing_interests"
                disabled="disabled"
                placeholder="Enter competing interests here..."></textarea>
    </div>
  </fieldset>

  <fieldset class="buttons">
    <button class="button btn_submit">
      Submit
    </button>

    <a class="link cancel btn_cancel">
      cancel
    </a>
  </fieldset>
</form>
</#macro>

<#macro reportCommentForm>
  <@commentErrorMessageBlock>
    <@commentErrorMessage "missingComment">You must say something in your flag comment</@commentErrorMessage>
    <@commentErrorMessage "commentLength">
      Your flag comment is {length} characters long, it can not be longer than {maxLength}.
    </@commentErrorMessage>
  </@commentErrorMessageBlock>

  <form class="cf" onsubmit="return false;">
    <fieldset class="">
      <span class="topic">
        <#include "flagPreamble.ftl" />
      </span>
      <div class="cf">
        <input type="radio" name="reason" value="spam" id="spam" checked/>
        <label for="spam">Spam</label>
      </div>
      <div class="cf">
        <input type="radio" name="reason" value="offensive" id="offensive"/>
        <label for="offensive">Offensive</label>
      </div>
      <div class="cf">
        <input type="radio" name="reason" value="inappropriate" id="inappropriate"/>
        <label for="inappropriate">Inappropriate</label>
      </div>
      <div class="cf">
        <input type="radio" name="reason" value="other" id="other"/>
        <label for="other">Other</label>
      </div>
      <div id="flag_text">
        <textarea placeholder="Add any additional information here..." name="additional_info"></textarea>
      </div>
    </fieldset>

    <fieldset class="buttons">
      <button class="button btn_submit">submit</button>
      <a class="link cancel btn_cancel">cancel</a>
    </fieldset>

  </form>
</#macro>


<!-- DOM elements that are copied or moved to the comment sections, e.g., new comment form,
reply to comment or report/flag a comment. -->

<div class="comment-stash hide">
  <div id="respond_prototype" class="reply subresponse cf" style="display: none;">
  <@newCommentForm />
  </div>
  
  <div id="report_prototype" class="reply review cf" style="display: none;">
    <div class="flagForm">
    <@reportCommentForm />
    </div>
    <div class="flagConfirm" style="display: none;">
      <form class="cf" onsubmit="return false;">
        <h4>Thank You!</h4>

        <p>Thank you for taking the time to flag this posting; we review flagged postings on a regular basis.</p>
        <button class="button close_confirm">close</button>
      </form>
    </div>
  </div>
</div>


