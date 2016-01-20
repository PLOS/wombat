<#if user??>
  <#assign defaultVal="<em>No answer</em>"/>

  <#function userValue val>
  <#-- In user-submitted text, render raw line breaks at HTML line breaks. -->
    <#return val?replace('\n', '<br/>') />
  </#function>

<article id="user-container">
  <h1>${user.displayName!defaultVal}</h1>

  <dl class="tight-list">
    <dt>Title</dt>
    <dd><#if user.title?has_content>${user.title}<#else>${defaultVal}</#if></dd>

    <dt>Full Name</dt>
    <dd>
      <#if !user.givenNames?has_content && !user.surname?has_content>
              ${defaultVal}
            <#else>
      ${user.givenNames!} ${user.surname!}
      </#if>
    </dd>

    <dt>Location</dt>
    <dd>
      <#if !user.city?has_content && !user.country?has_content>
      ${defaultVal}
      <#else>
      ${user.city!}<#if user.city?has_content && user.country?has_content>
        ,&nbsp;</#if>${user.country!}
      </#if>
    </dd>

    <#if user.organizationVisibility>
      <dt>Organization Address</dt>
      <dd><#if user.postalAddress?has_content>${userValue(user.postalAddress)}<#else>${defaultVal}</#if></dd>

      <dt>Organization Type</dt>
      <dd><#if user.organizationType?has_content>${userValue(user.organizationType)}<#else>${defaultVal}</#if></dd>

      <dt>Organization Name</dt>
      <dd><#if user.organizationName?has_content>${userValue(user.organizationName)}<#else>${defaultVal}</#if></dd>

      <dt>Your Role</dt>
      <dd><#if user.positionType?has_content>${userValue(user.positionType)}<#else>${defaultVal}</#if></dd>
    </#if>

    <dt>Short Biography</dt>
    <dd><#if user.biography?has_content>${userValue(user.biography)}<#else>${defaultVal}</#if></dd>

    <dt>Research Areas</dt>
    <dd><#if user.researchAreas?has_content>${userValue(user.researchAreas)}<#else>${defaultVal}</#if></dd>

    <dt>Interests</dt>
    <dd><#if user.interests?has_content>${userValue(user.interests)}<#else>${defaultVal}</#if></dd>

    <dt>Website URL</dt>
    <dd><#if user.homePage?has_content><a href="${user.homePage}">${userValue(user.homePage)}</a>
    <#else>${defaultVal}</#if></dd>

    <dt>Blog URL</dt>
    <dd><#if user.weblog?has_content><a href="${user.weblog}">${userValue(user.weblog)}</a>
    <#else>${defaultVal}</#if></dd>
  </dl>
</article>
</#if>