/**
 * Created by pgrinbaum on 5/21/14.
 * using jcarousel
 */

$(function () {

  var current_locator = '[data-js="carousel-current-item"]';
  var index_locator = 'span[data-js="carousel-total-index"]';

  $('.jcarousel')
    // get index
    .on('jcarousel:create',function (event, carousel) {
      var total_index = $(this).jcarousel('items').size();
      $(this).next('.carousel-control').find(index_locator).html(total_index);
      $(this).next('.carousel-control').find(current_locator).html("1");
    }).
    // change number
    on('jcarousel:animateend',function (event, carousel) {
      var current_item = $(this).jcarousel('visible').index();
      var current_item_readable = (current_item + 1);
      $(this).next('.carousel-control').find(current_locator).html(current_item_readable);
    })
    /// initialise jcar
    .jcarousel({wrap:'both'});

  $('.jcarousel-prev').jcarouselControl({
    target: '-=1'
  });

  $('.jcarousel-next').jcarouselControl({
    target: '+=1'
  });
});
