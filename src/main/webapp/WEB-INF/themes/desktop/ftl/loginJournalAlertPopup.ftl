<#include "common/userMgmtUrl.ftl" />

<div class="top-header">
  <span class="heading">Save this journal alert</span>
</div>

<div class="body-wrapper">
  <div class="check-box"><strong>Log in to create a weekly email alert for:</strong>
  ${subjectName!""}
  </div>
  <div class="button-wrapper">
    <#assign loginUrl = "../user/secure/login?page=${getLinkToCurrentPage()?url('UTF-8')}" />
    <#assign registerUrl = "${userMgmtUrl('registration/new')}" />

    <input type="button" class="primary" id="btn-login-alert" onclick="window.location.href='${loginUrl}'" value="Log In"/>
    <input type="button" class="primary" id="btn-create-alert" onclick="window.location.href='${registerUrl}'" value="Create Account"/>
    <input type="button" class="btn-cancel-alert" value="Cancel"/>
  </div>
</div>