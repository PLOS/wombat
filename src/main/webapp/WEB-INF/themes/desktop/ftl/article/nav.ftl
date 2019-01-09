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

<div id="nav-article">
  <ul class="nav-secondary">

    <li class="nav-comments" id="nav-comments">
      <a href="article/comments?id=${article.doi}">Reader Comments (${commentCount.root})</a>
    </li>

    <li class="nav-media" id="nav-media" data-doi="${article.doi}">
      <a href="<@siteLink handlerName="articleRelatedContent" queryParameters={"id": article.doi} />">
        Media Coverage <span id="media-coverage-count"></span>
      </a>
    </li>

  <#if figures?has_content>
    <li id="nav-figures"><a href="#" data-doi="${article.doi}">Figures</a></li>
  </#if>
  </ul>
</div>
<@js target="resource/js/components/scroll.js"/>
<@js target="resource/js/components/nav_builder.js"/>
<@js target="resource/js/components/floating_nav.js"/>
