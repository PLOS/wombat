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


<script type="text/template" id="articleRelatedContentSectionTemplate">
  <h3><%= section.title %></h3>
  <ul>
    <% _.each(items, function(item) { %>
    <li>
      <b><%= item.publication %></b>:
      "<a href="<%= item.referral %>"><%= item.title %></a>"
      <% if (!_.isEmpty(item.published_on)) { %>
        &nbsp;&nbsp;
        <%= moment(item.published_on).format("DD MMM YYYY") %>
      <% } %>
    </li>
    <% }); %>
  </ul>
</script>

<div id="media_coverage">
  <h2>Media Coverage of this Article <#--a href="" class="ir" title="More information">info</a--></h2>
  <div id="media-coverage-data">
  <#include "mediaCurationForm.ftl">
  </div>
</div>

<@js src="resource/js/pages/related_content.js"/>