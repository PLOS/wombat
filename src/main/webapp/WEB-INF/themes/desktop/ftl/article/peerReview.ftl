<html>
<#assign title = article.title, articleDoi = article.doi />

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />

<div class="set-grid">
<#include "articleHeader.ftl" />
  <section class="article-body">

    <#include "tabs.ftl" />

    <@displayTabList "PeerReview" />

    <div class="col-sm-10">

      <div class="content">
        <h2>Peer Review History</h2>
        <div class="stage">
          <div class="articleinfo">
            <p><strong>Editor:</strong> Sotirios Koutsopoulos</p>
            <p>  <strong>Reviewers:</strong> Lauren Bianchini, Katie Hickling</p>
          </div>
          <table class="table table-bordered reviewHistory">
            <tbody>
            <tr>
              <th>First Decision </th>
            </tr>
            <tr>
              <td>
                <a data-toggle="collapse" href="#collapseExample" role="button" aria-expanded="true" aria-controls="collapseExample" class="">
                  Decision Letter - Version 1
                </a>
                <div class="date"><span class="decisionDate">October 1, 2018</span></div>
                <div class="collapse in" id="collapseExample" aria-expanded="true" style="">
                  <div class="card card-body">
                    <p> Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident.</p>
                    <p> Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident.</p>
                    <p> Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident.</p>
                    <dl class="version-attachments">
                      <dt>Attachments</dt>
                      <dd><a href="#" target="_blank" title="Download .docx file">One Reviewer - v0.docx</a> (946 KB)</dd>
                      <dd><a href="#" target="_blank" title="Download .docx file">Another Reviewer - v0.docx</a> (772 KB)</dd>
                      <dd><a href="#" target="_blank" title="Download .docx file">Editor file - v0.docx</a> (1.1 MB)</dd>
                      <dd><a href="#" target="_blank" title="Download .csv file">One Data File 2018-09-27T11_53_36-0700.csv</a> (329 KB)</dd>
                    </dl>
                  </div>
                </div>
              </td>
            </tr>
            <tr class="version-attachments">
              <td>
                <a data-toggle="collapse" href="#collapseExample4" role="button" aria-expanded="false" aria-controls="collapseExample4">Author Response - Version 1</a>
                <div class="date"><span class="decisionDate">October 12, 2018</span></div>
                <div class="collapse" id="collapseExample4">
                  <div class="card card-body">
                    <p> Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident.</p>
                    <dl class="version-attachments">
                      <dt>Attachments</dt>
                      <dd><a href="#" target="_blank" title="Download PDF file">Response-to-Reviewers.pdf</a> (902 KB)</dd>
                    </dl>
                  </div>
                </div>
              </td>
            </tr>
            <tr>
              <th>Second Decision </th>
            </tr>
            <tr>
              <td>
                <a data-toggle="collapse" href="#collapseExample2" role="button" aria-expanded="false" aria-controls="collapseExample2">
                  Decision Letter - Version 2
                </a>
                <div class="date"><span class="decisionDate">November 1, 2018</span></div>
                <div class="collapse" id="collapseExample2">
                  <div class="card card-body">
                    <p>  Dear Dr. %LAST_NAME% Thank you very much for submitting your manuscript "Improving breast cancer risk prediction models: The addition of a polygenic risk score mammographic density and endogenous hormones" (PMEDICINE-D-17-04070) for consideration at PLOS Medicine. Your paper was evaluated by a senior editor and discussed among all the editors here. It was also discussed with an academic editor with relevant expertise and sent to independent reviewers including a statistical reviewer. The reviews are appended at the bottom of this email and any accompanying reviewer attachments can be seen via the link below. In light of these reviews I am afraid that we will not be able to accept the manuscript for publication in the journal in its current form but we would like to consider a revised version that addresses the reviewers' and editors' comments. Obviously we cannot make any decision about publication until we have seen the revised manuscript and your response and we plan to seek re-review by one or more of the reviewers. In revising the manuscript for further consideration your revisions should address the specific points made by each reviewer and the editors. Please also check the guidelines for revised papers at http://journals.plos.org/plosmedicine/s/revising-your-manuscript for any that apply to your paper. In your rebuttal letter you should indicate your response to the reviewers' and editors' comments the changes you have made in the manuscript and include either an excerpt of the revised text or the location (eg: page and line number) where each change can be found. Please submit a clean version of the paper as the main article file; a version with changes marked should be uploaded as a marked up manuscript. In addition we request that you upload any figures associated with your paper as individual TIF or EPS files with 300dpi resolution at resubmission; please read our figure guidelines for more information on our requirements: http://journals.plos.org/plosmedicine/s/figures. Need assistance with your figure files? While revising your submission please upload your figure files to the PACE digital diagnostic tool http://pace.apexcovantage.com/ PACE helps ensure that figures meet PLOS requirements. To use PACE you must first register as a user. Then login and navigate to the UPLOAD tab where you will find detailed instructions on how to use the tool. If you encounter any issues or have any questions when using PACE please email us at PLOSMedicine@plos.org. We also ask that you include a short non-technical Author Summary of your research to make findings accessible to a wide audience that includes both scientists and non-scientists. The Author Summary should immediately follow the Abstract in your revised manuscript. This text is subject to editorial change and should be distinct from the scientific abstract. Please see our author guidelines for more information: http://journals.plos.org/plosmedicine/s/revising-your-manuscript. We expect to receive your revised manuscript by %DATE_REVISION_DUE%. Please email us (plosmedicine@plos.org) to discuss this if you have any questions or concerns or would like to request an extension. We ask every co-author listed on the manuscript to fill in a contributing author statement. If any of the co-authors have not filled in the statement we will remind them to do so when the paper is revised. If all statements are not completed in a timely fashion this could hold up the re-review process. Should there be a problem getting one of your co-authors to fill in a statement we will be in contact. YOU MUST NOT ADD OR REMOVE AUTHORS UNLESS YOU HAVE ALERTED THE EDITOR HANDLING THE MANUSCRIPT TO THE CHANGE AND THEY SPECIFICALLY HAVE AGREED TO IT. Please use the following link to submit the revised manuscript: http://pmedicine.edmgr.com/ Your article can be found in the "Submissions Needing Revision" folder. To enhance the reproducibility of your results we recommend that you deposit your laboratory protocols in protocols.io where a protocol can be assigned its own identifier (DOI) such that it can be cited independently in the future. For instructions see http://journals.plos.org/plosmedicine/s/submission-guidelines#loc-methods. Please ensure that the paper adheres to the PLOS Data Availability Policy (see http://journals.plos.org/plosmedicine/s/data-availability) which requires that all data underlying the study's findings be provided in a repository or as Supporting Information. For data residing with a third party authors are required to provide instructions with contact information for obtaining the data. PLOS journals do not allow statements supported by "data not shown" or "unpublished results." For such statements authors must provide supporting data or cite public sources that include it. We look forward to receiving your revised manuscript.
                      Sincerely Clare Stone PhD. Managing Editor PLOS Medicine plosmedicine.org
                    </p>
                    <p>Requests from the editors:
                      Data – you say that you need to confer and discuss with colleagues. Please ensure this is resolved by the time of resubmission. We will not proceed with the next stage of peer review without data or clarification. PLOS Medicine requires sharing of the de-identified data underlying a study’s reported results unless it would be illegal or unethical for the authors to share their data. That a company considers the underlying data to be proprietary is not an acceptable reason according to the PLOS Data Policy for authors not to share the data. If a third-party dataset has been analyzed readers must be able to access the data that the authors were able to access. Additional information on the PLOS Data Policy is available at: http://journals.plos.org/plosone/s/data-availability Note that at a minimum PLOS requires a “minimal data set” that consists of the data set used to reach the conclusions drawn in the manuscript with related metadata and methods and any additional data required to replicate the reported study findings in their entirety. Authors do not need to submit their entire data set or the raw data collected during an investigation but the following must be submitted: (a) the values behind the means standard deviations and other measures reported; (b) the values used to build graphs.
                      Abstract – limitations are needed as the final sentence of the methods and findings section.
                      Title: should have the study design as the second part suggest a change from Improving breast cancer risk prediction models: The addition of a polygenic risk score mammographic density and endogenous hormones To Mammographic density endogenous hormones and polygenic risk scores; an improved breast cancer prediction model ….or similar
                      Validation: Per one of the referee’s suggestions we would like to see additional validation. We require an analysis plan : If a prospective analysis plan (from your funding proposal IRB or other ethics committee submission study protocol or other planning document written before analyzing the data) was used in designing the study please include the relevant prospectively written document with your revised manuscript as a supplemental file. If no such document currently exists please make sure that the Methods section transparently describes when analyses were planned and when/why any data-driven changes to analyses took place. If this document does exist and you provide in the supp files please provide a ‘shout out’ in the methods section to it and also mention if no deviations were made or if they were what they are. Please provide the relevant reporting guidelines (Perhaps STARD ?) : http://www.equator-network.org/ </p>


                    <p>Comments from the reviewers: </p>

                    <p>    Reviewer #1: Reviewer Text</p>

                    <p>Reviewer #2: This is an interesting and important study. The topic of prediction of breast cancer incidence has received a lot of attention over the years and the contribution of markers for better prediction is a continuing story. The authors present high quality work and make convincing points. - study design: seems efficient; for others to judge - statistical analysis: more or less standard well done - interpretation: * rather than p-values suggest to focus on point estimates and 95% CI rather than SE in all tables. Drop all p-values throughout the full paper. Irrelevant with large numbers. * interpretation of an increase in AUC is difficult. The trick of saying: "those at twice avg risk increase" is simply a translation of what we can expect with AUCs assuming the risk distributions are more or less normal. The key issue is is of course whether 2.27% has any clinical meaning? I have seen 10% as a threshold? The authors need to discuss this issue in more detail. * Preferably the authors move on to more direct measures of clinical usefulness. Stuart Baker has proposed Relative Utility (RU); Vickers proposed Net Benefit (NB). NB goes back a long way (Peirce 1884) and is very attractive if well explained. Several papers have tried to explain e.g. BMJ. 2016 Jan 25;352; Eur J Clin Invest. 2012 Feb;42(2):216-28; etc. * So consider a reasonable range of thresholds and then indicate how many marker measurements would be needed to identify 1 more women with cancer (in fact: net cases). A technical challenge is to go from case-control to cohort setting but I'm confident the authors can figure this out (BMC Med Inform Decis Mak. 2011 Jun 22;11:45.) * the measures of clinical usefulness should be in addition to the increases in AUC. The expression of AUC increase in ' units' is non standard and strange. AUC ranges between 0 and 1; so an increase is not 8.2 units but 0.082 (with 95% CI). * Fig 1 is difficult to understand. Suggest to replace by a Decision Curve Analysis (showing NB at the y axis). </p>

                    <p>Reviewer #3: Summary This is a straightforward study investigating comparing performance of a multi-model breast cancer risk-prediction model that includes molecular markers of risk with two well-established model that use only lifestyle/environmental risk factors (the Gail model and the Rosner-Colditz model). The manuscript is well-written and clear. There appear to be no major flaws in the study design and general approach and the conclusion that "the addition of a polygenic risk score mammographic density and endogenous hormones substantially improve(d) existing risk models" is justified. Major comments 1. My major issue with this manuscript relates to novelty or significance. That multi-modal risk model (lifestyle/environment plus mammographic density plus polygenic risk score) improves the performance of established risk models is in itself established so the concept is not new and the findings are as would be expected. The novelty here is the addition of endogenous serum hormones. It is unsurprising that adding these improves model performance further. Whether this is sufficient to warrant publication in PLoS Medicine is an editorial decision! 2. The study design is to use a nested case-control study within the Nurses Health Study to estimate the relative risks of invasive breast cancer with quartiles of PRS MD and circulating hormones. These relative risks were used in a risk model in addition to a risk score derived from the Gail model or the Rosner-Colditz model. Model performace was evaluated using the age-adjusted area under the receiver operator characteristic curve (AUC) as a measure of discrimintation. Ten-fold cross validation was used to assess the impact of over fitting. While this approach is a valid method to compare the performance of the different models there is no final model produced with defined characteristics. It is notable that the authors do not describe a specific model - with details of the coefficients for each of the input parameters. In other words the authors have not provided a definitive model with specified characteristics that could actually have a clinical application. Such a model would need external validation in an independent data set as well as validation using additional characteristics such as calibration and goodness of fit. My point here is not that this invalidates what has been done but that this to some extent limits the significance of the findings. Minor points 3. line 96 and elsewhere: citation [Rice et al] needs to be a supescripted number 4. Imputed risk factor data are used for some of the analyses. A table with the numbers of samples with missing data for each of the imputed variables ought to be provided. 5. Given the monotonic relationship between PRS MD and circulting hormones and risk estimation of risk in quartile would effectively result in the discarding of useful information. Treating these variables as continuous would surely improve discrimination further. Why not siply use the estiamte of risk that is directly derived from the calculated PRS? The authors need to justify this approach. Similarly external estimates of the risk associated with MD could have been used. Paul Pharoah </p>
                    <dl class="version-attachments">
                      <dt>Attachments</dt>
                      <dd><a href="#" target="_blank" title="Download .docx file">One Reviewer - v1.docx</a> (946 KB)</dd>
                      <dd><a href="#" target="_blank" title="Download .docx file">Another Reviewer - v1.docx</a> (772 KB)</dd>
                      <dd><a href="#" target="_blank" title="Download .docx file">Editor file - v1.docx</a> (1.1 MB)</dd>
                    </dl>
                  </div>
                </div>
              </td>
            </tr>
            <tr class="author-response">
              <td>
                <a data-toggle="collapse" href="#collapseExample5" role="button" aria-expanded="false" aria-controls="collapseExample5">Author Response - Version 2</a>
                <div class="date"><span class="decisionDate">December 3, 2018</span></div>
                <div class="collapse" id="collapseExample5">
                  <div class="card card-body">
                    <p> Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident.</p>
                  </div>
                </div>
              </td>
            </tr>
            <tr>
              <th>Accepted </th>
            </tr>
            <tr>
              <td>
                <a data-toggle="collapse" href="#collapseExample3" role="button" aria-expanded="false" aria-controls="collapseExample3">
                  Decision Letter - Version 3
                </a>
                <div class="date"><span class="decisionDate">December 29, 2018</span></div>
                <div class="collapse" id="collapseExample3">
                  <div class="card card-body">
                    Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident.
                  </div>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
          <div class="tpr-info">
            <h3>Open letter on the publication of peer review reports</h3>
            <p>PLOS recognises the benefits of transparency in the peer review process. Therefore, we enable the publication of all of the content of peer review and author responses alongside final, published articles. Reviewers remain anonymous, unless they choose to reveal their names.</p>

            <p>We encourage other journals to join us in this initiative. We hope that our action inspires the community, including researchers, research funders, and research institutions, to recognize the benefits of published peer review reports for all parts of the research system.</p>
            <p>Learn more at <a href="http://asapbio.org/letter" target="_blank" title="Link opens in new window">ASAPbio</a>.</p>
          </div>
        </div>
      </div>
      <!-- content -->
    </div>

  </section>
  <aside class="article-aside">
  <#include "aside/sidebar.ftl" />
  </aside>
</div>


<#include "../common/footer/footer.ftl" />
<#include "articleJs.ftl" />


<@renderJs />


</body>
</html>
