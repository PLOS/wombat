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

<#include '../baseTemplates/default.ftl' />
<#assign title = 'Page Not Found' />
<#assign mainClass = "error" />

<@page_header />

<h1>Page Not Found</h1>

<p><strong>Looking for an article?</strong> Use the article search box above, or try the <a href="<@siteLink handlerName="advancedSearch" />">advanced search form</a>.</p>

<p><strong>Need information about the journal or about submitting a manuscript?</strong> Use the Publish and About menus above, <a
    href="//plos.org/search">or
  search the PLOS websites</a>. You can also <@siteLink handlerName="siteContent" pathVariables={"pageName": "contact"} ; href><a href="${href}">contact the journal office</a> </@siteLink>.</p>

<p><strong>Experiencing an issue with the website?</strong> Please use the <a href="<@siteLink handlerName="feedback" />">feedback form</a> and provide a detailed description of the
  problem.</p>
<@page_footer />

