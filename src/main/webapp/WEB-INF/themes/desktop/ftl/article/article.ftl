<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />
<#assign tabPage="Article"/>

<#assign adPage="Article"/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />
<#include "../common/article/articleType.ftl" />

<#include "analyticsArticleJS.ftl" />

<body class="article ${journalStyle}">



<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">
  <#include "articleHeader.ftl" />
  <section class="article-body">

  <#include "tabs.ftl" />
    <@displayTabList 'Article' />

    <div class="article-container">

    <#include "nav.ftl" />
    <#include "articleLightbox.ftl" />

      <div class="article-content">

      <#include "amendment.ftl" />

      <#-- Figure carousel is placed here, then inserted midway through article text by JavaScript -->
      <#include "figure_carousel.ftl" />

        <div class="article-text" id="artText">
        ${articleText}

          <div class="ref-tooltip">
             <div class="ref_tooltip-content">

             </div>
          </div>

        </div>
      </div>
    </div>

    </section>
    <aside class="article-aside">
    <#include "aside/sidebar.ftl" />
    </aside>
  </div>

  <#include "protein-viewer.ftl"/>

  <#include "../common/footer/footer.ftl" />

  <#include "articleJs.ftl" />
<#include "../common/configIntFigJS.ftl" />

  <@js src="resource/js/components/table_open.js"/>
  <@js src="resource/js/components/figshare.js"/>
  <#--TODO: move article_lightbox.js to baseJs.ftl when the new lightbox is implemented sitewide -->
  <@js src="resource/js/vendor/jquery.panzoom.min.js"/>
  <@js src="resource/js/vendor/jquery.mousewheel.js"/>


  <@js src="resource/js/components/lightbox.js"/>


  <@js src="resource/js/pages/article_body.js"/>

  <@renderJs />

<#include "mathjax.ftl">

  <script type="text/javascript">


    function setLigands(structure, viewer) {
      var ligands = structure.select({rnames: ['SAH', 'RVP']});
      viewer.ballsAndSticks('ligands', ligands);
      viewer.centerOn(structure);
    }

    (function ($) {

   var figLink = '<@siteLink handlerName="repoObject" pathVariables={"key": "${intfigData[0].resources[0]}" + ".fake"}></@siteLink>';
      console.log(figLink);
      /*filesizetable*/
      $('#artText').populateFileSizes(<#include "fileSizeTable.ftl"/>);

      //pv.pathPDBFolder(folder='/resource');
      // override the default options with something less restrictive.
      var options = {
        width: 600,
        height: 600,
        antialias: true,
        quality: 'medium'
      };
      var viewer = pv.Viewer($('#viewer').get(0), options);
      
      pv.io.fetchPdb(figLink, function (structure) {
        // display the protein as cartoon, coloring the secondary structure
        // elements in a rainbow gradient.
        viewer.cartoon('structure', structure, {color: color.ssSuccession()});
        setLigands(structure, viewer);

        var structure;

        function lines() {
          viewer.clear();
          viewer.lines('structure', structure);
          setLigands(structure, viewer);
        }

        function cartoon() {
          viewer.clear();
          viewer.cartoon('structure', structure, {color: color.ssSuccession()});
          setLigands(structure, viewer);
        }

        function lineTrace() {
          viewer.clear();
          viewer.lineTrace('structure', structure);
          setLigands(structure, viewer);
        }

        function sline() {
          viewer.clear();
          viewer.sline('structure', structure);
          setLigands(structure, viewer);
        }

        function tube() {
          viewer.clear();
          viewer.tube('structure', structure);
          setLigands(structure, viewer);
        }

        function trace() {
          viewer.clear();
          viewer.trace('structure', structure);
          setLigands(structure, viewer);
        }

        function preset() {
          viewer.clear();
          viewer.cartoon('protein', structure);
          setLigands(structure, viewer);
        }


        document.getElementById('cartoon').onclick = cartoon;
        document.getElementById('line-trace').onclick = lineTrace;
        document.getElementById('preset').onclick = preset;
        document.getElementById('lines').onclick = lines;
        document.getElementById('trace').onclick = trace;
        document.getElementById('sline').onclick = sline;
        document.getElementById('tube').onclick = tube;

      });
    })(jQuery);

  </script>


<#include "aside/crossmarkIframe.ftl" />
<#--
TODO: move reveal mode & fig-viewer divs to global location when the new lightbox is implemented sitewide
-->
<div class="reveal-modal-bg"></div>

</body>
</html>
