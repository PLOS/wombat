(function ($) {
  var cut_tweet = (function() {
    var get_text, parts, after_text, count_text, cutit, mod_cut, newhref;
    get_text = $('#twitter-share-link').attr('href');
    //split after '&' in '&text='
    parts = get_text.split('&');
    after_text = parts[1].substring(5);
    count_text = after_text.length;

    if (count_text > 115) {
      cutit = '&text=' + after_text.substring(0, 115) + ' ...';
      //replace semicolons
      mod_cut = cutit.replace(/;/g,'%3B');
      newhref = parts[0] + mod_cut;
      return $('#twitter-share-link').attr('href', newhref);
    }
  })();

})(jQuery);