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

<h2>Save this search</h2>

  Save search as<span class="required">*</span> <br/>
  <input type="text" class="big-box" id="text_name_savedsearch"/><br/>
  <input type="hidden" id="alert_query_savedsearch" value=''/>

  <div class="check-box"><strong>Create an email alert:</strong>
    <input type="checkbox" checked id="cb_weekly_savedsearch"/>Weekly
    <input type="checkbox" id="cb_monthly_savedsearch"/>Monthly
  </div>

  <div class="grey-text">You can change these options in your Profile &gt; Search Alerts</div>

  <div class="button-wrapper">
    <input type="button" class="primary" id="btn-save-savedsearch" value="Save"/>
    <input type="button" class="btn-cancel-savedsearch" value="Cancel"/>
    <span class="errortext" id="span_error_savedsearch"></span>
  </div>