
<div id="media_coverage_addition">Found additional news media or blog coverage for the article? <a href="#"
 data-reveal-id="media-coverage-modal">Please
  let us know.</a></div>

<div id="media-coverage-modal" data-reveal aria-labelledby="modalTitle" aria-hidden="true" role="dialog">
  <h2>Submit a link to media coverage of this article</h2>
  <a class="close-reveal-modal" aria-label="Close"></a>

  <div id="media-coverage-form" class="cf">
    <form data-doi="${article.doi}" class="form standard">
      <div id="mcform-error"></div>
      <ul>
        <li><i>All fields are required except where indicated</i></li>
        <li class="small cf">
         <div id="name-and-email">
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
        <li><label for="mcform-comment">Comments (<i>Optional</i>):</label>
          <textarea rows="3" cols="50" name="comment" id="mcform-comment" maxlength="1000"></textarea>
        </li>
        <li>
          <label for="mcform-consent">
            <input type="checkbox" id="mcform-consent" name="consent">
            I have read and agree to the terms of the <a href="https://www.plos.org/privacy-policy" target="_blank" title="Page opens in new window">PLOS Privacy Policy</a> and hereby consent to send my personal information to PLOS.
          </label>
          <div class="form-error"></div>
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

<@js src="resource/js/vendor/foundation/foundation.js"/>
<@js src="resource/js/vendor/foundation/foundation.reveal.js"/>
<@js src="resource/js/vendor/foundation-datepicker.min.js"/>

<@js src="resource/js/pages/media_curation_form.js"/>