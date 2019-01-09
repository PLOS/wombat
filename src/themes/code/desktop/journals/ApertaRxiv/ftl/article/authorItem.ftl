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

<#macro authorItemMeta author author_index>

  <#if author.equalContrib>
  <p class="author-contribute">
    <span class="contribute"> </span> Contributed equally to this work with:
    <#list equalContributors as contributor>
    ${contributor}<#if contributor_has_next>,</#if>
    </#list>
  </p>
  </#if>
  <#if author.deceased><p class="author-deceased" id="authDeceased-${author_index?c}">&dagger; Deceased.</p></#if>
  <#if author.corresponding??><p class="author-corresponding" id="authCorresponding-${author_index?c}"> ${author.corresponding}</p></#if>
  <#if author.currentAddresses?? && author.currentAddresses?size gt 0>
  <p class="author-address" id="authCurrentAddress-${author_index?c}">
    <#list author.currentAddresses as address>
    ${address}<#if address_has_next>; </#if>
    </#list>
  </p>
  </#if>
  <#if author.orcid?? && author.orcid.authenticated>
  <p class="author-orcid" id="authOrcid-${author_index?c}">
    <!--<span class="type">ORCID</span>-->
    <a href="${author.orcid.value}">${author.orcid.value}</a>
  </p>
  </#if>
  <#if author.customFootnotes?? && author.customFootnotes?size gt 0>
    <#list author.customFootnotes as note>
    ${note}
    </#list>
  </#if>
  <#if author.roles?? && author.roles?size gt 0>
  <p class="author-roles" id="authRoles">
    <span class="type">Roles</span><#list author.roles as role >
      ${role.content}<#if role_has_next>,</#if>
    </#list>
  </p>
  </#if>
  <#if author.affiliations?? && author.affiliations?size gt 0>
  <p class="author-affiliation" id="authAffiliations-${author_index?c}"><span class="type"><#if author.affiliations?size gt 1>Affiliations<#else>Affiliation</#if></span>
    <#list author.affiliations as affil>
    ${affil}<#if affil_has_next>, </#if>
    </#list>
  </p>
  </#if>

</#macro>

<#macro authorItemFull author author_index author_has_next if_expander=false hidden=false>
<dt class="author-toggle<#if hidden> on-show-more hide</#if>" href="#authorBox${author_index}">
${author.fullName}<#if author.corresponding??><span class="email"></span></#if>
</dt>
<dd class="author-box hide" id="authorBox${author_index}">
  <@authorItemMeta author author_index/>
</dd>
</#macro>
