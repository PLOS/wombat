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

<#-- JavaScript code necessary to submit comments from a page. Acts on the form from newCommentForm.ftl. -->

<@js src="resource/js/pages/comments.js" />

<script type="text/javascript">
  var comments = null;
  (function ($) {
    window.onload = function () {
      comments = new $.fn.comments();
    <#if indentationWidth??>
      comments.indentationWidth = ${indentationWidth?c};
    </#if>
      comments.addresses = {
      <@siteLink handlerName="postCommentFlag" ; url>
        submitFlagURL: "${url?js_string}",
      </@siteLink>
      <@siteLink handlerName="postComment" ; url>
        submitReplyURL: "${url?js_string}",
        submitDiscussionURL: "${url?js_string}",
      </@siteLink>
      <@siteLink handlerName="articleCommentTree" ; url> <#-- Omit 'id' parameter; JS will fill it in -->
        listThreadURL: "${url?js_string}"
      </@siteLink>
      };

    <#-- Applies only on the root new-comment page.
         Others invoke wireCompetingInterestRadioButtons dynamically and won't have .startDiscussion
      -->
      comments.wireCompetingInterestRadioButtons($('.startDiscussion'));
    };
  }(jQuery));
</script>
