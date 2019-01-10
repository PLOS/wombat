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
<#--AKA ORCID api key-->
<script type="text/javascript">
  var ORCID_CLIENT_ID = <@themeConfig map="orcid" value="clientId" ; client_id>"${client_id}"</@themeConfig>;
  var ORCID_CLIENT_SECRET = <@themeConfig map="orcid" value="clientSecret" ; client_secret>"${client_secret}"</@themeConfig>;
  var ORCID_HOST = <@themeConfig map="orcid" value="host" ; host>"${host}"</@themeConfig>;
  var ORCID_REDIRECT_URI = <@themeConfig map="orcid" value="redirectUri" ; redirectUri>"${redirectUri}"</@themeConfig>;
</script>