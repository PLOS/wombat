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