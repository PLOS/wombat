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

<@js src="resource/js/taxonomy-browser.js" />
<div id="taxonomy-browser" class="areas" data-search-url="<@siteLink handlerName="browse"/>">
  <div class="wrapper">
    <div class="taxonomy-header">
      Browse Subject Areas
      <div id="subjInfo">?</div>
      <div id="subjInfoText">
        <p>Click through the PLOS taxonomy to find articles in your field.</p>
        <p>For more information about PLOS Subject Areas, click
          <a href="<@siteLink path='s/help-using-this-site#loc-subject-areas'/>">here</a>.
        </p>
      </div>
    </div>
    <div class="levels">
      <div class="levels-container cf">
        <div class="levels-position"></div>
      </div>
      <a href="#" class="prev"></a>
      <a href="#" class="next active"></a>
    </div>
  </div>
  <div class="taxonomy-browser-border-bottom"></div>
</div>