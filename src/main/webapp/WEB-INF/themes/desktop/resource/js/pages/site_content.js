(function ($) {

    /// floating nav

    $('nav.site-content-nav').floatingNav({
        content: $('.body-content'),
        link_selector: 'a',
        section_anchor: 'h2, h3',
        section_anchor_attr: 'id'
    });


})(jQuery);