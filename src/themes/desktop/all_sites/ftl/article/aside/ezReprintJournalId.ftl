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

<#--

  We must assign this value based on the article journal of original publication (defined by eISSN), not necessarily
  the journal of the site we are rendering right now.

  It would be nice if we could define the actual ezPrintJournalId values in each journal's theme, but that would entail
  looking up another theme based on the article's eISSN and then retrieving the EzReprint ID from the other theme. That
  would require a lot of middle-tier support for something that exists only in plos-themes.

  Thus, we're sticking with the same global table from eISSNs to EzReprint IDs as in Ambra. Refactoring it out would be
  great if practical.

  -->
<#assign ezReprintJournalId = "" />
<#if article.eIssn == "1545-7885"><#-- PlosBiology -->
  <#assign ezReprintJournalId = "1" />
</#if>
<#if article.eIssn == "1549-1676"><#-- PlosMedicine -->
  <#assign ezReprintJournalId = "2" />
</#if>
<#if article.eIssn == "1553-7358"><#-- PlosCompBiol -->
  <#assign ezReprintJournalId = "3" />
</#if>
<#if article.eIssn == "1553-7404"><#-- PlosGenetics -->
  <#assign ezReprintJournalId = "4" />
</#if>
<#if article.eIssn == "1553-7374"><#-- PlosPathogens -->
  <#assign ezReprintJournalId = "5" />
</#if>
<#if article.eIssn == "1932-6203"><#-- PlosONE -->
  <#assign ezReprintJournalId = "7" />
</#if>
<#if article.eIssn == "1555-5887"><#-- PlosClinicalTrials -->
  <#assign ezReprintJournalId = "6" />
</#if>
<#if article.eIssn == "1935-2735"><#-- PlosNtds -->
  <#assign ezReprintJournalId = "316" />
</#if>
