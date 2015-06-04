<#include "../macro/removeTags.ftl" />
<#include "../common/title/titleFormat.ftl" />

<#macro pageCSS>

  <#if !cssFile?has_content>
    <#assign cssFile="screen.css"/>
  </#if>

  <@cssLink target="resource/css/${cssFile}"/>

</#macro>


<head prefix="og: http://ogp.me/ns#">
    <title><@titleFormat removeTags(title) /></title>

<@pageCSS/>

<@renderCssLinks />

    <meta name="asset-url-prefix" content="<@siteLink path='indirect/'/>"/>
    <!-- LEMUR CSS -->
<#list hostedData.css_sources as css_source>
    <link rel="stylesheet" type="text/css"
          href="<@siteLink path='indirect/'/>${css_source}"/>
</#list>

    <meta name="root-url" content="/wombat/DesktopPlosBiology/h/ember-app/">

    <!--[if IE 8]>
  <link rel="stylesheet" type="text/css" href="<@siteLink path="resource" />
    <![endif]-->

    <link media="print" rel="stylesheet" type="text/css" href="<@siteLink path="resource/css/print.css"/>"/>
<#-- hack to put make global vars accessible in javascript. Would be good to come up with a better solution -->
    <script type="text/javascript">
        var siteUrlPrefix = "<@siteLink path=''/>";
    </script>

    <script type="text/javascript" src="<@siteLink path="resource/js/vendor/modernizr-v2.7.1.js"/>"></script>
<#-- //html5shiv. js and respond.js - enable the use of foundation's dropdowns to work in IE8 -->
<#-- //The rem  polyfill rewrites the rems in to pixels. I don't think we can call this using the asset manager. -->
    <!--[if IE 8]>
  <script src="<@siteLink path="resource                       "/>  </script>
  <
  script
      src= <@siteLink path="resource / js / vendor / respond.min.js"/> ></script>
      <script src="<@siteLink path="resource                     "/>  </script>
    <![endif]-->
    <link rel="shortcut icon" href="<@siteLink path="resource/img/favicon.ico"/>" type="image/x-icon"/>

    <meta name="ameliorate/config/environment"
          content="%7B%22modulePrefix%22%3A%22ameliorate%22%2C%22environment%22%3A%22development%22%2C%22baseURL%22%3A%22/%22%2C%22locationType%22%3A%22auto%22%2C%22EmberENV%22%3A%7B%22FEATURES%22%3A%7B%7D%7D%2C%22APP%22%3A%7B%22name%22%3A%22ameliorate%22%2C%22version%22%3A%220.0.0.0eb49f80%22%7D%2C%22contentSecurityPolicyHeader%22%3A%22Content-Security-Policy-Report-Only%22%2C%22contentSecurityPolicy%22%3A%7B%22default-src%22%3A%22%27none%27%22%2C%22script-src%22%3A%22%27self%27%20%27unsafe-eval%27%22%2C%22font-src%22%3A%22%27self%27%22%2C%22connect-src%22%3A%22%27self%27%22%2C%22img-src%22%3A%22%27self%27%22%2C%22style-src%22%3A%22%27self%27%22%2C%22media-src%22%3A%22%27self%27%22%7D%2C%22exportApplicationGlobal%22%3Atrue%7D"/>

<#-- // TODO: NEED BACKEND
  <meta name="description" content="${freemarker_config.getMetaDescription(journalContext)}"/>
  <meta name="keywords" content="${freemarker_config.getMetaKeywords(journalContext)}"/>-->

<#if article??>
<#-- // citation meta tags -->
  <#include "../article/metaTags.ftl" />

</#if>
<#--<#include "../common/analytics.ftl" />-->

</head>
<#-- //references js that is foundational like jquery and foundation.js. JS output is printed at the bottom of the body.
-->
<#include "../common/baseJs.ftl" />

