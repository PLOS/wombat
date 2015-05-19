(function ($) {

    /// floating nav

    $('nav.site-content-nav').floatingNav({
        content: $('.lemur-content '),
        link_selector: 'a',
        section_anchor: 'a[name]',
        section_anchor_attr: 'id',
        margin: 10,
        ignored_sections: 'H4'
    });


  // initialize tooltip_hover for everything

})(jQuery);