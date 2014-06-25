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

<div class="spotlight">
<#include "adSlotBottom.ftl" />
</div>

<#include "../common/footer/footer.ftl" />

<script src="resource/js/vendor/jquery-1.11.0.js"></script>
<script src="resource/js/vendor/jquery.carousel.js"></script>
<script src="resource/js/components/carousel.js"></script>
<script src="resource/js/vendor/jquery.dotdotdot.js"></script>

<!--TODO: the following need to be on all pages, not just home-->
 <!--
 TODO: foundation-altered.js is in use for development. Need to
  download a customized foundation.js that includes only what we're using
  (Remove javascript for topbar)
  -->
<script src="resource/js/vendor/foundation-altered.js"></script>
<script src="resource/js/vendor/hoverIntent.js"></script>

<script src="resource/js/components/navsearch.js"></script>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
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
    //HoverIntent.js is used for the main navigation delay on hover
    function showIt() {

        $(this).addClass("hover");
        $('.dropdown', this).css('visibility', 'visible');

    }
    function hideIt(){

      $(this).removeClass("hover");
      $('.dropdown', this).css('visibility', 'hidden');

    }
    $('li.has-dropdown').hoverIntent(showIt, hideIt);
  });

</script>


</body>
</html>
