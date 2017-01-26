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

<input type="hidden" name="reCaptcha-info" id="reCaptcha-info" value='${recaptchaPublicKey!''}'/>

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
  <div id="media_coverage_addition">Found additional news media or blog coverage for the article? <a href="#"
                                                                                                     data-reveal-id="media-coverage-modal">Please
    let us know.</a></div>

  <div id="media-coverage-data"></div>

  <div id="media-coverage-modal" data-reveal aria-labelledby="modalTitle" aria-hidden="true" role="dialog">
    <h2>Submit a link to media coverage of this article</h2>
    <a class="close-reveal-modal" aria-label="Close"></a>

    <div id="media-coverage-form" class="cf">
      <form data-doi="${article.doi}" class="form standard">
        <div id="mcform-error"></div>
        <ul>
          <li class="small cf">
            <div>
              <label for="mcform-name">Your name:</label>
              <input type="text" name="name" id="mcform-name" <#if user??>value="${user.displayName}"</#if>>
              <span class="form-error"></span>
            </div>
            <div class="last">
              <label for="mcform-email">Your email address:</label>
              <input type="text" name="email" id="mcform-email" <#if user??>value="${user.email}"</#if>>
              <span class="form-error"></span>
            </div>
          </li>
          <li class="cf">
            <label for="mcform-link">Media link URL (website)</label>
            <input type="text" name="link" id="mcform-link" placeholder="e.g. http://www...." maxlength="1000">
            <span class="form-error"></span>
          </li>
          <li class="cf">
            <label for="mcform-title">Article Title</label>
            <input type="text" name="title" id="mcform-title" placeholder="" maxlength="1000">
            <span class="form-error"></span>
          </li>
          <li class="cf">
            <label for="mcform-publishedOn">Published on</label>
            <input type="text" name="publishedOn" id="mcform-publishedOn" placeholder="YYYY-MM-DD" maxlength="1000">
            <span class="form-error"></span>
          </li>
          <li><label for="mcform-comment">Comments:</label>
            <textarea rows="3" cols="50" name="comment" id="mcform-comment" maxlength="1000"></textarea>
          </li>
          <li><label>Security Check:</label>

            <p class="note">This question is to determine if you're a human visitor in order to prevent automated spam
              submissions. </p>

            <div id="mcform-captcha"></div>
            <span class="form-error"></span>
          </li>
          <li>

            <button type="submit" class="button primary">submit</button>
            <span class="button cancel">cancel</span>

          </li>
        </ul>
      </form>
    </div>

    <div id="media-coverage-success">
      <h3>Thank you!</h3>

      <p>Your submission will be reviewed by our staff.</p>
    </div>

    <div id="media-coverage-failure">
      <p>There was a problem with your submission. Please try again later.</p>
    </div>
  </div>
</div>

<#include "recommendedArticles.ftl" />