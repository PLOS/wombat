<html>
<#assign title = article.title, articleDoi = article.doi />
<#assign depth = 0 />
<#assign cssFile = 'site-content.css'/>

<#include "../common/head.ftl" />
<#include "../common/journalStyle.ftl" />

<body class="${journalStyle}">

<#include "../common/header/headerContainer.ftl" />
<div class="set-grid">
    <article>

        <h1>Thank You</h1>

        <p><strong>Your e-mail has been sent!</strong></p>

        <p class="source">
            <span>Back to article:</span>
            <br/>
            <strong>
                <a href="<@siteLink handlerName="article" queryParameters={'id': article.doi} />"
                   title="Back to original article">
                ${article.title}
                </a>
            </strong>
        </p>
    </article>
</div>

<#include "../common/footer/footer.ftl" />

<@renderJs />

</body>
</html>
