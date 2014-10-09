
${categoryTerms?size}
<#assign articleTypeHeading = (article.articleType??)?string(article.articleType.heading, '') />
<#if categoryTerms?? && articleTypeHeading?? && articleTypeHeading != "Correction" && articleTypeHeading != "Expression of Concern" && articleTypeHeading != "Retraction" >
<div class="subject-areas-container">
  <h3>Subject Areas <div id="subjInfo">?</div>
    <div id="subjInfoText">
      <span class="inline-intro">We want your feedback.</span> Do these subject areas make sense for this article? If not, click the flag next to the incorrect subject area and we will review it. Thanks for your help!
    </div>
  </h3>
  <ul id="subjectList">
    <#list categoryTerms as categoryTerm>

      <li>
        <a href='${legacyUrlPrefix}search/advanced?unformattedQuery=subject%3A%22${categoryTerm?url}%22'>${categoryTerm}</a>
          <span
            data-articleDoi="${article.doi}"
            data-categoryname="${categoryTerm}"
            class="taxo-flag">
          </span>
      </li>
    </#list>
  </ul>
</#if>