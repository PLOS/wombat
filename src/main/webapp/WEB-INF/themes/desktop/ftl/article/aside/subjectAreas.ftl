
<#assign articleTypeHeading = (article.articleType??)?string(article.articleType.heading, '') />
<#if categoryTerms?? && articleTypeHeading?? && articleTypeHeading != "Correction" && articleTypeHeading != "Expression of Concern" && articleTypeHeading != "Retraction" >
<div class="subject-areas-container">
  <h3>Subject Areas <div id="subjInfo">?</div>
    <div id="subjInfoText"><p>For more information about PLOS Subject Areas, click <a href="${legacyUrlPrefix}static/help#subjectAreas">here</a>.</p>
      <span class="inline-intro">We want your feedback.</span> Do these Subject Areas make sense for this article? Click the target next to the incorrect Subject Area and let us know. Thanks for your help!


    </div>
  </h3>
  <ul id="subjectList">
    <#list categoryTerms as categoryTerm>

      <li>
        <a class="taxo-term" title="Search for articles about ${categoryTerm}" href='${legacyUrlPrefix}search/advanced?unformattedQuery=subject%3A%22${categoryTerm?url}%22'>${categoryTerm}</a>
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