<script type="text/javascript">
    var WombatConfig = WombatConfig || {};
    WombatConfig.resourcePath = "<@siteLink handlerName="staticResource" wildcardValues=[""]/>";
    WombatConfig.imgPath = "<@siteLink handlerName="staticResource" wildcardValues=["img/"]/>";
    WombatConfig.journalKey = <@themeConfig map="journal" value="journalKey" ; journalKey>"${journalKey}"</@themeConfig>;
    WombatConfig.figurePath = "<@siteLink handlerName="assetFile" />";
    WombatConfig.metrics = WombatConfig.metrics || {};
    WombatConfig.metrics.referenceUrl      = "http://lagotto.io/plos";
    WombatConfig.metrics.googleScholarUrl  = "http://scholar.google.com/scholar";
    WombatConfig.metrics.googleScholarCitationUrl  = WombatConfig.metrics.googleScholarUrl + "?hl=en&lr=&cites=";
    WombatConfig.metrics.crossrefUrl  = "http://www.crossref.org";
</script>