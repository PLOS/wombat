<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
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

           <h2>Peer Review History</h2>
       <table class="table table-bordered review-history">
      <tbody>
        <tr>
          <th>Original Submission  <div class="date"><span class="decision-date">September 29, 2018</span></div></th>
        </tr>
         
        <tr class="decision-letter">
          <td>
            <!-- trigger for expand and collapse -->
            <a data-toggle="collapse" 
              href="#collapseExample" 
              role="button" 
              aria-expanded="false" 
              aria-controls="collapseExample">
              Decision Letter
            </a> 
            <!-- end trigger for expand and collapse -->
            - Lauren Bianchini, Editor 
            <div class="date">
              <span class="decision-date">October 1, 2018</span>
            </div>
            
            <!-- accordion container -->
            <div class="collapse" id="collapseExample">
                <p>[decisionLetter.contents]</p>
            
                <dl class="version-attachments">
                  <dt>Attachments</dt>
                  <dd><a href="#" target="_blank" title="Download .docx file">Attachment 1</a>  (532KB)
                  <br>Submitted filename: <i>This is my file name.docx</i>
                  </dd>
                  <dd><a href="#" target="_blank" title="Download .docx file">Attachment 2</a>  (946 KB)<br>Submitted filename: <i>Shall I compare thee to a summers day.docx</i></dd>
                  <dd><a href="#" target="_blank" title="Download .docx file">Attachment 3</a>  (772 KB)<br>Submitted filename: <i>So much depends upon a red wheelbarrow.docx</i></dd>
                  <dd><a href="#" target="_blank" title="Download .csv file">Attachment 4</a> (1.1 MB)<br>Submitted filename: <i>One Data File 2018-09-27T11_53_36-0700.csv</i></dd>
                </dl>
            </div>
            <!-- end accordion container -->
            
          </td>
        </tr>
          <tr>
          <th>Resubmission - Version 2 </th>
        </tr>
     <tr class="author-response">
          <td>
            <a data-toggle="collapse" href="#collapseExample5" role="button" aria-expanded="false" aria-controls="collapseExample5">Author Response </a> 
            <div class="date">
              <span class="decision-date">October 23, 2018</span>
            </div>
          
          </td>
        </tr>
    
       <tr class="decision-letter">
          <td>
                  <!-- trigger for expand and collapse -->
            <a data-toggle="collapse" 
              href="#collapseExample" 
              role="button" 
              aria-expanded="false" 
              aria-controls="collapseExample">
              Decision Letter
            </a> 
            <!-- end trigger for expand and collapse -->
            - Katie Hickling, Editor 
            <div class="date">
              <span class="decision-date">November 1, 2018</span>
            </div>
            
            <!-- accordion container -->
            <div class="collapse" id="collapseExample">
                <p>[decisionLetter.contents]</p>
            </div>
            <!-- end accordion container -->
          </td>
        </tr> <tr>
          <th>Resubmission - Version 3  </th>
        </tr>
        <tr class="author-response">
          <td>
            <a data-toggle="collapse" href="#collapseExample5" role="button" aria-expanded="false" aria-controls="collapseExample5">Author Response </a> 
            <div class="date">
              <span class="decision-date">December 3, 2018</span>
            </div>
          
          </td>
        </tr>
          <tr class="decision-letter">
          <td>
            <a data-toggle="collapse" href="#collapseExample3" role="button" aria-expanded="false" aria-controls="collapseExample3">
            Decision Letter 
            </a> - Sotirios Koutsopoulos, Editor
            <div class="date"><span class="decision-date">January 02, 2018</span></div>
            <div class="collapse" id="collapseExample3">
              
            </div>
          </td>
        </tr>
       <tr>
          <th>Accepted </th>
        </tr>
     
      </tbody>
    </table>          <div class="tpr-info">
            <h3>Open letter on the publication of peer review reports</h3>
            <p>PLOS recognises the benefits of transparency in the peer review process. Therefore, we enable the publication of all of the content of peer review and author responses alongside final, published articles. Reviewers remain anonymous, unless they choose to reveal their names.</p>

            <p>We encourage other journals to join us in this initiative. We hope that our action inspires the community, including researchers, research funders, and research institutions, to recognize the benefits of published peer review reports for all parts of the research system.</p>
            <p>Learn more at <a href="http://asapbio.org/letter" target="_blank" title="Link opens in new window">ASAPbio</a>.</p>
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
