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

<script src="resource/js/vendor/jquery-1.11.0.js"></script>
<script src="resource/js/vendor/jquery.carousel.js"></script>
<script src="resource/js/components/carousel.js"></script>
<script src="resource/js/vendor/jquery.dotdotdot.js"></script>

<!--TODO: the following needs to be on all pages, not just home-->
<script src="resource/js/vendor/foundation-altered.js"></script>

<script>

	$(document).foundation({
    //The above is needed for the Foundation Top-bar
    //Below is needed for Foundation Tooltips on the home page in 'recently published articles'
    tooltip: {
      'wrap' : 'word',
      selector : '.truncated-tooltip',
      tip_template : function (selector, content) {
        return '<span data-selector="' + selector + '" class="'
          + Foundation.libs.tooltip.settings.tooltip_class.substring(1)
          + '">' + content + '</span>';
      }
    }
  });
  $(document).ready(function() {
    $(".truncated-tooltip").dotdotdot({
      height: 45
    });
  });

</script>
</body>
</html>
