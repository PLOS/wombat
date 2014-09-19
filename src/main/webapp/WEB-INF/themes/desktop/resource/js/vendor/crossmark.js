var crossmark = {
    sOpenSelector: "#open-crossmark",
    sDOI: "",
    sDomain: "",
    sURIScheme: "",
    sCrossMarkURL: "",
    sStylesURL: "",
    sTooltipID: "crossmark-tooltip-130",
    sTooltipSelector: "#crossmark-tooltip-130",
    sTooltipCopy: "Click to get updates and verify authenticity.",
    initialize: function() {
      this.sDOI = this.detectDOI();
      this.sDomain = window.location.hostname;
      this.sURIScheme = window.location.protocol;
      this.sCrossMarkURL = "crossmark.crossref.org/dialog/?doi=" + this.sDOI + "&domain=" + this.sDomain + "&uri_scheme=" + this.sURIScheme + "&cm_version=" +
        this.scriptVersion();
      this.sStylesURL = "http://crossmark.crossref.org/stylesheets/" + this.versionDir() + "crossmark_widget.css"
    console.log(this.sDOI);
    },
    addStylesheet: function() {
      $cmjq(this.sOpenSelector).parent().eq(0).prepend("<link media='screen' rel='stylesheet' type='text/css' href='" + this.sStylesURL + "'/>");
    },
    activateTooltip: function() {
      var a = this;
      $cmjq("body").append('<div id="' + this.sTooltipID + '" class="crossmark-tooltip" style="display: none;"><div class="cmtttop"></div><div class="cmttmid"><p>' + this.sTooltipCopy + '</p></div><div class="cmttbot"></div></div>');
      var b = $cmjq("#crossmark-icon");
      b.attr({
        title: "",
        alt: ""
      }).show();
      b.mouseover(function() {
        var c = b.offset(),
          d = c.left + b.width() / 2 - $cmjq("#crossmark-icon").width() / 2,
          c = c.top - $cmjq(a.sTooltipSelector).height() + 10;
        $cmjq(a.sTooltipSelector).css({
          left: d,
          top: c
        }).show();
      });
      b.mouseout(function() {
        $cmjq(a.sTooltipSelector).hide();
      });
    },
    scriptVersion: function() {
      var a = ($cmjq('script[src$="crossmark.js"]')[0] || $cmjq('script[src$="crossmark.min.js"]')[0]).src.split("/");
      return "javascripts" == a[a.length - 2] ? "v1.2" : a[a.length -
        2]
    },
    versionDir: function() {
      var a = this.scriptVersion();
      return "v1.2" == a ? "" : a + "/"
    },
    activateDialog: function() {
      var a = this;
      $cmjq(this.sOpenSelector).click(function() {
        $cmjq("#crossmark-dialog-frame").attr("src", "http://" + a.sCrossMarkURL);
        $cmjq("#crossmark-dialog").dialog("open");
        $cmjq(a.sTooltipSelector).hide();
        this.preventDefault;
        return !1
      })
    },
    detectDOI: function() {
      this.sDOI = "";
      var a = $cmjq("meta").filter(function() {
        return /dc\.identifier/i.test($cmjq(this).attr("name"))
      }).attr("content");
      a && (a = a.replace(/^info:doi\//, ""), a = a.replace(/^doi:/,
        ""));
      return a
    }
  },
  $cmjq = jQuery.noConflict();
jQuery(function(a) {
  $cmjq("#crossmark-dialog").dialog({
    zIndex: 3999,
    autoOpen: !1,
    modal: !0,
    resizable: !1,
    draggable: !1,
    open: function() {
      $cmjq(".ui-widget-overlay").click(function() {
        $cmjq("#crossmark-dialog").dialog("close");
      });
    },
    beforeClose: function() {
      $cmjq(".ui-widget-overlay").unbind();
    },
    height: 550,
    width: 550,
    dialogClass: "crossmark-ui-dialog"
  });
  crossmark.initialize();
  crossmark.addStylesheet();
  crossmark.activateTooltip();
  crossmark.activateDialog();
});