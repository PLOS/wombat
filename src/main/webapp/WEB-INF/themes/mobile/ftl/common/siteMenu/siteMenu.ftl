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

<#include "siteMenuFlag.ftl" />
<#if hasSiteMenu>

  <#macro menuSpecialSection title>
  <!-- dont' do anything for mobile -->

  </#macro>
  <#macro siteMenuCalloutSpecial  buttonText buttonTarget linkText linkTarget >
  <p class="button-contain special">
    <a class="button button-default" href="${buttonTarget}">
    ${buttonText}
    </a>
    <a class="button-link" href="${linkTarget}">
    ${linkText}
    </a>
  </p>

  </#macro>

  <#macro siteMenuCalloutSingleButton buttonText buttonTarget>
    <p class="button-contain special">
      <a class="rounded coloration-white-on-color" href="${buttonTarget}">
      ${buttonText}
      </a>
    </p>
    </div>  <!-- opens in siteMenuCalloutDescription -->
  </#macro>

  <#macro menuGroup title singleColumn=false containsCallout=false>
    <#nested/>
  </#macro>

  <#macro menuSection title containsSubmit=false>
  <li class="accordion-item">
    <a class="expander">
      <span class="arrow">Expand</span>
    ${title}
    </a>

    <ul class="secondary accordion-content">
      <#nested>
    </ul>
  </li>
  </#macro>
  <#macro menuLink href id=''>
  <li>
    <a class="btn-lg" href="${href}"  <#if id?has_content>id=${id}</#if>><#nested></a>
  </li>
  </#macro>
  <#macro submitBlock>
  <#-- TODO Document -->
  </#macro>
<div id="common-menu-container" class="full-menu-container coloration-border-top">
  <nav class="full-menu">
    <ul class="primary accordion">
      <#include "siteMenuItems.ftl" />

      <#-- TODO: implement when we support logged-in functionality.
      <li class="accordion-item">
        <a class="expander">
          <span class="arrow">Expand</span>
          Create an Account
        </a>

        <p class="accordion-content secondary">[TEMP - CONTENT]</p>
      </li>
      <li class="accordion-item">
        <a class="expander">
          <span class="arrow">Expand</span>
          Sign In
        </a>

        <p class="accordion-content secondary">[TEMP - CONTENT]</p>
      </li>
      -->

    </ul>
  </nav>

  <div class="full-menu-tout">
    <#macro siteMenuCalloutHeadline>
      <p><small id="callout-headline" class="lead-in"><#nested/></small></p>
    </#macro>
    <#macro siteMenuCalloutDescription>

       <p> <small id="callout-description" class="pad-below"><#nested/></small></p>

    </#macro>
    <#macro siteMenuCalloutButton href>
  
      <p><a class="rounded coloration-white-on-color" href="<@siteLink path='s/' + href/>" id="callout-button">
        <#nested/>
      </a></p>
    </#macro>
    <#macro siteMenuCalloutSpecial  buttonText buttonTarget linkText linkTarget >
     
      <p>
          <a id="callout-button" class="coloration-light-text" href="${buttonTarget}"> &raquo;
          ${buttonText}
          </a>
        </p>
        <p>
          <a class="rounded coloration-light-text header-style" id="callout-link" href="${linkTarget}">
          ${linkText}
          </a>
        </p>
     

    </#macro>

    <div id="submit-manuscript-container">
      <#include "siteMenuCallout.ftl" />
    </div>
  </div>

</div>

</#if>
