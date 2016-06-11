;
(function(window, angular, undefined) {

	angular.module('homer').controller('GoodFileUploadCtrl', [
		
		'$scope', '$http', '$log', '$interpolate', '$translate', 'actionToUrlMask', 'upload',
		function ($scope, $http, $log, $interpolate, $translate, actionToUrlMask, upload) {
			
			$translate([
				'GOOD_UPDATE_NOTIFICATION_ERROR_UPLOAD_ERROR_TITLE',
				'GOOD_UPDATE_NOTIFICATION_ERROR_REASON_TEXT'
			])
			.then(function(translations) {

				upload.option('url', actionToUrlMask['Pictures.add'].replace(/\/\:goodId/i, '\/' + $scope.goodRefId))
				$scope.options = upload.options

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#done
				$scope.$on('fileuploaddone', function(Event, data) {
					
					var newFileId = data.result, oldFile = data.files[0]

					var ext = oldFile.type.split('/')[1]
					if (ext == 'jpeg') ext = 'jpg'

					$scope.replace(
						[oldFile], [{
						name: String(newFileId),
						thumbnailUrl: $interpolate(actionToUrlMask['Pictures.thumb'])({
							id: newFileId
						}),
						url: $interpolate(actionToUrlMask['Pictures.view'])({
							id: newFileId, 
							extension: ext
						}),
						deleteUrl: $interpolate(actionToUrlMask['Pictures.delete'])({
							id: newFileId
						}),
						deleteType: 'DELETE',
						size: undefined
					}])
				})

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#fail
				$scope.$on('fileuploadfail', function(Event, data) {
					if (data.errorThrown == 'error') {
						window.swal(
							$interpolate(translations.GOOD_UPDATE_NOTIFICATION_ERROR_UPLOAD_ERROR_TITLE)({
								file: data.files[0].name
							}),
							$interpolate(translations.GOOD_UPDATE_NOTIFICATION_ERROR_REASON_TEXT)({
								status: data.status,
								statusText: data.statusText
							}),
							'error'
						)
					}
				})

				$scope.cancelUploadButton = false
				$scope.loadingFiles = true
				$scope.queue = []

				$http.get($scope.options.url)
				.then(
					function (response) {
						$scope.loadingFiles = false
						angular.forEach(response.data || [], function(value, key) {

							var ext = value.extension, id = value.id
							
							$scope.queue.push({
								name: String(id),
								thumbnailUrl: $interpolate(actionToUrlMask['Pictures.thumb'])({
									id: id
								}),
								url: $interpolate(actionToUrlMask['Pictures.view'])({
									id: id, 
									extension: ext
								}),
								deleteUrl: $interpolate(actionToUrlMask['Pictures.delete'])({
									id: id
								}),
								deleteType: 'DELETE',
								size: undefined
							})
						})
					},
					function () {
						$scope.loadingFiles = false
					}
				)
			})

		}
	])

})(window, angular, void 0);