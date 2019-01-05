<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      lang="en" xml:lang="en"
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

      <h2>Peer Review REAL DATA</h2>
      ${peerReview?web_safe}
      <hr/>
      <h2>Prototype with fake data below</h2>
      <hr/>
           <h2>Peer Review History</h2>
       <table class="table table-bordered review-history">
      <tbody>
        <tr>
          <th>Original Submission  
            <div class="date">
              <span class="decision-date">September 29, 2018</span>
              </div>
            </th>
        </tr>
         
        <tr>
          <td>
            
             <div class="decision-letter" itemscope 
               itemtype="http://schema.org/Review">
             <div itemprop="itemReviewed" itemscope
               itemtype="http://schema.org/ScholarlyArticle">
                 
                <@siteLink handlerName="article" absoluteLink=true queryParameters={"id" : article.doi} ; articleUrl>
                <meta itemprop="url" content="${articleUrl}" />
                </@siteLink>
              
            </div>
    
            <!-- trigger for expand and collapse -->
            <a data-toggle="collapse" 
              href="#decisionLetter" 
              role="button" 
              aria-expanded="false" 
              aria-controls="decisionLetter">
              Decision Letter
            </a> 
            <!-- end trigger for expand and collapse -->
            - <span itemprop="author" itemscope 
              itemtype="http://schema.org/Person">
                <span itemprop="name">
                  Lauren Bianchini, Editor 
                </span>
              </span>
            <div class="date">
              <time class="decision-date" 
                itemprop="dateCreated"
                datetime="2018-10-01">
                October 1, 2018
              </time>
            </div>
            
            <!-- accordion container -->
             <div itemprop="reviewBody" class="collapse" id="decisionLetter">
               
               <!-- decision letter text -->
                <p>[decisionLetter.contents]</p>
            
                <!-- reviewer attachments -->
                <dl class="review-files">
                  <dt>Attachments</dt>
                  <dd><a itemprop="url" href="#" target="_blank" title="Download .docx file">Attachment 1</a>  (532KB)
                  <br>Submitted filename: <i>This is my file name.docx</i>
                  </dd>
                  <dd><a itemprop="url" href="#" target="_blank" title="Download .docx file">Attachment 2</a>  (946 KB)<br>Submitted filename: <i>Shall I compare thee to a summers day.docx</i></dd>
                  <dd><a itemprop="url" href="#" target="_blank" title="Download .docx file">Attachment 3</a>  (772 KB)<br>Submitted filename: <i>So much depends upon a red wheelbarrow.docx</i></dd>
                  <dd><a itemprop="url" href="#" target="_blank" title="Download .csv file">Attachment 4</a> (1.1 MB)<br>Submitted filename: <i>One Data File 2018-09-27T11_53_36-0700.csv</i></dd>
                </dl>
            </div>
            <!-- end accordion container -->
            
             </div>
          </td>
        </tr>
          <tr>
          <th>Resubmission - Version 2 </th>
        </tr>
     <tr>
          <td>
            <!-- author response -->
            <a data-toggle="collapse" 
              href="#decisionLetter2" 
              role="button" 
              aria-expanded="false" 
              aria-controls="decisionLetter2">
                Author Response 
              </a> 
              <div class="date">
                <time class="response-date">
                  October 23, 2018
                </time>
              </div>
          
          </td>
        </tr>
    
       <tr>
          <td>
                     <div class="decision-letter" itemscope 
               itemtype="http://schema.org/Review">
             <div itemprop="itemReviewed" itemscope
               itemtype="http://schema.org/ScholarlyArticle">
                 
                <@siteLink handlerName="article" absoluteLink=true queryParameters={"id" : article.doi} ; articleUrl>
                <meta itemprop="url" content="${articleUrl}" />
                </@siteLink>
              
            </div>
                  <!-- trigger for expand and collapse -->
            <a data-toggle="collapse" 
              href="#decisionLetter3" 
              role="button" 
              aria-expanded="false" 
              aria-controls="decisionLetter3">
              Decision Letter
            </a> 
            <!-- end trigger for expand and collapse -->
            - <span itemprop="author" itemscope 
              itemtype="http://schema.org/Person">
                <span itemprop="name">
                Katie Hickling
                </span>
                </span>, Editor 
              <div class="date">
              <time class="decision-date" 
                itemprop="dateCreated"
                datetime="2018-11-01">
                November 1, 2018
              </time>
            </div>
            
            <!-- accordion container -->
            <div itemprop="reviewBody" class="collapse" id="decisionLetter3">
                <p>[decisionLetter.contents]</p>
            </div>
            <!-- end accordion container -->
                     </div>
          </td>
        </tr> 
        <tr>
          <th>Resubmission - Version 3  </th>
        </tr>
        <tr>
                    <td>
            <!-- author response -->
            <a data-toggle="collapse" 
              href="#decisionLetter4" 
              role="button" 
              aria-expanded="false" 
              aria-controls="decisionLetter4">
                Author Response 
              </a> 
              <div class="date">
                <time class="response-date">
                  December 3, 2018
                </time>
              </div>
          
          </td>
        </tr>
          <tr>
          <td>         <div class="decision-letter" itemscope 
               itemtype="http://schema.org/Review">
             <div itemprop="itemReviewed" itemscope
               itemtype="http://schema.org/ScholarlyArticle">
                 
                <@siteLink handlerName="article" absoluteLink=true queryParameters={"id" : article.doi} ; articleUrl>
                <meta itemprop="url" content="${articleUrl}" />
                </@siteLink>
              
            </div>
                  <!-- trigger for expand and collapse -->
            <a data-toggle="collapse" 
              href="#decisionLetter3" 
              role="button" 
              aria-expanded="false" 
              aria-controls="decisionLetter3">
              Decision Letter
            </a> 
            <!-- end trigger for expand and collapse -->
            - <span itemprop="author" itemscope 
              itemtype="http://schema.org/Person">
                <span itemprop="name">
                Katie Hickling
                </span>
                </span>, Editor 
              <div class="date">
              <time class="decision-date" 
                itemprop="dateCreated"
                datetime="2019-01-01">
                January 1, 2019
              </time>
            </div>
            
            <!-- accordion container -->
            <div itemprop="reviewBody" class="collapse" id="decisionLetter3">
                <p>[decisionLetter.contents]</p>
            </div>
            <!-- end accordion container -->
          </div>
          </td>
        </tr>
       <tr>
          <th>Accepted </th>
        </tr>
     
      </tbody>
    </table>          
    <div class="tpr-info">
            <h3>Open letter on the publication of peer review reports</h3>
            <p>PLOS recognizes the benefits of transparency in the peer review process. Therefore, we enable the publication of all of the content of peer review and author responses alongside final, published articles. Reviewers remain anonymous, unless they choose to reveal their names.</p>

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
