<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
			lang="en" xml:lang="en"
			itemscope itemtype="http://schema.org/Article"
			class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#include "../common/head.ftl" />
<body class="home">

<#include "../common/header/header.ftl" />

<#include "body.ftl" />


<div class="spotlight"></div>

<#include "../common/footer/footer.ftl" />

<script type="text/javascript" src="resource/js/vendor/jquery-1.11.0.js"></script>
<script type="text/javascript" src="resource/js/vendor/jquery.carousel.js"></script>
<script type="text/javascript" src="resource/js/components/carousel.js"></script>
<script type="text/javascript" src="resource/js/vendor/jquery.dotdotdot.js"></script>
<script type="text/javascript" src="resource/js/vendor/foundation-altered.js"></script>


<script>

	$(document).foundation({
   /* tooltip: {
      selector : '.truncated-tooltip',
      tip_template : function (selector, content) {
        return '<span data-selector="' + selector + '" class="'
          + Foundation.libs.tooltip.settings.tooltip_class.substring(1)
          + '">' + content + '</span>';
      }
    }*/
  });
  $(document).ready(function() {
   /* $(".truncated-tooltip").dotdotdot({
      wrap: 'word',
      fallbackToLetter: true,
      height: 35
    });*/
  });

</script>
</body>
</html>
