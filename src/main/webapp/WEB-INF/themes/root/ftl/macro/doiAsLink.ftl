<#--
  Render a DOI as an anchor element that displays links to a URI at the global DOI resolver at dx.doi.org.

  Contrast to doiResolverLink.ftl, which builds a URL with the option to substitute a custom DOI resolver hostname.
  The doiAsLink macro is meant to render the DOI in a human-readable URI form that also functions as a web address.
  This is in accordance with CrossRef's DOI Display Guidelines.

  See:
    http://www.crossref.org/02publishers/doi_display_guidelines.html
    http://www.crossref.org/01company/pr/news080211.html

  It should not be necessary to override this macro to change its content: the global, publisher-neutral "dx.doi.org"
  is by definition the correct thing to display in every environment. May it never go offline.
  -->
<#macro doiAsLink doiName id='' class=''>
  <#assign doiHref>http://dx.doi.org/${doiName?replace('^(info:doi/|doi:)', '', 'r')}</#assign>
<a <#if id?has_content>id="${id}"</#if> <#if class?has_content>class="${class}"</#if> href="${doiHref}">${doiHref}</a>
</#macro>
