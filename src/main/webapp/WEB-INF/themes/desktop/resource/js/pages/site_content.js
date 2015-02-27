(function ($) {

    /// floating nav

    $('nav.site-content-nav').floatingNav({
        content: $('.body-content'),
        linkSelector: 'a',
        sectionAnchor: 'h2, h3',
        sectionAnchorAttr: 'id'
    });


})(jQuery);