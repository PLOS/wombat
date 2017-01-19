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

<#include '../../baseTemplates/articleSection.ftl' />
<#assign title = comment.title />
<#assign bodyId = 'page-comments-individual' />

<#macro commentBody comment>
<div class="context">
  <a class="expand">${comment.title}</a>

  <p class="details">
    <#include "userInfoLink.ftl" />
    Posted by <@userInfoLink comment.creator />
    on <@formatJsonDate date="${comment.created}" format="dd MMM yyyy 'at' hh:mm a" /></p>
</div>

<div class="response">
  <p>${comment.formatting.bodyWithHighlightedText}</p>

  <div class="response-menu">
  <#-- TODO: uncomment when we allow logged-in functionality
      <a class="flag-link">Flag for Removal</a>
      <a class="respond-link">Respond To This Post</a>
      -->
  </div>

</div>

  <#list comment.replies as reply>
  <section id="comments-related" class="thread-container">
    <div class="thread-level one">
      <div class="comment">
        <@commentBody comment=reply />
      </div>
    </div>
  </section>
  </#list>
</#macro>
<@page_header />
<#if userApiError??>
  <#include "userApiErrorMessage.ftl" />
<#else>
<div id="comment-content" class="content">
  <section id="comments-individual" class="comments">
    <section class="comment primary coloration-border-top">
      <@commentBody comment=comment />
    </section>
  </section>
</div>
</div>
</#if>
<section id="comment-info-window" class="modal-info-window top" data-method="full">

  <div class="modal-header clearfix">
    <a class="close coloration-text-color">v</a>
  </div>

  <div class="modal-content"></div>

</section><#--end model info window-->
<@page_footer />
