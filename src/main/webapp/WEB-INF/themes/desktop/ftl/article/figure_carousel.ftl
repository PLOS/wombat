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

<#if figures?has_content>
<div id="figure-carousel-section">
  <h2>Figures</h2>

  <div id="figure-carousel">

    <div class="carousel-wrapper"><#-- Horizontally scrolling view window -->
      <div class="slider"><#-- Large-width container -->
        <#list figures as figure>

          <div class="carousel-item lightbox-figure" data-doi="${figure.doi?html}">

              <@siteLink handlerName="figureImage" queryParameters=(versionPtr + {"size": "inline", "id": figure.doi}) ; src>
                <img src="${src?html}"
                     <#if figure.title?has_content >
                     alt="${figure.title?html}"
                     </#if>
                />
              </@siteLink>

            </div>
        </#list>
      </div>
    </div>

    <div class="carousel-control">
      <span class="button previous"></span>
      <span class="button next"></span>
    </div>
    <div class="carousel-page-buttons">

    </div>
  </div>
</div>
<@js src="resource/js/vendor/jquery.touchswipe.js" />
<@js src="resource/js/components/figure_carousel.js" />
<@js src="resource/js/vendor/jquery.dotdotdot.js" />
</#if>

