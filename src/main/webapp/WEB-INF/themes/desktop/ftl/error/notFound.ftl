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

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = "Page Not Found" />
<#include "../common/head.ftl" />

<#include "../common/journalStyle.ftl" />
<body class="static ${journalStyle}">
<#include "../common/header/headerContainer.ftl" />

<article class="error-page">
  <h1>Page Not Found</h1>
  <div class="content">
    <p>Looking for an article? Use the article search box above, or try the <a href="<@siteLink handlerName="advancedSearch" />">advanced search form</a>.</p>

    <p>Experiencing an issue with the website? Please use the <a href="<@siteLink handlerName="feedback" />">feedback form</a> and provide a detailed description of the
      problem.</p>
  </div>
</article>
<#include "../common/footer/footer.ftl" />
<@js target="resource/js/global.js" />
<@renderJs />

</body>
</html>

