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
