var PeerReview = function () {
  var animation_time = '500';
  self.togglePeerReviewAccordion = function (expandLink) {
    var accordionItem = expandLink.parents(".peer-review-accordion-item");
    var accordionContent = accordionItem.find('.peer-review-accordion-content');
    if (accordionItem.hasClass('expanded')) {
          accordionItem.removeClass('expanded');
          accordionContent.slideUp(animation_time);
        } else {
          accordionItem.addClass('expanded');
          accordionContent.slideDown(animation_time);
        }
  };

  $('.peer-review-accordion-expander').click(function (e) {
    e.preventDefault();
    self.togglePeerReviewAccordion($(this));
  });
};

var peerReview;

$(document).ready(function () {
  peerReview = new PeerReview();
});
