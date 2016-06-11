;
(function(window, angular, undefined) {

	angular.module('homer')

	.controller('SubcategoriesCtrl', ['$scope', '$http', '$interpolate', '$modal',
		'$location', '$translate', 'actionToUrlMask',
		function($scope, $http, $interpolate, $modal, $location, $translate,
			actionToUrlMask) {

			$translate([
					'SUBCATEGORIES_MODAL_ADD_VALIDATION_EMPTY_ERROR',
					'SUBCATEGORIES_MODAL_ADD_VALIDATION_MINLENGTH_ERROR',
					'SUBCATEGORIES_MODAL_ADD_VALIDATION_EXCEED_ERROR',
					'SUBCATEGORIES_INIT_ERROR_NOT_FOUND_TITLE',
					'SUBCATEGORIES_INIT_ERROR_NOT_FOUND_TEXT',
					'SUBCATEGORIES_INIT_ERROR_NO_LIST_TITLE',
					'SUBCATEGORIES_INIT_ERROR_NO_LIST_TEXT',
					'SUBCATEGORIES_NOTIFICATION_WARNING_HAS_CHILDREN_TEXT',
					'SUBCATEGORIES_NOTIFICATION_WARNING_BUTTON_CONFIRM',
					'SUBCATEGORIES_NOTIFICATION_WARNING_BUTTON_CANCEL',
					'SUBCATEGORIES_NOTIFICATION_WARNING_TITLE',
					'SUBCATEGORIES_NOTIFICATION_WARNING_TEXT',
					'SUBCATEGORIES_NOTIFICATION_SUCCESS_TITLE',
					'SUBCATEGORIES_NOTIFICATION_SUCCESS_TEXT',
					'SUBCATEGORIES_NOTIFICATION_ERROR_TITLE',
					'SUBCATEGORIES_NOTIFICATION_ERROR_HAS_CHILDREN_TEXT',
					'SUBCATEGORIES_NOTIFICATION_ERROR_REASON_TEXT',
					'SUBCATEGORIES_LIST_ONHOME_TRUE',
					'SUBCATEGORIES_LIST_ONHOME_FALSE'
				])
				.then(function(translations) {

					$scope.translations = translations

					var statusOk = function(status) {
						return status >= 200 && status <= 299
					}

					$scope.subcategoriesManager = {

						hideButtons: false,

						init: function(currentCategory, childCategories) {
							if (angular.equals(childCategories, false)) {
								window.swal(
									translations.SUBCATEGORIES_INIT_ERROR_NOT_FOUND_TITLE,
									translations.SUBCATEGORIES_INIT_ERROR_NOT_FOUND_TEXT,
									'error'
								)
								this.hideButtons = true
							} else if (false == statusOk(childCategories.status)) {
								window.swal(
									translations.SUBCATEGORIES_INIT_ERROR_NO_LIST_TITLE,
									translations.SUBCATEGORIES_INIT_ERROR_NO_LIST_TEXT,
									'error'
								)
								this.hideButtons = true
							} else {
								var parsedSubcategories = []
								angular.forEach(childCategories.data.data, function(category, key) {
									var parsedSubcategory = {}
									angular.forEach(category, function(value, key) {
										if (key == '0') parsedSubcategory.id = value
										if (key == '1') parsedSubcategory.title = value
										if (key == '2') parsedSubcategory.onhome = value
									})
									parsedSubcategories.push(parsedSubcategory)
								})
								parsedSubcategories.sort(function(first, second) {
									return first.id > second.id ? 1 : ~0
								})
								this.currentCategory = currentCategory
								this.childCategories = childCategories
								$scope.subcategories = parsedSubcategories
								if (currentCategory.data.data.ru) {
									var pathtoroot = angular.fromJson(currentCategory.data.data.ru.pathtoroot)
									if (angular.isArray(pathtoroot)) {
										var breadcrumbs = []
										var pathtorootids = []
										angular.forEach(currentCategory.data.category.pathtorootids.split(
											'/'), function(pathId, key) {
											if (pathId !== '') {
												pathtorootids.push(pathId)
											}
										})
										angular.forEach(pathtoroot, function(category, key) {
											breadcrumbs.push({
												id: pathtorootids[key],
												title: category.title
											})
										})
										breadcrumbs.push({
											id: currentCategory.data.category.id,
											title: currentCategory.data.data.ru.title
										})
										$scope.breadcrumbs = breadcrumbs
									}
								}
							}
						},

						add: function() {
							var subcategoriesManager = this
							$modalInstance = $modal.open({
								templateUrl: '/assets/admin/views/modal/subcategory.add.html',
								controller: ['$scope', function($scope) {
									$scope.Validation = {
										value: '',
										invalidMsg: false,
										validate: function() {
											if (this.value.length == 0) {
												this.invalidMsg = translations.SUBCATEGORIES_MODAL_ADD_VALIDATION_EMPTY_ERROR
											} else if (this.value.length < 2) {
												this.invalidMsg = translations.SUBCATEGORIES_MODAL_ADD_VALIDATION_MINLENGTH_ERROR
											} else if (this.value.length > 250) {
												this.invalidMsg = $interpolate(translations.SUBCATEGORIES_MODAL_ADD_VALIDATION_EXCEED_ERROR)
													({
														exceed: this.value.length - 250
													})
											} else {
												this.invalidMsg = false
											}
										}
									}

									$scope.Validation.validate()

									$scope.modalManager = {
										ok: function() {
											$http.post(actionToUrlMask['Categories.add'], angular.toJson({
													'title': $scope.Validation.value,
													'parentId': subcategoriesManager.currentCategory.data
														.category.id
												}))
												.then(function(data) {
													$modalInstance.close()
													$location.path('/admin/categories/' + data.data +
														'/edit')
												})
										},
										cancel: function() {
											$modalInstance.dismiss('cancel')
										}
									}
								}]
							})
						},

						delete: function() {
							if (this.childCategories.data.data.length) {
								window.swal({
									text: translations.SUBCATEGORIES_NOTIFICATION_WARNING_HAS_CHILDREN_TEXT,
									title: '',
									type: 'warning'
								})
							} else {
								var subcategoriesManager = this
								window.swal({
										title: translations.SUBCATEGORIES_NOTIFICATION_WARNING_TITLE,
										text: translations.SUBCATEGORIES_NOTIFICATION_WARNING_TEXT,
										type: 'warning',
										showCancelButton: true,
										confirmButtonText: translations.SUBCATEGORIES_NOTIFICATION_WARNING_BUTTON_CONFIRM,
										cancelButtonText: translations.SUBCATEGORIES_NOTIFICATION_WARNING_BUTTON_CANCEL,
										closeOnConfirm: true,
										closeOnCancel: true
									},
									function(isConfirm) {
										if (isConfirm) {
											$http.delete($interpolate(actionToUrlMask['Categories.delete'])
													({
														id: subcategoriesManager.currentCategory.data.category.id
													}))
												.then(
													function(data) {
														window.swal(
															translations.SUBCATEGORIES_NOTIFICATION_SUCCESS_TITLE,
															translations.SUBCATEGORIES_NOTIFICATION_SUCCESS_TEXT,
															'success'
														)
														$location.path('/admin/categories')
													},
													function(data) {
														window.swal(
															translations.SUBCATEGORIES_NOTIFICATION_ERROR_TITLE,
															$interpolate(translations.SUBCATEGORIES_NOTIFICATION_ERROR_REASON_TEXT)
															({
																status: data.status,
																statusText: data.statusText
															}),
															'error'
														)
													}
												)
										}
									})
							}
						},

						edit: function() {
							$location.path('/admin/categories/' + this.currentCategory.data.category
								.id + '/edit')
						}
					}

					$scope.subcategoryManager = {

						edit: function(catId) {
							$location.path($interpolate('/admin/categories/{{id}}')({
								id: catId
							}) + '/edit')
						},

						delete: function(catId) {
							window.swal({
									title: translations.SUBCATEGORIES_NOTIFICATION_WARNING_TITLE,
									text: translations.SUBCATEGORIES_NOTIFICATION_WARNING_TEXT,
									type: 'warning',
									showCancelButton: true,
									confirmButtonText: translations.SUBCATEGORIES_NOTIFICATION_WARNING_BUTTON_CONFIRM,
									cancelButtonText: translations.SUBCATEGORIES_NOTIFICATION_WARNING_BUTTON_CANCEL,
									closeOnConfirm: false,
									closeOnCancel: true
								},
								function(isConfirm) {
									if (isConfirm) {
										$http.delete($interpolate(actionToUrlMask['Categories.delete'])
												({
													id: catId
												}))
											.then(
												function(data) {
													for (var i = 0, lCats = $scope.subcategories.length; i <
														lCats; ++i) {
														if ($scope.subcategories[i].id == catId) {
															$scope.subcategories.splice(i, 1)
															return window.swal(
																translations.SUBCATEGORIES_NOTIFICATION_SUCCESS_TITLE,
																translations.SUBCATEGORIES_NOTIFICATION_SUCCESS_TEXT,
																'success'
															)
														}
													}
												},
												function(data) {
													if (data.data.msg == 'CATEGORY_HAS_CHILDREN') {
														window.swal(
															translations.SUBCATEGORIES_NOTIFICATION_ERROR_TITLE,
															translations.SUBCATEGORIES_NOTIFICATION_ERROR_HAS_CHILDREN_TEXT,
															'error'
														)
													} else {
														window.swal(
															translations.SUBCATEGORIES_NOTIFICATION_ERROR_TITLE,
															$interpolate(translations.SUBCATEGORIES_NOTIFICATION_ERROR_REASON_TEXT)
															({
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

					$scope.subcategoriesManager.init($scope.currentCategory, $scope.childCategories)

				})

		}
	])

})(window, angular, void 0);
