(function ($) {
var tooltip_references, initTooltipReference;

  function getMediaCoverageCount(almData) {
    for (var i = 0; i < almData.sources.length; i++) {
      if (almData.sources[i].name == 'articlecoveragecurated') {
        return almData.sources[i].metrics.total;
      }
    }
    return 0;
  }

  function addMediaCoverageLink() {
    var $media = $('#nav-media');
    if ($media.length) {
      var doi = $media.data('doi');
      $media.getArticleSummary(doi,
          function (data) {
            var mediaCoverageCount = getMediaCoverageCount(data);
            $media.find('#media-coverage-count').text('(' + mediaCoverageCount + ')');
          });
    }
  }

  /// write article nav
  $article = $('.article-content');

  $('#nav-article').buildNav({
    content: $article
  });

  $('#nav-article').floatingNav({
    sections: $article.find('div.toc-section')
  });

  addMediaCoverageLink();
  figshareWidget();

  var hasFigures = $('#artText .figure').length;

  if(hasFigures){

  $('#figure-carousel-section').buildFigureCarousel({});

  };

  function formatHumanReadableByteSize(bytes) {
    // Space before "Bytes". All others are concatenated to number with no space.
    var units = [' Bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];

    var increment = 1000; // could change to 1024
    var roundingThreshold = units.indexOf('MB'); // for smaller units than this, show integers
    var precision = 100; // round to this precision if unit is >= roundingThreshold

    var n = bytes;
    var unitIndex = 0;
    for (; unitIndex < units.length; unitIndex++) {
      if (n >= increment) {
        n /= increment;
      } else break;
    }

    n = (unitIndex < roundingThreshold) ? Math.round(n) : Math.round(n * precision) / precision;

    return n + units[unitIndex];
  }

  // Will be invoked directly from article HTML, where the templating engine will inject the fileSizeTable data.
  $.fn.populateFileSizes = function (fileSizeTable) {

    if(hasFigures){
    $('.file-size').each(function (index, element) {
      var $el = $(element);
      var doi = $el.attr('data-doi');
      var fileSize = $el.attr('data-size');
      var size = fileSizeTable[ doi][ /**/fileSize];
      $el.text('(' + formatHumanReadableByteSize(size) + ')');
    });
    }
  };

  $('.table-download').on('click', function(){
    var table = $(this).parent(),
    figId = $(this).data('tableopen');
    return tableOpen(figId, "CSV", table);
  });

  $('.table-wrap .expand').on('click', function(){
    var table = $(this).parent(),
    figId = $(this).data('tableopen');
    return tableOpen(figId, "HTML", table);
  });

// tooltips for the links to references in the article body eg '[4]'
  tooltip_references = function () {

  $('.ref-tip').hover(
    function () {
      var $ref_link = $(this);  // hovered over link
      var $position = $ref_link.position();
      var $link_width = $ref_link.width();
      var $ref_number = $ref_link.text().slice(1, -1);  //drop the brackets [4]
      var ref_label = '#ref' + $ref_number;  // form the id in the references li.s
      var $matching_ref = $(ref_label).html(); //get the reference content
      var $ref_tooltip = $('.ref-tooltip');  //find the tooltip div
      var $ref_content = $ref_tooltip.find('.ref_tooltip-content').html($matching_ref); //add the references content
      var $ref_link_top = $position.top;
      var $ref_link_left = $position.left;
      var $tooltip_height = $ref_tooltip.height();
      var $tooltip_width = $ref_tooltip.width();

      $ref_link_top = ($ref_link_top - $tooltip_height)-20;//top of ref link - height of the tooltip - 20px margin
      //left position of ref link - tooltip width - link width/2 so can center over link:
      $ref_link_left = $ref_link_left - ($tooltip_width - $link_width)/2;

      $ref_tooltip.css({ //place the tooltip 20px above & centered over the ref link
        'top': $ref_link_top,
        'left': $ref_link_left}).html($ref_content).fadeIn('fast');
    },
    function () {
      $('.ref-tooltip').fadeOut('fast');
    });
  };
  initTooltipReference = tooltip_references();

})(jQuery);
