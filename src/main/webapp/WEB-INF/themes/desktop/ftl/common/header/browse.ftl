<@js src="resource/js/taxonomy-browser.js" />
<#include "../legacyLink.ftl" />
<div id="taxonomy-browser" class="areas" data-url-prefix="${legacyUrlPrefix}">
  <div class="wrapper">
    <div class="taxonomy-header">
      Browse Subject Areas
      <div id="subjInfo">?</div>
      <div id="subjInfoText">
        <p>Click through the PLOS taxonomy to find articles in your field.</p>
        <p>For more information about PLOS Subject Areas, click
          <a href="<@siteLink path='s/help-using-this-site#loc-subject-areas'/>">here</a>.
        </p>
      </div>
    </div>
    <div class="levels">
      <div class="levels-container cf">
        <div class="levels-position"></div>
      </div>
      <a href="#" class="prev"></a>
      <a href="#" class="next active"></a>
    </div>
  </div>
  <div class="taxonomy-browser-border-bottom"></div>
</div>