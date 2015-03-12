(function ($) {

    /// floating nav

    $('nav.site-content-nav').floatingNav({
        content: $('.lemur-content '),
        link_selector: 'a',
        section_anchor: 'a[name]',
        section_anchor_attr: 'id'
    });


})(jQuery);