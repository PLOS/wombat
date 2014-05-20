<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article"
      class="no-js">
<#assign depth = 0 />
<#assign title = '' />
<#include "../common/head.ftl" />
<body class="home">

<#include "../common/header/header.ftl" />

<#include "body.ftl" />
<div class="row">
  <article class="column-3 ">
    <div class="header">Topic:Astrobiology</div>
    <div class="carousel">
			<ul>
				<li class="slides"> <h3>Enable Turn Key Web Readiness</h3>
        <p>Enable Turn Key Web Readiness</p>
					<div class="arrows">
						<span class="previous">previous</span>
						<span class="index">1</span>
						of
						<span class="total">3</span>
						<span class="next">next</span>
					</div></li>
				<li class="slides"> <h3>Enable Turn Key Web Readiness</h3>
					<p>Enable Turn Key Web Readiness</p>
					<div class="arrows">
						<span class="previous">previous</span>
						<span class="index">1</span>
						of
						<span class="total">3</span>
						<span class="next">next</span>
					</div></li>
				<li class="slides"> <h3>Enable Turn Key Web Readiness</h3>
					<p>Enable Turn Key Web Readiness</p>
        </li>
			</ul>

		</div>
  </article>
</div>

 <script type="application/javascript">
	 $('.previous').click(function() {
		 $('.slides').hide();
		 var previous = $(this).closest('.slides').prevAll('.slides').eq(0);
		 if (previous.length === 0) previous = $(this).closest('.slides').nextAll('.slides').last();
		 previous.show();
	 });

	 $('.next').click(function() {
		 $('.slides').hide();
		 var next = $(this).closest('.slides').nextAll('.slides').eq(0);
		 if (next.length === 0) next = $(this).closest('.slides').prevAll('.slides').last();
		 next.show();
	 });
 </script>

<div class="spotlight"></div>

<#include "../common/footer/footer.ftl" />

<script type="text/javascript" src="resource/js/vendor/jquery-1.11.0.js"></script>

</body>
</html>
