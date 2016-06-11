/**
 * HOMER - Responsive Admin Theme (Modifiend by hypotenuse)
 * version 1.7
 *
 */
(function(window, angular, undefined) {
	angular.module('homer', [

		// AngularJS native directives for Bootstrap
		'ui.bootstrap',

		// Angular flexible routing
		'ui.router',

		// Angular animations
		'ngAnimate',

		// Summernote plugin (https://github.com/summernote/angular-summernote)
		'summernote',

		// Blueimp file upload module (https://github.com/blueimp/jQuery-File-Upload)
		'blueimp.fileupload',

		'ngSanitize',

		//Multilanguage support
		'pascalprecht.translate',

		// AngularJS-native version of Select2 and Selectize (https://github.com/angular-ui/ui-select)
		'ui.select',

		'datatables',

		'datatables.select',

		'colorpicker.module',

		'ngFileUpload',

	]);
})(window, window.angular, void 0);
