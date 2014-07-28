<#--
    This file shows metadata about this application and its service component. It prints the components' version numbers
    (first this webapp, then the service component, separated by a slash), to be put into a page footer or similar place
    for human readability. It also prints an HTML block comment with more complete build information, for developer use.

    Note that the block comment following this one should in fact be an HTML block comment ('!'), not a FreeMarker block
    comment ('#') as usual. We want it to show up in page source.
  -->
<!--
  Webapp build: <@buildInfo component='webapp' field='version' /> at <@buildInfo component='webapp' field='date' /> by <@buildInfo component='webapp' field='user' />, commit: <@buildInfo component='webapp' field='commitIdAbbrev' />
  Service build: <@buildInfo component='service' field='version' /> at <@buildInfo component='service' field='date' /> by <@buildInfo component='service' field='user' /> , commit: <@buildInfo component='service' field='commitIdAbbrev' />
  -->
<#--<@buildInfo component='webapp' field='version' />/<@buildInfo component='service' field='version' /><#t>-->
3.0.0<#t>
