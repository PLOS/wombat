(function ($) {
  /// floating nav

  $('nav#nav-toc').floatingNav({
    content: $('section'),
    link_selector: 'a',
    section_anchor: 'article .section a[name]',
    section_anchor_attr: 'id',
    margin: 10
  });

})(jQuery);