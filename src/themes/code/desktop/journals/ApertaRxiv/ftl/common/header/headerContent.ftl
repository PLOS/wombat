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

<header>

<#--If we need ads we can add them  -->
  <#--<div id="topslot" class="head-top">-->
  <#--<#include "adSlotTop.ftl" />-->
  <#--</div>-->

  <nav class="nav-main">
    <ul>
      <li class="search">
        <a href="#" data-component="toggleme" data-target="#navsearch"><span>Search</span><i class="icon-search"></i></a>
      </li>
      <li class="logo">

        <div itemscope itemtype="http://schema.org/Organization">
          <a itemprop="url" href="<@siteLink path="." />">
            <img itemprop="logo" src="<@siteLink path="/resource/img/logo.svg" />" alt="${siteTitle}" />
          </a>
          <div>
      </li>
      <li class="menu">
        <a href="#"><span>How it works</span><i class="icon-menu"></i></a>
      </li>

    </ul>
    <div id="navsearch" class="head-search hide">
    <#include "search.ftl" />
    </div>

  </nav>

</header>