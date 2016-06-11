;
(function(window, angular, undefined) {

	angular.module('homer')

	.controller('ExcelsettingsCtrl', ['$scope', '$http', '$modal', '$location',
		'$interpolate', '$translate', 'actionToUrlMask', 'DTOptionsBuilder',
		'DTColumnDefBuilder', '$state',
		function($scope, $http, $modal, $location, $interpolate, $translate,
			actionToUrlMask, DTOptionsBuilder, DTColumnDefBuilder, $state) {

			$translate([
					'NOTIFICATION_WARNING_TITLE',
					'NOTIFICATION_WARNING_TEXT',
					'NOTIFICATION_WARNING_BUTTON_CONFIRM',
					'NOTIFICATION_WARNING_BUTTON_CANCEL',
					'NOTIFICATION_SUCCESS_TITLE',
					'NOTIFICATION_SUCCESS_TEXT',
					'NOTIFICATION_ERROR_TITLE',
					'NOTIFICATION_ERROR_REASON_TEXT',
					'NOTIFICATION_DELETE_SUCCESS_TITLE',
					'NOTIFICATION_DELETE_SUCCESS_TEXT',
				])
				.then(function(translations) {

					$scope.excelsettingsManager = {

						list: function() {
							var requestOk = function(data) {
								var parsedSettings = []
								angular.forEach(data.data.data, function(item, key) {
									var parsedSetting = {}
									parsedSetting.id = item[0]
									parsedSetting.title = item[1]
									parsedSettings.push(parsedSetting)
								})
								parsedSettings.sort(function(first, second) {
									return first.id > second.id ? 1 : ~0
								})
								$scope.excelsettings = parsedSettings
								if ($translate.use() === 'ru_RU') {
									$scope.dtOptions.withPaginationType('full_numbers').withLanguageSource('/assets/admin/scripts/controllers/locale-ru_RU.json')
								}
							}
							var requestFail = function(data) {
								$log.warn('Something went wrong: (status ' + data.status + data.statusText + ')')
							}

							$http.get(actionToUrlMask['ExcelSettings.list'])
								.then(requestOk, requestFail)
						},

						add: function() {
							$location.path('/admin/excelsettings/new')
						}
					}

					$scope.excelsettingManager = {
						edit: function(excelsettingId) {
							$location.path($interpolate('/admin/excelsettings/{{id}}')({
								id: excelsettingId
							}))
						},
						delete: function(excelsettingId) {
							window.swal({
									title: translations.NOTIFICATION_WARNING_TITLE,
									text: translations.NOTIFICATION_WARNING_TEXT,
									type: 'warning',
									showCancelButton: true,
									confirmButtonText: translations.NOTIFICATION_WARNING_BUTTON_CONFIRM,
									cancelButtonText: translations.NOTIFICATION_WARNING_BUTTON_CANCEL,
									closeOnConfirm: false,
									closeOnCancel: true
								},
								function(isConfirm) {
									if (isConfirm) {
										$http.delete($interpolate(actionToUrlMask['ExcelSettings.delete'])({
												id: excelsettingId
											}))
											.then(
												function(data) {
													for (var i = 0, lCats = $scope.excelsettings.length; i < lCats; ++i) {
														if ($scope.excelsettings[i].id == excelsettingId) {
															
															$state.go($state.$current, null, {
																reload: true
															})

															window.swal(
																translations.NOTIFICATION_DELETE_SUCCESS_TITLE,
																translations.NOTIFICATION_DELETE_SUCCESS_TEXT,
																'success'
															)
														}
													}
												},
												function(data) {
													window.swal(
														translations.NOTIFICATION_ERROR_TITLE,
														$interpolate(translations.NOTIFICATION_ERROR_REASON_TEXT)
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

					$scope.dtOptions = DTOptionsBuilder.newOptions().withPaginationType('full_numbers').withDisplayLength(10)
					$scope.dtColumnDefs = [
						DTColumnDefBuilder.newColumnDef(0),
						DTColumnDefBuilder.newColumnDef(1)
					]

					$scope.excelsettingsManager.list()

				})
		}
	])

})(window, angular, void 0);
