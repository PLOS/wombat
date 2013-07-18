<div id="container-main">

    <#include "../common/header.ftl" />

    <div id="article-content" class="content" data-article-id="0059893">
        <article class="article-item">
            <a class="save-article circular coloration-text-color" data-list-type="individual">x</a>
            <h5 class="item-title lead-in">RESEARCH IN TRANSLATION</h5>

            <h2 class="article-title">${article.title}</h2>

            <p class="author-list">
            <#list article.authors as author>
                <a class="author-info" data-author-id="${author_index}">
                ${author.fullName}</a><#if author_has_next><#-- no space -->,</#if>
            </#list>
            </p>

            <div class="retraction red-alert">
                <h3>Retraction:</h3>

                <p>Comparative Expression Profile of miRNA and mRNA in Primary Peripheral Blood Mononuclear Cells
                    Infected with Human Immunodeficiency Virus (HIV-1)</p>

                <p>The authors wish to retract this article for the following reason:</p>

                <p>Upon re-evaluation of the analyses performed, we discovered an error in the data fed into the
                    software, which resulted in incorrect results in Table 2 and Figure 2. During the initial analysis,
                    we eliminated miRNAs if they showed an expression CT of value 35 in over 75% of the samples. This
                    decision was based on the instructions from the software during the initial data feed process for
                    the selection of particular miRNAs (row) for exclusion. Unfortunately, the software included the
                    excluded miRNAs as controls along with the endogenous controls and analyzed the data.</p>
            </div>
            <div class="correction-alert coloration-text-color">
                <span class="plos-font">e</span> Correction added <span class="bold">08 Jul 2012</span>
            </div><#--end retraction-->
        <#-- In articleSuffix.ftl: Close <article class="article-item"> -->
        <#-- In articleSuffix.ftl: Close <div id="article-content"> -->
        <#-- In articleSuffix.ftl: Close <div id="container-main"> -->
