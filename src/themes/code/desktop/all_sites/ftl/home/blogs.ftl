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

<#include "blogFeed.ftl" />

<#macro blogFeed blogUrl blogTitle blogFeedURL blogPostCount="2" blogStyle="">
  <@themeConfig map="blogs" value="rootUrl" ; hostName>
    <div id="blogs" data-feed-url="${hostName}${blogFeedURL}" data-postcount="${blogPostCount}" class="${blogStyle}">
      <div class="block-header"><a href="${hostName}${blogUrl}" id="blogtitle">${blogTitle}</a></div>
      <div id="blogrss"><#-- feed hook --></div>
      <div class="more-link">
        <a id="blogLink" href="${hostName}">See All Blogs</a>
      </div>
    </div>
  </@themeConfig>
</#macro>



<#--<@js target="resource/js/components/blogcall.js" />-->
