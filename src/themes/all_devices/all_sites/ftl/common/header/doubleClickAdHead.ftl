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

<#--This script initializes DoubleClick and is tightly coupled with doubleClickAdZone -->
<#--The ID used here must match the ID used in doubleClickAdZone - this is static for the -->
<#--banner ads (always ID 0) and skyscraper ads (always ID 1). However, medium rectangle ads have-->
<#--dynamic IDs defined in doubleClickAdHeadSetup-->

<!-- DoubleClick overall ad setup script -->
<script type='text/javascript'>
  var googletag = googletag || {};
  googletag.cmd = googletag.cmd || [];
  (function() {
    var gads = document.createElement('script');
    gads.async = true;
    gads.type = 'text/javascript';
    var useSSL = 'https:' == document.location.protocol;
    gads.src = (useSSL ? 'https:' : 'http:') +
        '//www.googletagservices.com/tag/js/gpt.js';
    var node = document.getElementsByTagName('script')[0];
    node.parentNode.insertBefore(gads, node);
  })();
</script>

<!-- DoubleClick ad slot setup script -->
<#macro doubleClickAdHead>
  <script id="doubleClickSetupScript" type='text/javascript'>
    googletag.cmd.push(function() {
      <#nested/>
      googletag.pubads().enableSingleRequest();
      googletag.enableServices();
    });
  </script>
</#macro>

<#macro doubleClickBannerAd zone width=728 height=90 count=0>
  googletag.defineSlot('/75507958/${zone}', [${width}, ${height}], 'div-gpt-ad-1458247671871-${count}').addService(googletag.pubads());
</#macro>

<#macro doubleClickSkyscraperAd zone width=160 height=600 count=1>
  googletag.defineSlot('/75507958/${zone}', [${width}, ${height}], 'div-gpt-ad-1458247671871-${count}').addService(googletag.pubads());
</#macro>

<#macro doubleClickMediumRectangleAd zone count width=300 height=250>
  googletag.defineSlot('/75507958/${zone}', [${width}, ${height}], 'div-gpt-ad-1458247671871-${count}').addService(googletag.pubads());
</#macro>