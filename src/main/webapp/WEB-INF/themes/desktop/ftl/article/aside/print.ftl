<div class="print-article" id="printArticle" data-js-tooltip-hover="trigger">

  Print
  <ul class="print-options" data-js-tooltip-hover="target">
    <li><#-- // TODO: check the google analytics. the following is copied from Ambra -->
        <a href="#" onclick="if(typeof(_gaq) != 'undefined'){ _gaq.push(['_trackEvent','Article', 'Print', 'Click']); } window.print(); return false;" class="preventDefault" title="Print Article">Print article</a>
    </li>
  <#include "printService.ftl" />
  </ul>
</div>