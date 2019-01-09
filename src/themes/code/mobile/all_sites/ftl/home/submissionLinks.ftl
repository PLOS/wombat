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

<div id="submission-buttons" class="two-btn-container">

<#-- TODO: mobile-specific versions of these pages  -->
<#macro submissionButtons submissionInstructionLink editorialManagerId
submissionInstructionText="Submission<br/>Instructions"
submitYourManuscriptText="Submit your<br/>Manuscript"
>
  <a id="submission-instructions" href="${submissionInstructionLink}"
     class="btn-left coloration-light-text">${submissionInstructionText}</a>
  <a id="submission-manuscript" href="https://www.editorialmanager.com/${editorialManagerId}/default.asp"
     class="btn-right coloration-light-text">${submitYourManuscriptText}</a>
</#macro>
<#include "submissionButtons.ftl" />
</div>
