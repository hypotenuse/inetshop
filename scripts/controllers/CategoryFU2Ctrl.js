;
(function(window, angular, undefined) {

	angular.module('homer').controller('CategoryFU2Ctrl', [
		
		'$scope', '$http', '$log', '$interpolate', '$timeout', '$translate', 'actionToUrlMask', 'upload',
		function ($scope, $http, $log, $interpolate, $timeout, $translate, actionToUrlMask, upload) {

			$translate([
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_UPLOAD_ERROR_TITLE',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_REASON_TEXT',
				'CATEGORY_UPDATE_UPLOAD_SUCCESSFULLY_UPLOADED'
			])
			.then(function(translations) {

				var category = $scope.currentCategory.data.category
				var catid = category.id

				upload = angular.copy(upload)

				upload.option('url', $interpolate(actionToUrlMask['Categories.addCategoryPicture'])({ id: catid }))
				upload.option('dropZone', angular.element('#\\#tab-image'))
				$scope.options = upload.options

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#done
				$scope.$on('fileuploaddone', function(Event, data) {

					var nocacheid = Math.random().toString().split('.')[1]
					var result = data.result
					var oldFile = data.files[0]

					$scope.replace(
						[oldFile], [{
						
						name: translations.CATEGORY_UPDATE_UPLOAD_SUCCESSFULLY_UPLOADED,

						thumbnailUrl: $interpolate(actionToUrlMask['Categories.thumb'])({
							id: catid,
							nocacheid: nocacheid
						}),
						url: $interpolate(actionToUrlMask['Categories.thumb'])({
							id: catid,
							nocacheid: nocacheid
						}),
						deleteUrl: $interpolate(actionToUrlMask['Categories.deleteCategoryPicture'])({
							id: catid
						}),
						deleteType: 'GET',
						size: undefined
					}])

					$scope.disableAddFileButton = $scope.disableLoadButton = true

				})

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#fail
				$scope.$on('fileuploadfail', function(Event, data) {
					if (data.errorThrown == 'error') {
						window.swal(
							$interpolate(translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_UPLOAD_ERROR_TITLE)({
								file: data.files[0].name
							}),
							$interpolate(translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_REASON_TEXT)({
								status: data.status,
								statusText: data.statusText
							}),
							'error'
						)
					}
					else if (data.errorThrown == 'abort') {
						$scope.disableAddFileButton = $scope.disableLoadButton = false
					}
				})
				
				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#add
				$scope.$on('fileuploadadd', function(Event, data) {
					$scope.disableAddFileButton = true
					while ($scope.queue.length) {
						$scope.queue.shift()
					}
				})

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#processalways
				$scope.$on('fileuploadprocessalways', function(Event, data) {
					var processedFile = data.files[data.index]
					if (processedFile.error) {
						$scope.disableLoadButton = true
					}
					else {
						$scope.disableLoadButton = false
					}
				})

				$scope.disableAddFileButton = $scope.disableLoadButton = $scope.cancelUploadButton = $scope.loadingFiles = false
				$scope.queue = []

				if (category.haveCategoryPicture) {
					
					$scope.disableAddFileButton = $scope.disableLoadButton = true

					var pictureCategoryUrl = $scope.currentCategory.data.pictureCategoryUrl
					var fileName = pictureCategoryUrl.match(/\d+?\.\w{3,3}/i)[0]

					$scope.queue.push({
						name: fileName,
						thumbnailUrl: pictureCategoryUrl,
						url: pictureCategoryUrl,
						deleteUrl: $interpolate(actionToUrlMask['Categories.deleteCategoryPicture'])({
							id: catid
						}),
						deleteType: 'GET',
						size: undefined
					})
				}

			})
		}
	])

})(window, angular, void 0);