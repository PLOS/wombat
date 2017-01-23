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

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">


<#assign title = article.title />
<#assign articleDoi = article.doi />
<#assign cssFile="comments.css"/>

<#include "../../common/head.ftl" />
<#include "../../common/journalStyle.ftl" />

<body class="article ${journalStyle}">

<#include "../../common/header/headerContainer.ftl" />
<div class="set-grid">
<#include "../articleHeader.ftl" />
  <section class="article-body">

  <#include "../tabs.ftl" />
  <@displayTabList 'Comments' />

    <div id="thread" class="startDiscussion">
      <div class="loader">  </div>

    <#if areCommentsDisabled?? && areCommentsDisabled>
      <#include "commentsDisabledMessage.ftl" />
    <#else>
      <h2>Start a Discussion</h2>

      <div class="reply cf form-default" id="respond">
        <h4>Post Your Discussion Comment</h4>

      <#include "newCommentForm.ftl" />
      <@newCommentForm true />

      </div>
    </#if>

    </div>

  </section>
  <aside class="article-aside">
  <#include "../aside/sidebar.ftl" />
  </aside>
</div>

<#include "../../common/footer/footer.ftl" />


<#include "../articleJs.ftl" />
<#include "commentSubmissionJs.ftl" />
<@renderJs />


<script type="text/javascript" async src="//platform.twitter.com/widgets.js"></script>

</body>
</html>
