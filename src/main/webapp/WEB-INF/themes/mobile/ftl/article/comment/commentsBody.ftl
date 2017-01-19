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

<section class="comments">
<#list articleComments as comment>
  <div class="comment">

    <div class="context">
      <#assign reqPath = "comment" />
      <#if mode?? && mode = "corrections">
        <#assign reqPath = "correction" />
      </#if>
      <a href="${reqPath}?id=${comment.commentUri}" class="expand">${comment.title}</a>

      <p class="details">
        <#include "userInfoLink.ftl" />
        Posted by <@userInfoLink comment.creator />
        on <@formatJsonDate date="${comment.created}" format="dd MMM yyyy 'at' hh:mm a" />
      </p>
    </div>

    <#if comment.replyTreeSize &gt; 0>
      <div class="responses">
        <p class="response-header">
          <a>${comment.replyTreeSize}
            <#if comment.replyTreeSize == 1 >
              RESPONSE
            <#else>
              RESPONSES
            </#if>
          </a>
          | <@formatJsonDate date="${comment.mostRecentActivity}" format="dd MMM yyyy 'at' hh:mm a" />
        </p>
      </div>
    </#if>
  </div>
</#list>
</section>

<#include "../../common/bottomMenu/bottomMenu.ftl" />
