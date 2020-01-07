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

<#include "../common/maxAuthorsToShow.ftl" />
<#macro authorList authors=[]>
<p title="<#list authors as author>${author.fullName}<#if author_has_next>, </#if></#list>">
  <#if authors?size gt maxAuthorsToShow + 1>
    <#--
    Put all authors in the range from maxAuthorsToShow-1 to size-1 in the expander.
    I.e., before clicking the expander, the user sees the first maxAuthorsToShow-1 authors and the last author.
    If the expander would contain only one author, just show the author instead.
    -->
    <#list authors as author><#-- Before the expander -->
      <#if author_index lt (maxAuthorsToShow - 1) >
        <span class="author">${author.fullName}<#if author_has_next>,</#if></span>
      </#if>
    </#list>

    <span data-js-toggle="toggle_add">&nbsp;[ ... ],</span>
    <#assign lastAuthor = authors[authors?size - 1] />
    <span class="author last-author">${lastAuthor.fullName}</span>
  <#else>
  <#-- List authors with no expander -->
    <#list authors as author>
      <span class="author">${author.fullName}<#if author_has_next>,</#if></span>
    </#list>
  </#if>
</p>
</#macro>