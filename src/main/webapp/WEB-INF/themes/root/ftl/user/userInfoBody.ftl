<#if user??>
  <#assign defaultVal="<em>No answer</em>"/>

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

    <dt>Organization Address</dt>
    <dd><#if user.postalAddress?has_content>${user.postalAddress}<#else>${defaultVal}</#if></dd>

    <dt>Organization Type</dt>
    <dd><#if user.organizationType?has_content>${user.organizationType}<#else>${defaultVal}</#if></dd>

    <dt>Organization Name</dt>
    <dd><#if user.organizationName?has_content>${user.organizationName}<#else>${defaultVal}</#if></dd>

    <dt>Your Role</dt>
    <dd><#if user.positionType?has_content>${user.positionType}<#else>${defaultVal}</#if></dd>

    <dt>Short Biography</dt>
    <dd><#if user.biography?has_content>${user.biography}<#else>${defaultVal}</#if></dd>

    <dt>Research Areas</dt>
    <dd><#if user.researchAreas?has_content>${user.researchAreas}<#else>${defaultVal}</#if></dd>

    <dt>Interests</dt>
    <dd><#if user.interests?has_content>${user.interests}<#else>${defaultVal}</#if></dd>

    <dt>Website URL</dt>
    <dd><#if user.homePage?has_content><a href="${user.homePage}">${user.homePage}</a>
    <#else>${defaultVal}</#if></dd>

    <dt>Blog URL</dt>
    <dd><#if user.weblog?has_content><a href="${user.weblog}">${user.weblog}</a>
    <#else>${defaultVal}</#if></dd>
  </dl>
</article>
</#if>