
angular.module('homer')

.config(['$translateProvider', function($translateProvider) {

	var appLanguage = 'ru_RU'

	$translateProvider.useStaticFilesLoader({
		files: [{
			prefix: '/assets/admin/scripts/controllers/locale-',
			suffix: '.json'
		}]
	})
		
	$translateProvider.preferredLanguage(appLanguage)
	$translateProvider.useSanitizeValueStrategy(null)

}])

.run(['$translate', function($translate) {

	$translate('APP_TITLE').then(function(APP_TITLE) {
		angular.element('title').get(0).text = APP_TITLE
	})
	
}])

.controller('myTranslator', ['$translate', '$scope', function($translate, $scope) {
	$scope.changeLang = function (lang) {
		$translate.use(lang)
	}
}])