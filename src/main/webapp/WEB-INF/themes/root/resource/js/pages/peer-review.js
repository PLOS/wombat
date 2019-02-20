var PeerReview = new function() {
  this.animation_time = '500';
  this.togglePeerReviewAccordion = function (expandLink) {
    var accordionItem = expandLink.parents(".peer-review-accordion-item");
    var accordionContent = accordionItem.find('.peer-review-accordion-content');
    if (accordionItem.hasClass('expanded')) {
          accordionItem.removeClass('expanded');
          accordionContent.slideUp(this.animation_time);
        } else {
          accordionItem.addClass('expanded');
          accordionContent.slideDown(this.animation_time);
        }
  };
}

$(document).ready(function () {
  $('.peer-review-accordion-expander').click(function (e) {
    e.preventDefault();
    PeerReview.togglePeerReviewAccordion($(this));
  });

  var doi = new URLSearchParams(location.search).get("id").replace("10.1371/journal.", "");
  var reviewEl = document.querySelector("[data-doi='" + doi + "'] .peer-review-accordion-expander");
  if(reviewEl){
    PeerReview.togglePeerReviewAccordion($(reviewEl));
  }
});
