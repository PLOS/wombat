<#function alterJournalTitle journalTitle>
    <#--This is a hook that individual journals can use to implement their own alteration to journal titles and prevent
     the pollution of PLOS specific code in Wombat. See plos_themes/code/general/Plos/ftl/article/alterJournalTitle.ftl
     for an example implementation.-->
    <#return journalTitle />
</#function>