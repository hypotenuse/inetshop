
;
(function(window, angular, undefined) {

	angular.module('homer')

		.controller('CategoriesCtrl', ['$scope', '$http', '$modal', '$location', '$interpolate', '$translate', 'actionToUrlMask', function($scope, $http, $modal, $location, $interpolate, $translate, actionToUrlMask) {

			$translate([
				'CATEGORIES_MODAL_ADD_VALIDATION_EMPTY_ERROR',
				'CATEGORIES_MODAL_ADD_VALIDATION_MINLENGTH_ERROR',
				'CATEGORIES_MODAL_ADD_VALIDATION_EXCEED_ERROR',
				'CATEGORIES_NOTIFICATION_WARNING_TITLE',
				'CATEGORIES_NOTIFICATION_WARNING_TEXT',
				'CATEGORIES_NOTIFICATION_WARNING_BUTTON_CONFIRM',
				'CATEGORIES_NOTIFICATION_WARNING_BUTTON_CANCEL',
				'CATEGORIES_NOTIFICATION_SUCCESS_TITLE',
				'CATEGORIES_NOTIFICATION_SUCCESS_TEXT',
				'CATEGORIES_NOTIFICATION_ERROR_TITLE',
				'CATEGORIES_NOTIFICATION_ERROR_CHILDREN_TEXT',
				'CATEGORIES_NOTIFICATION_ERROR_REASON_TEXT',
				'CATEGORIES_LIST_ONHOME_TRUE',
				'CATEGORIES_LIST_ONHOME_FALSE'
			])
			.then(function(translations) {
				
				$scope.translations = translations

				$scope.categoriesManager = {

					list: function() {
						var requestOk = function(data) {
							var parsedCategories = []
							angular.forEach(data.data.data, function(category, key) {
								var parsedCategory = {}
								angular.forEach(category, function(value, key) {
									if (key == '0') parsedCategory.id = value
									if (key == '1') parsedCategory.title = value
									if (key == '2') parsedCategory.onhome = value
								})
								parsedCategories.push(parsedCategory)
							})
							parsedCategories.sort(function(first, second) { return first.id > second.id ? 1 : ~0 })
							$scope.categories = parsedCategories
						}
						var requestFail = function(data) {
							$log.warn('Something went wrong: (status ' + data.status + data.statusText + ')')
						}
						
						$http.get(actionToUrlMask['Categories.list'])
							.then(requestOk, requestFail)
					},
					
					add: function() {
						$modalInstance = $modal.open({
							templateUrl: '/assets/admin/views/modal/category.add.html',
							controller: ['$scope', function($scope) {
								$scope.Validation = {
									value: '',
									invalidMsg: false,
									validate: function() {
										if (this.value.length == 0) {
											this.invalidMsg = translations.CATEGORIES_MODAL_ADD_VALIDATION_EMPTY_ERROR
										}
										else if (this.value.length < 2) {
											this.invalidMsg = translations.CATEGORIES_MODAL_ADD_VALIDATION_MINLENGTH_ERROR
										}
										else if (this.value.length > 250) { 
											this.invalidMsg = $interpolate(translations.CATEGORIES_MODAL_ADD_VALIDATION_EXCEED_ERROR)({
												exceed: this.value.length - 250
											})
										}
										else {
											this.invalidMsg = false
										}
									}
								}
								
								$scope.Validation.validate()

								$scope.modalManager = {
									ok: function() {
										$http.post(actionToUrlMask['Categories.add'], angular.toJson({'title': $scope.Validation.value, 'parentId': null}))
										.then(function(data) {
											$modalInstance.close()
											$location.path('/admin/categories/' + data.data + '/edit')
										})
									},
									cancel: function() {
										$modalInstance.dismiss('cancel')
									}
								}
							}]
						})
					}
				}

				$scope.categoryManager = {
					edit: function(catId) {
						$location.path($interpolate('/admin/categories/{{id}}')({ id: catId }) + '/edit')
					},
					delete: function(catId) {
						window.swal({
							title: translations.CATEGORIES_NOTIFICATION_WARNING_TITLE,
							text: translations.CATEGORIES_NOTIFICATION_WARNING_TEXT,
							type: 'warning',
							showCancelButton: true,
							confirmButtonText: translations.CATEGORIES_NOTIFICATION_WARNING_BUTTON_CONFIRM,
							cancelButtonText: translations.CATEGORIES_NOTIFICATION_WARNING_BUTTON_CANCEL,
							closeOnConfirm: false,
							closeOnCancel: true
						},
						function (isConfirm) {
							if (isConfirm) {
								$http.delete($interpolate(actionToUrlMask['Categories.delete'])({ id: catId }))
								.then(
									function(data) {
										for (var i = 0, lCats = $scope.categories.length; i < lCats; ++i) {
											if ($scope.categories[i].id == catId) {
												$scope.categories.splice(i, 1)
												return window.swal(
													translations.CATEGORIES_NOTIFICATION_SUCCESS_TITLE, 
													translations.CATEGORIES_NOTIFICATION_SUCCESS_TEXT, 
													'success'
												)
											}
										}
									},
									function(data) {
										if (data.data.msg == 'CATEGORY_HAS_CHILDREN') {
											window.swal(
												translations.CATEGORIES_NOTIFICATION_ERROR_TITLE,
												translations.CATEGORIES_NOTIFICATION_ERROR_CHILDREN_TEXT,
												'error'
											)
										}
										else {
											window.swal(
												translations.CATEGORIES_NOTIFICATION_ERROR_TITLE,
												$interpolate(translations.CATEGORIES_NOTIFICATION_ERROR_REASON_TEXT)({
													status: data.status,
													statusText: data.statusText
												}),
												'error'
											)
										}
									}
								)
							}
						})
					}
				}

				$scope.categoriesManager.list()

			})
		
		}])

})(window, angular, void 0);

