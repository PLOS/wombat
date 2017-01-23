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

<#include 'baseTemplates/default.ftl'>
<#assign title = 'PLOS - Browse' />
<#assign bodyId = 'page-browse' />

<@themeConfig map="journal" value="journalKey" ; journalKey>
  <#assign journalKey =journalKey/>
</@themeConfig>

<@page_header />
<div id="browse-content" class="content">
  <div id="browse-container"></div>
</div>

<div id="subject-list-template" style="display: none;">
  <nav class="browse-level active">
    <ul class="browse-list">__TAXONOMY_LINKS__</ul>
  </nav>
</div>

<div id="subject-term-template" style="display: none;">
  <li>
    <a href="browse/__TAXONOMY_TERM_ESCAPED__?filterJournals=${journalKey}" class="browse-link
      browse-left">__TAXONOMY_TERM_LEAF__</a>
    <a class="__CHILD_LINK_STYLE__" data-term="__TAXONOMY_TERM_FULL_PATH__">
      View Subcategories
      <span class="arrow">arrow</span>
    </a>
  </li>
</div>
<#include "common/configJs.ftl" />
<@page_footer />
