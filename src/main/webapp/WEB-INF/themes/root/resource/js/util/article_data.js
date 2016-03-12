var ArticleData = {};

(function ($) {
  ArticleData = {
    doi:  $('meta[name=citation_doi]').attr('content'),
    date: $('meta[name=citation_date]').attr('content'),
    title: $('meta[name=citation_title]').attr('content'),
    description: $('meta[name=description]').attr('content'),
    keywords: $('meta[name=keywords]').attr('content'),
    journalTitle: $('meta[name=citation_journal_title]').attr('content'),
    firstpage: $('meta[name=citation_firstpage]').attr('content'),
    issue: $('meta[name=citation_issue]').attr('content'),
    volume: $('meta[name=citation_volume]').attr('content'),
    issn: $('meta[name=citation_issn]').attr('content'),
    journalAbbrev: $('meta[name=citation_journal_abbrev]').attr('content'),
    publisher: $('meta[name=citation_publisher]').attr('content'),
    pdfUrl: $('meta[name=citation_pdf_url]').attr('content')
  };

  ArticleData.authors = function () {
    var authorsList = $('meta[name=citation_author]');
    var authorsInstitutionsList = $('meta[name=citation_author_institution]');

    return _.map(authorsList, function (value, key) {
      return { name: $(value).attr('content'), institution: $(authorsInstitutionsList).attr('content') };
    });
  };

})(jQuery);