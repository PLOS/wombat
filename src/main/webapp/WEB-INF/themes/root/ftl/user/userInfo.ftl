<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#assign cssFile="site-content.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<#if user??>
  <#assign defaultVal="<em>No answer</em>"/>

  <h1>${user.displayName!defaultVal}</h1>

  <ul>
      <li>
          <span class=heading">Title</span>
          <span class="text"><#if user.title?has_content>${user.title}<#else>${defaultVal}</#if></span>
      </li>
      <li>
          <span class=heading">Full Name</span>
          <span class="text">
            <#if !user.givenNames?has_content && !user.surname?has_content>
              ${defaultVal}
            <#else>
              ${user.givenNames!} ${user.surname!}
            </#if>

          </span>
      </li>
      <li>
          <span class=heading">Location</span>
          <span class="text">
            <#if !user.city?has_content && !user.country?has_content>
              ${defaultVal}
            <#else>
              ${user.city!}<#if user.city?has_content && user.country?has_content>,&nbsp;</#if>${user.country!}
            </#if>
          </span>
      </li>
      <li>
          <span class=heading">Organization Address</span>
          <span class="text"><#if user.postalAddress?has_content>${user.postalAddress}<#else>${defaultVal}</#if></span>
      </li>
      <li>
          <span class=heading">Organization Type</span>
          <span class="text"><#if user.organizationType?has_content>${user.organizationType}<#else>${defaultVal}</#if></span>
      </li>
      <li>
          <span class=heading">Organization Name</span>
          <span class="text"><#if user.organizationName?has_content>${user.organizationName}<#else>${defaultVal}</#if></span>
      </li>
      <li>
          <span class=heading">Your Role</span>
          <span class="text"><#if user.positionType?has_content>${user.positionType}<#else>${defaultVal}</#if></span>
      </li>
      <li>
          <span class=heading">Short Biography</span>
          <span class="text"><#if user.biography?has_content>${user.biography}<#else>${defaultVal}</#if></span>
      </li>
      <li>
          <span class=heading">Research Areas</span>
          <span class="text"><#if user.researchAreas?has_content>${user.researchAreas}<#else>${defaultVal}</#if></span>
      </li>
      <li>
          <span class=heading">Interests</span>
          <span class="text"><#if user.interests?has_content>${user.interests}<#else>${defaultVal}</#if></span>
      </li>
      <li>
          <span class=heading">Website URL</span>
          <span class="text"><#if user.homePage?has_content><a href="${user.homePage}">${user.homePage}</a>
          <#else>${defaultVal}</#if></span>
      </li>
      <li>
          <span class=heading">Blog URL</span>
          <span class="text"><#if user.weblog?has_content><a href="${user.weblog}">${user.weblog}</a>
          <#else>${defaultVal}</#if></span>
      </li>
  </ul>

</#if>



<#include "../common/footer/footer.ftl" />

<@renderJs />

</body>
</html>
