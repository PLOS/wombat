<#macro commentErrorMessageBlock>
<div id="responseSubmitMsg" class="error" style="display:none">
  <#nested/>
</div>
</#macro>

<#macro commentErrorMessage key>
<p class="commentErrorMessage" data-error-key="${key}" style="display:none"
   data-error-message="<#nested/>"></p>
</#macro>
