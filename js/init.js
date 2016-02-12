(function($){
  $(function(){

	// Initialize collapse button
	$(".button-collapse").sideNav();
	// Initialize collapsible (uncomment the line below if you use the dropdown variation)
	//$('.collapsible').collapsible();
	$('.scrollspy').scrollSpy();
	$('.toc-wrapper').pushpin({ top: $('.toc-wrapper').offset() ? $('.toc-wrapper').offset().top : 0 });
  }); // end of document ready
})(jQuery); // end of jQuery name space
