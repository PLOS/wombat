/*
 * MetricTile:
 *
 * For Metrics tab we use tiles to display the data, this is the class responsible for display
 * the correct template and append to the main component.
 */
var MetricTile;

(function ($) {
  MetricTile = Class.extend({
    init: function(source){
      this.source = source;
    },
    //Template for tiles that the source has a link
    createWithLink: function () {
      var metricsTileTemplate = _.template($('#metricsTileTemplate').html());
      return  metricsTileTemplate({url: this.url, name: this.name, source_name: this.source.name, imgSrc: this.imageSrc, linkText: this.linkText});
    },
    //Template for tiles that the source has no link
    createWithNoLink: function () {
      var metricsTileTemplate = _.template($('#metricsTileTemplateNoLink').html());
      return  metricsTileTemplate({name: this.name, source_name: this.source.name, imgSrc: this.imageSrc, linkText: this.linkText});
    },
    createTile: function (elementToAppend) {
      this.beforeCreateTile();
      var tileElement = null;

      //Check if the tile has a URL, if it has, we use the link template (createWithLink()), if don't we use the no link one (createWithNoLink())
      if(this.hasUrl) {
        tileElement = this.createWithLink();
      }
      else {
        tileElement = this.createWithNoLink();
      }

      $(elementToAppend).append(tileElement);
      this.afterCreateTile();
    },
    //In some sources the data needs to be treated before pass to the template, this function responsible of treat this data.
    beforeCreateTile: function () {
      switch(this.source.name) {
        case 'twitter':
          this.source.events_url = ALM_CONFIG.hostname + '/works/doi.org/' + ArticleData.doi + "?source_id=twitter";
          break;
        default:
          break;
      }

      this.name = this.source.display_name;
      this.imageSrc = WombatConfig.imgPath + "logo-" + this.source.name + '.png';
      this.linkText = this.source.metrics.total;
      this.hasUrl = false;
      if(_.has(this.source, 'events_url') && !_.isEmpty(this.source.events_url)) {
        this.hasUrl = true;
        this.url = this.source.events_url.replace(/"/g, "%22");
      }
    },

    //In some sources we need to append a tooltip after the tile is appended to the template, this function is responsible for that.
    afterCreateTile: function () {
      var that = this;
      //The tooltip underscore template to compile.
      var tooltipTemplate = false;
      //The data for the tooltip underscore template.
      var tooltipData = false;
      //The ID of the element that should be mouse hovered to show the tooltip.
      var tooltipWrapperElementId = '#' + this.source.name + 'OnArticleMetricsTab';

      //For each source that needs a tooltip we add in the switch case and fill the tooltipTemplate, tooltipData and tooltipElementId.
      switch (this.source.name) {
        case 'facebook':
          tooltipTemplate = _.template($('#metricsTileFacebookTooltipTemplate').html());
          tooltipData = {
            likes: this.source.events[0].like_count,
            shares: this.source.events[0].share_count,
            comments: this.source.events[0].comment_count
          };
          break;
        case 'mendeley':
          tooltipTemplate = _.template($('#metricsTileMendeleyTooltipTemplate').html());
          tooltipData = {
            individuals: this.source.events.reader_count,
            groups: this.source.events.group_count
          };
          break;
        case 'figshare':
          var figshareTooltipTemplate = _.template($('#metricsTileFigshareTooltipTemplate').html());
          var figshareTooltipData = {
            items: {}
          };

          $.ajax({
            url: 'assets/figsAndTables?id=' + ArticleData.doi,
            dataType: 'json',
            error: function (jqXHR, textStatus, errorThrown) {

            },
            success: function (data) {
              if(data && that.source.events) {
                var events = {};
                _.each(that.source.events, function (event) {
                  if(event.doi && _.isString(event.doi)) {
                    var formattedDOI = event.doi.replace('https://dx.doi.org/', '');
                    events[formattedDOI] = event;
                  }
                  else {
                    events["SI"] = event;
                  }
                });

                data = _.map(data, function (item) {
                  var event = events[item.doi];

                  if(event){
                    item.totalStat = event.stats.downloads + event.stats.page_views;
                    item.link = event.figshare_url;
                  }

                  return item;
                });

                figshareTooltipData.items = _.sortBy(data, function (datum) {
                  // Group figures before tables; otherwise, preserve existing order.
                  return (datum.type === 'table') ? 1 : 0;
                });

                if(events["SI"]) {
                  var supportingInfo = {
                    title: 'Supporting Info Files',
                    totalStat: events["SI"].stats.downloads + events["SI"].stats.page_views,
                    link: events["SI"].figshare_url
                  };
                  figshareTooltipData.items.push(supportingInfo);
                }

                $('#figshareImageOnArticleMetricsTab')
                  .attr('data-options', 'align:right')
                  .attr('data-dropdown', 'dropdown-figshare')
                  .after(figshareTooltipTemplate(figshareTooltipData));
                var showDropdown = $('#article-metrics').attr('data-showTooltip');

                if (showDropdown) {
                  $(document).foundation('dropdown', 'reflow');
                }
              }
            }
          });
          break;
        default:
          break;
      }

      //If we have a template and data we append the element to the 'tooltipWrapperElementId' and initialize the tooltip plugin.
      if(tooltipTemplate && tooltipData) {
        $(tooltipWrapperElementId)
          .attr("data-js-tooltip-hover", "trigger")
          .append(tooltipTemplate(tooltipData));
        tooltip_hover.init();
      }
    }
  });
})(jQuery);