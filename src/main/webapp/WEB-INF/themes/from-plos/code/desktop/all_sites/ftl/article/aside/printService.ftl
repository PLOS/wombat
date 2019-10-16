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

<#include "ezReprintJournalId.ftl" />

<#assign titleWithNoHtmlTags = article.title?replace('<.+?>', '', 'r') />
<#macro authorList>
  <#list authors as author>
  ${author.fullName?url('UTF-8')}<#t/>
    <#if author_has_next>${', '?url('UTF-8')}</#if><#t/>
  </#list>
</#macro>

<#assign authors><@authorList/></#assign>
<#assign params = ["type=A", "page=0", "journal=${ezReprintJournalId}", "doi=${article.doi?url('UTF-8')}",
"volume=", "issue=", "title=${titleWithNoHtmlTags?url('UTF-8')}", "author_name=${authors}" ,
"start_page=1"]?join('&amp;') />

<li>
  <a title="Odyssey Press" href="https://www.odysseypress.com/onlinehost/reprint_order.php?${params}<#t/>
  <#if article.pageCount?? && article.pageCount gt 0>&amp;end_page=${article.pageCount?c}</#if><#t/>">
  <#t/>EzReprint
  </a>
</li>

