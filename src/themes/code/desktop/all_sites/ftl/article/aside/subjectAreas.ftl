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



<#if categoryTerms?? && articleType.name?? && articleType.name != "Correction" && articleType.name != "Expression of Concern" && articleType.name != "Retraction" >
<div class="subject-areas-container">
  <h3>Subject Areas <div id="subjInfo">?</div>
    <div id="subjInfoText">
      <p>For more information about PLOS Subject Areas, click
        <a href="https://github.com/PLOS/plos-thesaurus/blob/develop/README.md" target="_blank" title="Link opens in new window">here</a>.</p>
      <span class="inline-intro">We want your feedback.</span> Do these Subject Areas make sense for this article? Click the target next to the incorrect Subject Area and let us know. Thanks for your help!


    </div>
  </h3>
  <ul id="subjectList">
    <#list categoryTerms as categoryTerm>
      <li>
        <@themeConfig map="journal" value="journalKey" ; journalKey>
          <@siteLink handlerName="simpleSearch"
          queryParameters= {
          "filterSubjects": categoryTerm,
          "filterJournals": journalKey,
          "q": ""}; href>
              <a class="taxo-term" title="Search for articles about ${categoryTerm}"
                 href="${href}">${categoryTerm}</a>
          </@siteLink>
        </@themeConfig>
        <span class="taxo-flag">&nbsp;</span>
        <div class="taxo-tooltip" data-categoryname="${categoryTerm}"><p class="taxo-explain">Is the Subject Area <strong>"${categoryTerm}"</strong> applicable to this article?
          <button id="noFlag" data-action="remove">Yes</button>
          <button id="flagIt" value="flagno" data-action="add">No</button></p>
          <p class="taxo-confirm">Thanks for your feedback.</p>
        </div>
      </li>
    </#list>
  </ul>
</div>
<div id="subjectErrors"></div>
</#if>