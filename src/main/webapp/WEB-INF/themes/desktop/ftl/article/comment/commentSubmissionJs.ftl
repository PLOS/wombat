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
      </@siteLink>
      <@siteLink handlerName="ajaxComment" ; url> <#-- Omit 'id' parameter; JS will fill it in -->
        getAnnotationURL: "${url?js_string}",
      </@siteLink>
      <@siteLink handlerName="articleCommentTree" ; url> <#-- Omit 'id' parameter; JS will fill it in -->
        listThreadURL: "${url?js_string}"
      </@siteLink>
      };
    };
  }(jQuery));
</script>
