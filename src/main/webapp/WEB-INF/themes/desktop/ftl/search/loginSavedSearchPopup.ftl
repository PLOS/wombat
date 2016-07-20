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