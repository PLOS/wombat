<!DOCTYPE html>
<#--
  Plain page for showing 500 errors on requests that can't be matched to any site.
  -->
<html>
<head>
  <title>500 Internal Server Error</title>
</head>
<body>
<div id="container-main">
  <div class="page-not-found">

    <h1>Something's Broken!</h1>

    <p>
      We're sorry, our server has encountered an internal error or misconfiguration and is unable to complete
      your request. This is likely a temporary condition so please try again later.
    </p>

    <p>Thank you for your patience.</p>

    <div id="stackTrace">
      <h2>Technical Information for Developers</h2>
      <pre>${stackTrace}</pre>
    </div>

  </div>
</div>
</body>
</html>
