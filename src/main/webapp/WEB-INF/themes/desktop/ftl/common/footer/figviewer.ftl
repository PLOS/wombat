<div id="fig-viewer-content">
  <div class="fv-header">
    <h1 id="fvTitle"> </h1>
    <ul id="fvAuthors"> </ul>

    <ul class="fv-nav tabs" data-tab role="tablist">
      <li class="abst tab-title"><a href="#panel-abst" controls="panel-abst">Abstract</a></li>
      <li class="figs tab-title active"><a href="#panel-figs" controls="panel-figs">Figures</a></li>
      <li class="refs tab-title"><a href="#panel-refs"controls="panel-refs">References</a></li>
    </ul>

    <div class="fv-close close-reveal-modal" title="close">&nbsp;</div>
  </div>

  <div class="tabs-content">
    <section role="tabpanel" aria-hidden="true" class="content" id="panel-abst">
    <#-- content is appended with js in figviewer.js -->
    </section>

    <section role="tabpanel" aria-hidden="false" class="content active" id="panel-figs">
      <div id="fig-viewer-figs" class="pane">
        <div id="fig-viewer-slides"></div>
      </div>
      <div id="fig-viewer-thmbs">
        <div id="fig-viewer-thmbs-content"></div>
      </div>

      <div id="fig-viewer-controls">
        <span class="fig-btn thmb-btn"><i class="icn"></i> All Figures</span>
        <span class="fig-btn next"><i class="icn"></i> Next</span>
        <span class="fig-btn prev"><i class="icn"></i> Previous</span>
        <div class="loading-bar" style="display: none;"></div>

        <div id="fv-zoom" style="display: block;">
          <div id="fv-zoom-min"></div>
          <div id="fv-zoom-sldr" class="ui-slider ui-slider-horizontal ui-widget ui-widget-content ui-corner-all">
            <a class="ui-slider-handle ui-state-default ui-corner-all" href="#" style="left: 0%;"></a>
          </div>
          <div id="fv-zoom-max"></div>
        </div>
      </div>

    </section>

    <section role="tabpanel" aria-hidden="true" class="content" id="panel-refs">
    <#-- content is appended with js in figviewer.js -->
    </section>
  </div>
</div>
