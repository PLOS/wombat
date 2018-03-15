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

<h1>Download Citation</h1>

<p>
  <span>Article Source:</span>
  <a href="<@siteLink handlerName="article" queryParameters={'id': article.doi} />"
     title="Back to original article">
    <strong><@xform xml=article.title/></strong>
  </a>
  <br/>
<#include "citation.ftl" />
<@displayCitation article authors/>
</p>

<h2>Download the article citation in the following formats:</h2>
<ul>
  <li>
    <a href="<@siteLink handlerName="downloadRisCitation" queryParameters={'id': article.doi} />"
       title="RIS Citation">
      RIS</a> (compatible with EndNote, Reference Manager, ProCite, RefWorks)
  </li>
  <li>
    <a href="<@siteLink handlerName="downloadBibtexCitation" queryParameters={'id': article.doi} />"
       title="BibTex Citation">
      BibTex</a> (compatible with BibDesk, LaTeX)
  </li>
</ul>
