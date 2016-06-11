;
(function(window, angular, undefined) {

	angular.module('homer').controller('GoodFileDestroyCtrl', [

		'$scope', '$http', 'actionToUrlMask',
		function ($scope, $http, actionToUrlMask) {

			var file = $scope.file, state

			if (file.url) {
				file.$state = function () {
					return state
				}
				file.$destroy = function () {
					state = 'pending'
					return $http({
						url: file.deleteUrl,
						method: file.deleteType
					})
					.then(
						function () {
							state = 'resolved'
							$scope.clear(file)
						},
						function () {
							state = 'rejected'
						}
					)
				}
			}
			else if (!file.$cancel && !file._index) {
				file.$cancel = function () {
					$scope.clear(file)
				}
			}
		}
	])

})(window, angular, void 0);