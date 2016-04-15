
<@js src="resource/js/vendor/display-libraries/bio.pv.min.js"/>
<style>

  #gl {
    position:fixed;
    bottom:0px;
    top:0px;
    left:0px;
    right:0px;
  }
  #inspector {
    top:10px;
    left:10px;
    box-shadow: 2px 2px 5px #888888;
    border-radius:8px;
    position:absolute;
    background-color:#fafafa;
    padding:10px;
    border-style:solid;
    border-width:1px;
    border-color:#ccc;
  }

  #inspector ul {
    padding:0px;
  }

  #inspector ul li {
    margin-left:5px;
    margin-right:5px;
    margin-bottom:5px;
    list-style:none;
    cursor: pointer;
    color:#393
  }

  #inspector ul li:hover {
    color:#994444;
  }
  #inspector h1 {
    font-weight:normal;
    font-size:12pt;
  }

  #figure-lightbox-container-interactive{
    display: block;
    width: 800px;
    height: 800px;
    background: darkgrey none repeat scroll 0% 0%;
    padding: 0.1em;
    position: fixed;
    top: 3em;
    left: 25%;
    z-index: 6000;
    color: #fff
  }
</style>

<div id="figure-lightbox-container-interactive">

  <div id="viewer">
  </div>
  <div id=inspector>
    <h1>Choose Style</h1>
    <ul>
      <li id=preset>Preset</li>
      <li id=cartoon>Cartoon</li>
      <li id=tube>Tube</li>
      <li id=lines>Lines</li>
      <li id=line-trace>Line Trace</li>
      <li id=sline>Smooth Line Trace</li>
      <li id=trace>Trace</li>
    </ul>

    <span><a href='index.html'>About</a> | Code on <a href="http://github.com/biasmv/pv">github.com</a></span>
  </div>
</div>