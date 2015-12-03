/**
 * Created by pgrinbaum on 7/17/14.
 */

$( document ).ready(function() {
 //carousel

  $.ajax({
    url: 'http://blogs-stage.plos.org/biologue/?feed=json',
    data: {
      format: 'json'
    },
    error: function() {
      $('main').html('<p>An error has occurred</p>');
    },
    dataType: 'json',
    success: function(data) {
      var $title = $('p').text(data[0].title);
      $('#test')
          .append($title);
    },
    type: 'GET'
  });

  carousel.init();
 //tooltip - requires dotdotdot
  $(".truncated-tooltip").dotdotdot({
    height: 45
  });
});


