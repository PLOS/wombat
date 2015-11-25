(function ($) {

  /// floating nav

  $('nav.nav-toc').floatingNav({
    content: $('section'),
    link_selector: 'a',
    section_anchor: '.section > a',
    section_anchor_attr: 'id',
    margin: 10
  });



  // initialize tooltip_hover for everything

})(jQuery);