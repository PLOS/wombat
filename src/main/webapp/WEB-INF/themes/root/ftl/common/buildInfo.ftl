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
    This file shows metadata about this application and its service component as an HTML block comment,
    for developer use.

    Note that the block comment following this one should in fact be an HTML block comment ('!'),
    not a FreeMarker block comment ('#') as usual. We want it to show up in page source.
  -->
<!--
  Webapp build:  <@buildInfo component='webapp'  field='version' /> at <@buildInfo component='webapp'  field='date' /> by <@buildInfo component='webapp'  field='user' />, commit: <@buildInfo component='webapp'  field='commitIdAbbrev' />
  Service build: <@buildInfo component='service' field='version' /> at <@buildInfo component='service' field='date' /> by <@buildInfo component='service' field='user' />, commit: <@buildInfo component='service' field='commitIdAbbrev' />
  Enabled dev features: <@buildInfo component='webapp' field='enabledDevFeatures' />
  -->
