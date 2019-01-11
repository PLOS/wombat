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
    <div class="image">
      <img src="<@siteLink path="resource/img/404error.png"/>" alt="no image to display"/>
      <p class="caption">
      <a href="https://creativecommons.org/licenses/by/2.0/deed.en">CC</a>
      Image courtesy of
      <a href="https://www.flickr.com/photos/heypaul/107326169/">Hey Paul on Flickr</a>
      </p>
    </div>

    <div class="content">
      <p><strong>Looking for an article?</strong> Use the article search box above, or try the <a href="<@siteLink handlerName="advancedSearch" />">advanced search form</a>.</p>

      <p><strong>Need information about the journal or about submitting a manuscript?</strong> Use the Publish and About menus above, <a
          href="//plos.org/search">or
        search the PLOS websites</a>. You can also <@siteLink handlerName="siteContent" pathVariables={"pageName": "contact"} ; href><a href="${href}">contact the journal office</a> </@siteLink>.</p>

      <p><strong>Experiencing an issue with the website?</strong> Please use the <a href="<@siteLink handlerName="feedback" />">feedback form</a> and provide a detailed description of the
        problem.</p>
    </div>
 </article>
<#include "../common/footer/footer.ftl" />
<@js target="resource/js/global.js" />

</body>
</html>
