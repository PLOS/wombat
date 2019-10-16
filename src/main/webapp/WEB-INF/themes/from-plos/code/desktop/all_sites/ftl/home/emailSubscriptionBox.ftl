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

<div class="plos-row">
    <div class="home-email-subscription">
        <form>
          <div>
            <h2>Get new content from <@themeConfig map="journal" value="journalName" /> in your inbox</h2>
            <input type="hidden" name="journalKey" value="<@themeConfig map="journal" value="journalKey" />">

            <div class="input-group">

                <input type="text" id="email" name="email" placeholder="Your email address">
                <label for="email" class="error"></label>
            </div>
          </div>
          <div class="consent">
          <label for="email_consent" class="email_consent">
            <input type="checkbox" id="email_consent" name="consent" value="true">
            I have read and agree to the terms of the <a href="https://www.plos.org/privacy-policy" target="_blank" title="Page opens in new window">PLOS Privacy Policy</a>
          </label>
            <label class="error_consent"></label>
          </div>
          <div class="button-row">
            <button type="submit" class="button-default">Sign up</button>
          </div>
        </form>
        <div class="success-message">
            <h2>Thank you! You have successfully subscribed to the <@themeConfig map="journal" value="journalName" /> newsletter.</h2>
        </div>
        <div class="error-message">
            <h2>Sorry, an error occurred while sending your subscription. Please try again later.</h2>
            <a class="button-default retry" href="#">Try again</a>
        </div>
    </div>
</div>

<@js src="resource/js/util/class.js"/>
<@js src="resource/js/components/home_subscription.js"/>
