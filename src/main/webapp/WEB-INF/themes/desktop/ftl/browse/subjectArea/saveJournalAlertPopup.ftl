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

<div class="top-header">
  <span class="heading">Save this journal alert</span>
</div>

<#assign profileUrl = "${userMgmtUrl('account/edit-profile')}" />

<div class="body-wrapper">
  <div class="check-box"><strong id="text-journal-alert-prompt">
    <#if subscribed>You are currently subscribed to:
    <#else>Create a weekly email alert for:
    </#if>
    </strong>
  ${subjectName!""}
  </div>
  <div class="grey-text">
    You can change these options by clicking <a href="${profileUrl}">profile</a> then "Alerts and Notifications"
    <#if subscribed>Would you like to unsubscribe?</#if>
  </div>
  <div class="button-wrapper">
    <#if subscribed>
    <input type="button" class="primary" id="btn-save-journal-alert" value="Unsubscribe"/>
    <#else>
    <input type="button" class="primary" id="btn-save-journal-alert" value="Save"/>
    </#if>
    <input type="button" class="btn-cancel-alert" value="Cancel"/>
    <span class="errortext" id="save-journal-alert-error"></span>
  </div>
</div>