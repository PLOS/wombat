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



</main><#-- opened in headerContainer.ftl -->

<footer id="pageftr">
  <div class="row">
  

     <div class="block x-small">   
      
    	<#include "footerSecondaryLinks.ftl" />
  </div>
   
    <div class="block xx-small">	
    <#include "footerTertiaryLinks.ftl" />
    </div>
    <div class="block xx-small">
     <#include "footerPrimaryLinks.ftl" />
     </div>
     <div class="block x-small">
       	<#macro footerLogo src alt>
	 		 <img src="${src}" alt="${alt}" class="logo-footer"/>
      </#macro> 	
      <#include "footerLogo.ftl" />
    </div>
	 

  <#include "footerCredits.ftl" />   
     
  </div>
 

<@js src="resource/js/global.js" />

</footer>

