<!-- for LEMUR: block begins here: -->
<div id="home-content">
	<div class="row hero">
  <#include "blocks/hero.ftl" />
    <#include "blocks/billboard.ftl" />
	</div>
	<div class="row one">
  <#include "blocks/editorialBlockMedium.ftl" />
    <#include "blocks/articleList.ftl" />
    <#include "blocks/currentIssue.ftl" />
	</div>

	<div class="row two">
  <#include "blocks/editorialBlockMedium.ftl" />
    <#include "blocks/editorialBlockMedium.ftl" />
	</div>

	<div class="row three">

		<div class="column left">
    <#include "blocks/editorialBlockLarge.ftl" />
     <#include "blocks/editorialBlockLarge.ftl" />
     <#include "blocks/editorialBlockSmall.ftl" />
     <#include "blocks/editorialBlockSmall.ftl" />
     <#include "blocks/editorialBlockSmall.ftl" />
     <#include "blocks/editorialBlockSmall.ftl" />
		</div>
		<div class="column right">
    <#include "blocks/submissionLinks.ftl" />
      <#include "blocks/socialLinks.ftl" />
      <#include "blocks/twitter.ftl" />
      <#include "blocks/blogs.ftl" />
		</div>

	</div>

</div>
<!-- for LEMUR: block ends here -->