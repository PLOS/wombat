<div class="print-article" id="printArticle" data-js-tooltip-hover="trigger">

  Print
  <ul class="print-options" data-js-tooltip-hover="target">
    <li>
      <#--<a title="Print Article"
         onclick="window.print(); return false;">
        Print article
      </a>-->
        <a href="#" onclick="if(typeof(_gaq) != 'undefined'){ _gaq.push(['_trackEvent','Article', 'Print', 'Click']); } window.print(); return false;" title="Print Article">Print article</a>
    </li>
  <#include "printService.ftl" />
  </ul>
</div>