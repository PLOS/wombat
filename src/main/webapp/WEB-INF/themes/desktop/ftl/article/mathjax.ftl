<!-- This file should be loaded before the renderJs, to avoid conflicts with the FigShare, that implements the MathJax also. -->

<!--  mathjax configuration options  -->
<!-- more can be found at http://docs.mathjax.org/en/latest/ -->
<script type="text/x-mathjax-config">
MathJax.Hub.Config({
  "HTML-CSS": {
    scale: 100,
    availableFonts: ["STIX","TeX"],
    preferredFont: "STIX",
    webFont: "STIX-Web",
    linebreaks: { automatic: false }
  },
  jax: ["input/MathML", "output/HTML-CSS"]
});
</script>

<script type="text/javascript" src="https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=MML_HTMLorMML"></script>
