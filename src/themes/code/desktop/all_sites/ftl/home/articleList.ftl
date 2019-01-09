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

  <h3>Recently Published Articles</h3>

  <#if sections.recent?? >
  <section>
    <ul id="article-results" class="list-plain results">
    <#list sections.recent as article>
      <li>
        <a href="<@siteLink path="article?id=${article.doi}"/>" data-tooltip class="truncated-tooltip" title="${article.title}">${article.title}</a>
      </li>
    </#list>
    </ul>
  </section>

  <#else>
  <section>
    <ul id="article-results" class="list-plain results">
      <li>
        <div class="error">
          Our system is having a bad day. Please check back later.
        </div>
      </li>
    </ul>
  </section>
  </#if>

  <#function daysBeforeNow numberOfDays>
    <#assign daysInMilliseconds = numberOfDays * 86400000 />
    <#return (.now?long - daysInMilliseconds)?number_to_datetime?string("yyyy-MM-dd") />
  </#function>
  <@themeConfig map="journal" value="journalKey" ; journalKey>
    <#assign allArticlesPath = "search?sortOrder=DATE_NEWEST_FIRST" +
    "&filterStartDate=" + daysBeforeNow(30) +
    "&filterEndDate=" + .now?string("yyyy-MM-dd") +
    "&filterJournals=" + journalKey + "&q="/>
  </@themeConfig>
  <div class="more-link"><a href=${allArticlesPath}>See all articles</a></div>
  <#--recent articles-->
  <@js target="resource/js/vendor/jquery.dotdotdot.js"/>