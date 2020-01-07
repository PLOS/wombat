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

<#--This macro is used in the body of the page, where the ad should be displayed-->
<#--It is tightly coupled with doubleClickAdHead, which produces matching Javascript in the head tag-->
<#--The ID used here must match the ID used in doubleClickAdHead - this is static for the -->
<#--banner ads (always ID 0) and skyscraper ads (always ID 1). However, medium rectangle ads have-->
<#--dynamic IDs defined in doubleClickAdHeadSetup-->
<#macro doubleClickAdZone id width height>
<!-- DoubleClick Ad Zone -->
  <div class='advertisement' id='div-gpt-ad-1458247671871-${id}' style='width:${width}px; height:${height}px;'>
    <script type='text/javascript'>
      googletag.cmd.push(function() { googletag.display('div-gpt-ad-1458247671871-${id}'); });
    </script>
  </div>
</#macro>