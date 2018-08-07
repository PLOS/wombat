<#--
  ~ Copyright (c) 2017 Public Library of Science
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the "Software"),
  ~ to deal in the Software without restriction, including without limitation
  ~ the rights to use, copy, modify, merge, publish, distribute, sublicense,
  ~ and/or sell copies of the Software, and to permit persons to whom the
  ~ Software is furnished to do so, subject to the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in
  ~ all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  ~ THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  ~ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  ~ DEALINGS IN THE SOFTWARE.
  -->

<#include "configJs.ftl" />
<#--//This file gets called in the head.ftl because it should sit above other js files- the actual Js gets printed in the bottom of the body. -->
<script src="https://code.jquery.com/jquery-2.1.4.min.js" ></script>
<script>window.jQuery || document.write('<script src="<@siteLink path="resource/js/vendor/jquery-2.1.4.min.js" />""><\/script>')</script>

<#if article??>
    <@themeConfig map="article" value="showFigShare" ; showFigShare>
        <#if (showFigShare?? && showFigShare)>
          <script type="text/javascript" src="https://widgets.figshare.com/static/figshare.js"></script>
        </#if>
    </@themeConfig>
</#if>

<#--//
fast click is required for mobile interactions for foundation.
-->
<@js src="resource/js/vendor/fastclick/lib/fastclick.js"/>
<@js src="resource/js/vendor/foundation/foundation.js"/>
<@js src="resource/js/vendor/underscore-min.js"/>
<@js src="resource/js/vendor/underscore.string.min.js"/>
<@js src="resource/js/vendor/moment.js" />


<#--//
The jQuery UI effects core is needed for some components
-->
<@js src="resource/js/vendor/jquery-ui-effects.min.js" />

<#--//include foundation js widgets here *
* unless you are absolutely sure they will only be in one scenario - then you
should include it in the ftl file that needs it. -->
<@js src="resource/js/vendor/foundation/foundation.tooltip.js"/>
<@js src="resource/js/vendor/foundation/foundation.dropdown.js"/>
<@js src="resource/js/vendor/foundation/foundation.tab.js" />
<@js src="resource/js/vendor/foundation/foundation.reveal.js"/>
<@js src="resource/js/vendor/foundation/foundation.slider.js"/>
<@js src="resource/js/vendor/imagesLoaded.js" />

<#-- // include components used universally here -->
<@js src="resource/js/components/toggle.js"/>
<@js src="resource/js/components/truncate_elem.js"/>
<@js src="resource/js/components/tooltip_hover.js"/>




