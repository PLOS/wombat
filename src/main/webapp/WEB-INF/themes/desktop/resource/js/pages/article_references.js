
(function ($) {


$('.references li').one('click','ul.reflinks li:first-child a',
    function (event) {
      event.preventDefault();
      console.log('blah');
      var $that = $(this);
      var queryStringAuthor = $(this).attr('data-author');
      var queryStringTitle= $(this).attr('data-title');
      var queryStringCit= $(this).attr('data-citation');

      var queryStringConcat = 'query.author=' + queryStringAuthor + '&query.title=' + queryStringTitle;
      var DOIResolver = 'http://dx.doi.org/';

      var url = "http://api.crossref.org/works?" + queryStringConcat + "&sort=score&rows=1";
      var crossrefSearchString =  'http://search.crossref.org/?q=' + queryStringCit;

      $.ajax({
            url: url,
          })
          .success(
              function( data ) {
                var DOIs = data.message.items[0].DOI;
                var titleAPI = data.message.items[0].title;
                var titleXML = queryStringTitle.replace( /%20/g, ' ');
                var TitleXML2 = s.trim(titleXML,'.');


                alert(titleXML);

                $that.attr('href', DOIResolver +  DOIs);

                console.log("titleAPI =" + titleAPI);
                console.log("titleXML =" + TitleXML2);

                if (titleAPI === TitleXML2){
                  console.log('title matches: true');
                } else{
                  console.log('title matches: false');

                }

                console.log( "Sample of datas3:", DOIs);
                console.log($that.attr('href'));
                console.log(url);
                window.open(DOIResolver + DOIs, '_new');


              }
          )
          .error(
              function () {
                window.open(crossrefSearchString);

              }
          )
          .done(function( status  ) {
            console.log(status)
          });

    }
);


/////////////
//var referenceValidation
//  referenceValidation = Class.extend({
//    queryString: null,
//  crossref_api:  "http://api.crossref.org/works?query=" + queryString + "&sort=score&rows=1",
//    DOIResolver: 'http://dx.doi.org/',
//
//
//  init: function () {
//    var that = this;
//
//
//
//  },
//    getJSON: function () {
//      var that = this;
//      $.ajax({
//            url: "http://api.crossref.org/works?query=" + queryString + "&sort=score&rows=1",
//          })
//          .success(
//              function( data ) {
//                var DOIs = data.message.items[0].DOI;
//                var DOIResolver = 'http://dx.doi.org/';
//                $that.attr('href', DOIResolver +  DOIs);
//                console.log( "Sample of datas3:", DOIs);
//                console.log($that.attr('href'));
//                console.log("http://api.crossref.org/works?" + queryString + "&sort=score&rows=1");
//                window.open(DOIResolver + DOIs, 'blah');
//                console.log(data);
//              }
//
//          )
//          .done(function( url  ) {
//            console.log('log' + url);
//            console.log("this plus " + $that.attr('href'));
//          });
//      return
//    }
//
//
//
//  })(jQuery);
//
//  new referenceValidation();


})(jQuery);


