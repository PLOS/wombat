<#-- Create a URL to a DOI resolver using a given DOI.

The domain used for the DOI resolver URL can be customized in themes via doiResolverDomain.ftl -->

<#-- assign doiResolverDomain var -->
<#include "../common/doiResolverDomain.ftl" />

<#function doiResolverLink doi>

  <#assign doi = doi.replace("info:doi/", "") />
  <#return "http://" + doiResolverDomain + "/" + doi />

</#function>