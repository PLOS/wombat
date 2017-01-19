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

<html>
<#assign title = article.title, articleDoi = article.doi />
<#assign cssFile="related-content.css"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<div class="set-grid">
<#include "articleHeader.ftl" />
  <section class="article-body">

  <#include "tabs.ftl" />

  <@displayTabList "Related" />
  <#include "relatedContentBody.ftl" />

  </section>
  <aside class="article-aside">
  <#include "aside/sidebar.ftl" />
  </aside>
</div>



<#include "../common/footer/footer.ftl" />
<#include "articleJs.ftl" />
<@js src="resource/js/vendor/foundation-datepicker.min.js"/>
<@js src="resource/js/pages/related_content.js"/>


<@renderJs />

<script type="text/javascript" src="http://www.google.com/recaptcha/api/js/recaptcha_ajax.js"></script>

</body>
</html>
