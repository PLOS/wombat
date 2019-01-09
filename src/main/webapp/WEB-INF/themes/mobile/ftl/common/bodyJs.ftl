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

<script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js" ></script>
<#if article??>
    <@themeConfig map="article" value="showFigShare" ; showFigShare>
        <#if (showFigShare?? && showFigShare)>
        <script type="text/javascript" src="https://widgets.figshare.com/static/figshare.js"></script>
        </#if>
    </@themeConfig>
</#if>

<#--<script src="https://code.jquery.com/jquery-2.1.4.min.js" ></script>-->

<#--<script>window.jQuery || document.write('<script src="<@siteLink path="resource/js/vendor/jquery-2.1.4.min.js" />""><\/script>')</script>-->
<@js target="resource/js/vendor/moment.js"/>
<@js target="resource/js/navigation.js" />
<@js target="resource/js/content.js" />
<@js target="resource/js/share.js" />
<@js target="resource/js/taxonomy.js" />
<@js target="resource/js/util/class.js"/>

<@js target="resource/js/vendor/underscore-min.js"/>
<@js target="resource/js/vendor/underscore.string.min.js"/>

<@renderJs />
