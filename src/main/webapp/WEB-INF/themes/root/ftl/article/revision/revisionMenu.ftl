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

<#include "revisionLabel.ftl"/>
<@js src="resource/js/components/revision_menu.js"/>


<!-- make ascending order in the FTL -->
<div id="revisionMenu">
  <form>
   <select name="revisionLink" id="revisionLink">
     <#list revisionMenu.revisions?reverse as revision>
       <@siteLink handlerName="article"
                 queryParameters={"id": article.doi, "rev": revision.revisionNumber?c}
          ; href>
        <option value="${href}" <#if revision.isDisplayed>selected</#if>>
          Version ${revision.revisionNumber} <@revisionLabel revision/>
        </option>
      </@siteLink>
  </#list>
    </select>
  </form>
</div>

