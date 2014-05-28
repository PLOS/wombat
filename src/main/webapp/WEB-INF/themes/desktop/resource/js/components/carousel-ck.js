/**
 * Created by pgrinbaum on 5/21/14.
 * using jcarousel
 */$(function(){var e='[data-js="carousel-current-item"]',t='span[data-js="carousel-total-index"]';$(".jcarousel").on("jcarousel:create",function(e,n){var r=$(this).jcarousel("items").size();$(this).next(".carousel-control").find(t).html(r)}).on("jcarousel:animateend",function(t,n){var r=$(this).jcarousel("visible").index(),i=r+1;$(this).next(".carousel-control").find(e).html(i)}).jcarousel({wrap:"both"});$(".jcarousel-prev").jcarouselControl({target:"-=1"});$(".jcarousel-next").jcarouselControl({target:"+=1"})});