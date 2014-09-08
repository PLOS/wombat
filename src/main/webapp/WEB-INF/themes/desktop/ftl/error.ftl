<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">

<#assign title = "Server Error" />
<#include "common/head.ftl" />

<#include "common/journalStyle.ftl" />
<body class="static ${journalStyle}">
<#include "common/header/header.ftl" />

   <article class="error-page">
     <h1>Something's Broken!</h1>

     <div class="content">
       <p>We're sorry, our server has encountered an internal error or misconfiguration and is unable to complete your
         request. This is likely a temporary condition so please try again later.</p>

       <p>Thank you for your patience.</p>

       <div title="+&nbsp;Technical Information for Developers">
         <pre>${stackTrace}</pre>
       </div>

     </div>
  </article>
<#include "common/footer/footer.ftl" />
<@js src="resource/js/global.js" />
<@renderJs />

</body>
</html>
