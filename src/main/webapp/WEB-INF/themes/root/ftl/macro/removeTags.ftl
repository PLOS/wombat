<#--

  Removes XML tags from a string, but leaves the text between the tags. For example, changes
    A <italic>Staphylococcus aureus</italic> Small RNA
  to
    A Staphylococcus aureus Small RNA

  This solution is quick and dirty; there are almost certainly edge-case bugs. One is if a tag attribute contains a '>'
  character, e.g.
    <tag mode="foo -> bar">
  A real XML library is generally preferable to such hacking, but it's difficult to apply one here because there isn't
  really an XML document to parse, just snippets of text that happen to contain XML syntax.

  -->
<#function removeTags string>
  <#return string?replace('</?[^<>]*>', '', 'r') />
</#function>
