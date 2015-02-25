(function ($) {

    /// floating nav

    $body = $('.body-content');

    $('nav.site-content').floatingNav({
        parentContainer: '.body-content',
        sections: $body.find('div[data-section-id]'),
        linkSelector: 'a',
        sectionAnchor: 'h2, h3',
        sectionAnchorAttr: 'id'
    });


})(jQuery);