;
(function(window, angular, undefined) {

	angular.module('homer')

	.controller('NewsCtrl', ['$scope', '$http', '$modal', '$location',
		'$interpolate', '$translate', 'actionToUrlMask', 'DTOptionsBuilder',
		'DTColumnDefBuilder', '$state',
		function($scope, $http, $modal, $location, $interpolate, $translate,
			actionToUrlMask, DTOptionsBuilder, DTColumnDefBuilder, $state) {

			$translate([
					'NEWS_MODAL_ADD_VALIDATION_EMPTY_ERROR',
					'NEWS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR',
					'NEWS_MODAL_ADD_VALIDATION_EXCEED_ERROR',
					'NEWS_NOTIFICATION_WARNING_TITLE',
					'NEWS_NOTIFICATION_WARNING_TEXT',
					'NEWS_NOTIFICATION_WARNING_BUTTON_CONFIRM',
					'NEWS_NOTIFICATION_WARNING_BUTTON_CANCEL',
					'NEWS_NOTIFICATION_SUCCESS_TITLE',
					'NEWS_NOTIFICATION_SUCCESS_TEXT',
					'NEWS_NOTIFICATION_ERROR_TITLE',
					'NEWS_NOTIFICATION_ERROR_REASON_TEXT',
					'NEWS_LIST_ONHOME_TRUE',
					'NEWS_LIST_ONHOME_FALSE'
				])
				.then(function(translations) {

					$scope.translations = translations

					$scope.newsManager = {

						list: function() {
							var requestOk = function(data) {
								var parsedNews = []
								angular.forEach(data.data.data, function(item, key) {
									var parsedNew = {}
									angular.forEach(item, function(value, key) {
										if (key == '0') parsedNew.id = value
										if (key == '1') parsedNew.title = value
										if (key == '2') parsedNew.onhome = value
									})
									parsedNews.push(parsedNew)
								})
								parsedNews.sort(function(first, second) {
									return first.id > second.id ? 1 : ~0
								})
								$scope.news = parsedNews
								if ($translate.use() === 'ru_RU') {
									$scope.dtOptions.withPaginationType(
											'full_numbers')
										.withLanguageSource(
											'/assets/admin/scripts/controllers/locale-ru_RU.json'
										);
								}
							}
							var requestFail = function(data) {
								$log.warn('Something went wrong: (status ' + data.status + data.statusText +
									')')
							}

							$http.get(actionToUrlMask['News.list'])
								.then(requestOk, requestFail)
						},

						add: function() {
							$modalInstance = $modal.open({
								templateUrl: '/assets/admin/views/modal/news.add.html',
								controller: ['$scope', function($scope) {
									$scope.Validation = {
										value: '',
										invalidMsg: false,
										validate: function() {
											if (this.value.length == 0) {
												this.invalidMsg = translations.NEWS_MODAL_ADD_VALIDATION_EMPTY_ERROR
											} else if (this.value.length < 2) {
												this.invalidMsg = translations.NEWS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR
											} else if (this.value.length > 250) {
												this.invalidMsg = $interpolate(translations.NEWS_MODAL_ADD_VALIDATION_EXCEED_ERROR)
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
											$http.post(actionToUrlMask['News.add'], angular.toJson({
													'title': $scope.Validation.value
												}))
												.then(function(data) {
													$modalInstance.close()
													$location.path('/admin/news/' + data.data)
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

					$scope.newManager = {
						edit: function(catId) {
							$location.path($interpolate('/admin/news/{{id}}')({
								id: catId
							}))
						},
						delete: function(catId) {
							window.swal({
									title: translations.NEWS_NOTIFICATION_WARNING_TITLE,
									text: translations.NEWS_NOTIFICATION_WARNING_TEXT,
									type: 'warning',
									showCancelButton: true,
									confirmButtonText: translations.NEWS_NOTIFICATION_WARNING_BUTTON_CONFIRM,
									cancelButtonText: translations.NEWS_NOTIFICATION_WARNING_BUTTON_CANCEL,
									closeOnConfirm: false,
									closeOnCancel: true
								},
								function(isConfirm) {
									if (isConfirm) {
										$http.delete($interpolate(actionToUrlMask['News.delete'])({
												id: catId
											}))
											.then(
												function(data) {
													for (var i = 0, lCats = $scope.news.length; i < lCats; ++i) {
														if ($scope.news[i].id == catId) {
															$state.go($state.$current, null, {
																reload: true
															});
															window.swal(
																translations.NEWS_NOTIFICATION_SUCCESS_TITLE,
																translations.NEWS_NOTIFICATION_SUCCESS_TEXT,
																'success'
															)
														}
													}
												},
												function(data) {
													window.swal(
														translations.NEWS_NOTIFICATION_ERROR_TITLE,
														$interpolate(translations.NEWS_NOTIFICATION_ERROR_REASON_TEXT)
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
					}
					$scope.dtOptions = DTOptionsBuilder.newOptions().withPaginationType(
						'full_numbers').withDisplayLength(10);
					$scope.dtColumnDefs = [
						DTColumnDefBuilder.newColumnDef(0),
						DTColumnDefBuilder.newColumnDef(1)
					];
					$scope.newsManager.list()

				})

		}
	])

})(window, angular, void 0);
