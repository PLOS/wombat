(function ($) {
  /// floating nav

  $('nav#nav-toc').floatingNav({
    content: $('article'),
    link_selector: 'a',
    section_anchor: '.section a[name]',
    section_anchor_attr: 'id',
    margin: 80
  });

})(jQuery);