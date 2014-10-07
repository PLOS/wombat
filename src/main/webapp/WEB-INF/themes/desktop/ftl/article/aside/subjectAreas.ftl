
<#--<#if articleInfoX.orderedCategories?? && articleType.heading != "Correction" && articleType.heading != "Expression of Concern" && articleType.heading != "Retraction" >-->
<div class="subject-areas-container">
  <h3>Subject Areas <span id="subjInfo">&nbsp;</span></h3>
  <div id="subjInfoText">
    <span class="inline-intro">We want your feedback.</span> Do these subject areas make sense for this article? If not, click the flag next to the incorrect subject area and we will review it. Thanks for your help!
  </div>

 <ul id="subjectList">
   <li>
     <a href='${legacyUrlPrefix}/search/advanced?unformattedQuery=subject%3A"Antiretroviral+therapy"'>antiretroviraal</a>
     <span data-categoryid="54321<#--${categoryModel.categoryID?c}-->"
           data-articleid="12345av<#--${ articleInfoX.id?c}-->"
           data-categoryname="antiretroviraal<#--${categoryNames[categoryModel_index]}-->"
           class="taxo-flag<#--<#if categoryModel.flagged> flagged"</#if>-->">&nbsp;</span>
   </li>
   <li>
     <a href='${legacyUrlPrefix}/search/advanced?unformattedQuery=subject%3A"Blood+plasma"'>blood plasma</a>
     <span>&nbsp;</span>
   </li>
   <li>
     <a>cholesterol</a>
     <span>&nbsp;</span>
   </li>
   <li>
     <a>drug therapy</a>
     <span>&nbsp;</span>
   </li>
   <li>
     <a>hiv-1</a>
     <span>&nbsp;</span>
   </li>
   <li>
     <a>lipid analysis</a>
     <span>&nbsp;</span>
   </li>
   <li>
     <a>lipds</a>
     <span>&nbsp;</span>
   </li>
   <li>
     <a>lipoproteins</a>
     <span>&nbsp;</span>
   </li>

 </ul>
  <#--  TODO: delete the following after John F reviews
    This bit of logic is a bit odd and perhaps should be moved to another tier due to its complexity
    For speed of retrieval, we store categories like so in the database:

     19563 | /Biology and life sciences/Biochemistry/Proteins/Structural proteins                                                        |
     40317 | /Biology and life sciences/Computational biology/Genome analysis/Genomic databases                                          |
     40319 | /Biology and life sciences/Genetics/Genomics/Genome analysis/Genomic databases                                              |
     32801 | /Biology and life sciences/Genetics/Genomics/Microbial genomics/Bacterial genomics                                          |
     17227 | /Biology and life sciences/Microbiology/Bacteriology/Bacterial genomics                                                     |
     32805 | /Biology and life sciences/Microbiology/Microbial genomics/Bacterial genomics                                               |
     21885 | /Biology and life sciences/Microbiology/Virology/Viral replication/Viral packaging                                          |
     17961 | /Biology and life sciences/Neuroscience/Cognitive science/Artificial intelligence/Artificial neural networks                |
     42611 | /Biology and life sciences/Organisms/Viruses/Bacteriophages                                                                 |
     33271 | /Computer and information sciences/Artificial intelligence/Artificial neural networks                                       |
     42659 | /Research and analysis methods/Database and informatics methods/Biological databases/Genomic databases                      |
     42886 | /Research and analysis methods/Database and informatics methods/Biological databases/Sequence databases                     |
     32187 | /Research and analysis methods/Molecular biology techniques/Sequencing techniques/Sequence analysis/Sequence databases      |
     19657 | /Research and analysis methods/Molecular biology techniques/Sequencing techniques/Sequence analysis/Sequence motif analysis

    Notice Bacterial genomics, it appears 3 times with a differing path.  But logically, this is the same subject area with
    differing paths leading to it.  Which one we select below may be non deterministic.  But I have to display a unique
    set of subject areas.  Hence when the user performs an action of flagging, I'm only flagging one of the above terms
    Though all three may be associated with an article.  When reporting on this data, it's imperative we don't group by ID, but the
    last term in the path.
  -->
  <#--  <#assign categoryNames = [] />
    <#assign finalCategories = [] />
    <#list articleInfoX.orderedCategories as categoryModel>
      <#if (finalCategories?size) gt max_categories>
        <#break>
      </#if>
      <#if categoryModel.subCategory?has_content>
        <#assign categoryName = categoryModel.subCategory />
      <#else>
        <#assign categoryName = categoryModel.mainCategory />
      </#if>

      <#if !categoryNames?seq_contains(categoryName)>
        <#assign categoryNames = categoryNames + [categoryName] />
        <#assign finalCategories = finalCategories + [categoryModel] />
      </#if>
    </#list>
    <#list finalCategories as categoryModel>
      <li id="subj${item_index}">
        <@s.url id="advancedSearchURL" unformattedQuery="subject:\"${categoryNames[categoryModel_index]}\"" namespace="/search" action="advancedSearch" />
        <a href="${advancedSearchURL}" title="Search for articles in the subject area:'${categoryNames[categoryModel_index]}'" id="subjAnchor">
          ${categoryNames[categoryModel_index]}
        </a>
        <div data-categoryid="${categoryModel.categoryID?c}" data-articleid="${articleInfoX.id?c}" data-categoryname="${categoryNames[categoryModel_index]}"
 class="flagImage<#if categoryModel.flagged> flagged</#if>" title="<#if !categoryModel.flagged>Flag '${categoryNames[categoryModel_index]}' as inappropriate<#else>Remove inappropriate flag from '${categoryNames[categoryModel_index]}'</#if>">&nbsp;</div>
      </li>
    </#list>
  </ul>
</div>
</#if>-->
