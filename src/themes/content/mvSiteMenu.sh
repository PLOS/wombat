for x in PlosBiology  PlosCollections  PlosCompBiol  PlosGenetics  PlosMedicine  PlosNtds  PlosOne  PlosPathogens
do
    mkdir -p $x/ftl/common/siteMenu/
    mv ../code/mobile/$x/ftl/common/siteMenu/siteMenuFooter.ftl $x/ftl/common/siteMenu/
done