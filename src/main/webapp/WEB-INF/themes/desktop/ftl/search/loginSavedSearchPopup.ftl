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

<#include "../common/userMgmtUrl.ftl" />
<h2>Save this search</h2>

  <div class="check-box">
    <strong>Log in to save a search alert</strong>
  </div>
  <div class="button-wrapper">
  <#assign currentPageLink = getLinkToCurrentPage()?url("UTF-8") />
    <input type="button" class="primary" id="btn-login-alert" onclick="window
        .location='user/secure/login?page=${currentPageLink}'" value="Log In"/>
    <input type="button" class="primary" id="btn-create-alert" onclick="window.location='${userMgmtUrl('registration/new')}'" value="Create Account"/>
    <input type="button" class="btn-cancel-savedsearch" value="Cancel"/>
    <span class="errortext" id="save-journal-alert-error"></span>
  </div>