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

<div id="social-links">
<#include "../common/title/siteTitle.ftl" />

  <h3>Connect with Us</h3>
  <ul class="social-media">
    <li id="social-link-email">
    <@siteLink handlerName="siteContent" pathVariables={"pageName": "contact"} ; href>
      <a href="${href}" class="social" title="Contact Us">
        <span class="icon-email" icon-hidden="true"></span>
        <span class="icon-text">Contact Us</span>
      </a>
    </@siteLink>
    </li>
    <li id="social-link-RSS">
      <a href="<@siteLink handlerName="homepageFeed" pathVariables={'feedType': 'atom'}/>"
         class="social" title="RSS">
        <span class="icon-rss" icon-hidden="true"></span>
        <span class="icon-text">RSS</span>
      </a>
    </li>
    <li id="social-link-twitter">
    <#include "../common/journalStyle.ftl" />
      <a href="https://twitter.com/<#if journalStyle == 'plosntd'>plosntds<#else>${journalStyle}</#if>"  class="social" title="${siteTitle} on Twitter" target="_blank">
        <span class="icon-twitter" icon-hidden="true"></span>
        <span class="icon-text">${siteTitle} on Twitter</span>
      </a>
    </li>
    <li id="social-link-facebook">
      <a href="https://www.facebook.com/plos.org" class="social" title="PLOS on Facebook" target="_blank">
        <span class="icon-facebook" icon-hidden="true"></span>
        <span class="icon-text">PLOS on Facebook</span>
    </a>
    </li>
    <li id="social-link-blogs">
      <a href="https://blogs.plos.org/" class="social-blogs" title="PLOS Blogs">PLOS Blogs</a>
    </li>
  </ul>

</div>
