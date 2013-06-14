<html>
<head>
    <title>Home</title>
</head>
<body>
<h1>
    Hello world!
</h1>

<#include "snippet.ftl" />

<P> The time on the server is ${serverTime}. </P>

<#if testObject??>
<p>SOA test:
<pre>
${testObject}
</pre>
</p>
<#else>
Could not contact the SOA server.
</#if>

</body>
</html>