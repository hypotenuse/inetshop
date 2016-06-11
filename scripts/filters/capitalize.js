
(function (window, angular, undefined) {
	angular.module('homer')
		.filter('capitalize',  function() {
			return function(langList) {
				var capitalized = []
				angular.forEach(langList, function(value) {
					capitalized.push(value.charAt(0).toUpperCase() + value.slice(1))
				})
				return capitalized
			}
		})
})(window, window.angular, void 0);