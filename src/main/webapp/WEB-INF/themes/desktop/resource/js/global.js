
//var s;

//Setting up global foundation settings - you can override these the documentation is here:
// http://foundation.zurb.com/docs/javascript.html#configure-on-the-fly
$(document).foundation({

  //Tooltips
  tooltip: {
    'wrap': 'word',
    // 'disable_for_touch': 'true',
    tip_template: function (selector, content) {
      return '<span data-selector="' + selector + '" class="'
        + Foundation.libs.tooltip.settings.tooltip_class.substring(1)
        + '">' + content + '</span>';
    }
  },
  // reveal is used for the figure viewer modal
  reveal: {
    animation: false
  }

});
(function ($) {

  var searchCheck = function (){

    if (!Modernizr.input.required) {
      $("form[name='searchForm']").submit(function() {
        var searchTerm = $("#search").val();
        if (!searchTerm) return false;
      });
    }

  };

  $(document).ready(function () {
    var runSearchCheck = searchCheck();
    // hover delay for menu
    hover_delay.init();
    //placeholder style change
    placeholder_style.init();
  });

})(jQuery);