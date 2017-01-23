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

<#--
    Prints a text block containing a citation for an article.

    Parameters:
      citation - The article to be cited. Must be an object containing a subset of the normal article
                 metadata fields.
      useUrlDoiStyle - Boolean. True to format the DOI as a URL at http://dx.doi.org/; false to
                       just prefix it with "doi:".
  -->
<#macro displayCitation citation useUrlDoiStyle authors=citation.authors>
  <#assign maxAuthors = 5 /><#-- May want this to be configurable in the future. -->

  <#list authors as author>
    <#if author_index lt maxAuthors>
    ${author.surnames!}<#t/>
      <#if author.givenNames?has_content>
        <#if author.surnames?has_content>
        &nbsp;<#t/>
          <@abbreviatedName>${author.givenNames}</@abbreviatedName><#t/>
        <#else>
        ${author.givenNames}<#t/>
        </#if>
      </#if>
      <#if author.suffix?has_content>
      &nbsp;<#t/>
      ${author.suffix?replace('.', '')}<#t/>
      </#if>
    <#-- Show a comma if another author or "et al." will follow -->
      <#if author_has_next || authors?size gt maxAuthors><#t/>,</#if>
    </#if>
  </#list>

  <#if authors?size gt maxAuthors>
  et al.
  </#if>

  <#if citation.publicationDate??>(<@formatJsonDate date="${citation.publicationDate}" format="yyyy" />)</#if>

  <#if citation.title??>
    <@xform xml=citation.title/><#if !citation.title?ends_with('?')>.</#if>
  </#if>

  <#if citation.journal??>
  ${citation.journal.title}<#t/>
    <#if citation.volume??> <#--space--> ${citation.volume}</#if><#if citation.issue??>(${citation.issue})</#if><#t/>
    <#if citation.eLocationId??><#t/>: ${citation.eLocationId}</#if>.
  </#if>

  <#if citation.doi??>
    <#if useUrlDoiStyle>
    http://dx.doi.org/${citation.doi}
    <#else>
    doi: ${citation.doi}
    </#if>
  </#if>

</#macro>
