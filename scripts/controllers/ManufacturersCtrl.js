(function(window, angular, undefined) {

	angular.module('homer')

	.controller('ManufacturersCtrl', ['$scope', '$state', '$http', '$modal', '$location', '$interpolate', '$translate', 'actionToUrlMask',
		'DTOptionsBuilder', 'DTColumnDefBuilder',
		function($scope, $state, $http, $modal, $location, $interpolate, $translate, actionToUrlMask, DTOptionsBuilder, DTColumnDefBuilder) {

			$translate([
					'MANUFACTURERS_MODAL_ADD_VALIDATION_EMPTY_ERROR',
					'MANUFACTURERS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR',
					'MANUFACTURERS_MODAL_ADD_VALIDATION_EXCEED_ERROR',
					'MANUFACTURERS_NOTIFICATION_WARNING_TITLE',
					'MANUFACTURERS_NOTIFICATION_WARNING_TEXT',
					'MANUFACTURERS_NOTIFICATION_WARNING_BUTTON_CONFIRM',
					'MANUFACTURERS_NOTIFICATION_WARNING_BUTTON_CANCEL',
					'MANUFACTURERS_NOTIFICATION_SUCCESS_TITLE',
					'MANUFACTURERS_NOTIFICATION_SUCCESS_TEXT'
				])
				.then(function(translations) {

					$scope.translations = translations;

					$scope.manufacturersManager = {
						list: function() {
							var requestOk = function(data) {
								var parsedmanufacturers = [];
								angular.forEach(data.data.data, function(manufacturer, key) {
									var parsedManufacturer = {};
									angular.forEach(manufacturer, function(value, key) {
										if (key == '0') parsedManufacturer.id = value;
										if (key == '1') parsedManufacturer.title = value;
										if (key == '2') parsedManufacturer.onhome = value;
									});
									parsedmanufacturers.push(parsedManufacturer);
								});
								parsedmanufacturers.sort(function(first, second) {
									return first.id > second.id ? 1 : ~0;
								});
								$scope.manufacturers = parsedmanufacturers;


								if ($translate.use() === 'ru_RU') {
									$scope.dtOptions.withPaginationType('full_numbers')
										.withLanguageSource('/assets/admin/scripts/controllers/locale-ru_RU.json');
								}


							};
							var requestFail = function(data) {
								$log.warn('Something went wrong: (status ' + data.status + data.statusText + ')');
							};

							$http.get(actionToUrlMask['Manufacturers.list'])
								.then(requestOk, requestFail);

						},
						add: function() {
							$modalInstance = $modal.open({
								templateUrl: '/assets/admin/views/modal/manufacturer.add.html',
								controller: ['$scope', function($scope) {
									$scope.Validation = {
										value: '',
										invalidMsg: false,
										validate: function() {
											if (this.value.length === 0) {
												this.invalidMsg = translations.MANUFACTURERS_MODAL_ADD_VALIDATION_EMPTY_ERROR;
											} else if (this.value.length < 2) {
												this.invalidMsg = translations.MANUFACTURERS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR;
											} else if (this.value.length > 250) {
												this.invalidMsg = $interpolate(translations.MANUFACTURERS_MODAL_ADD_VALIDATION_EXCEED_ERROR)({
													exceed: this.value.length - 250
												});
											} else {
												this.invalidMsg = false;
											}
										}
									};

									$scope.Validation.validate();

									$scope.modalManager = {
										ok: function() {
											$http.post(actionToUrlMask['Manufacturers.add'], angular.toJson({
													'title': $scope.Validation.value
												}))
												.then(function(data) {
													$modalInstance.close();
													$location.path('/admin/manufacturers/' + data.data + '/edit');
												});
										},
										cancel: function() {
											$modalInstance.dismiss('cancel');
										}
									};
								}]
							});
						}
					};

					$scope.manufacturerManager = {
						edit: function(mfId) {
							$location.path($interpolate('/admin/manufacturers/{{id}}')({
								id: mfId
							}) + '/edit');
						},
						delete: function(mfId) {
							window.swal({
									title: translations.MANUFACTURERS_NOTIFICATION_WARNING_TITLE,
									text: translations.MANUFACTURERS_NOTIFICATION_WARNING_TEXT,
									type: 'warning',
									showCancelButton: true,
									confirmButtonText: translations.MANUFACTURERS_NOTIFICATION_WARNING_BUTTON_CONFIRM,
									cancelButtonText: translations.MANUFACTURERS_NOTIFICATION_WARNING_BUTTON_CANCEL,
									closeOnConfirm: false,
									closeOnCancel: true
								},
								function(isConfirm) {
									if (isConfirm) {
										$http.delete($interpolate(actionToUrlMask['Manufacturers.delete'])({
												id: mfId
											}))
											.then(function(data) {
												for (var i = 0, lCats = $scope.manufacturers.length; i < lCats; ++i) {
													if ($scope.manufacturers[i].id == mfId) {
														$scope.manufacturers.splice(i, 1);
														$state.go($state.$current, null, {
															reload: true
														});
														return window.swal(
															translations.MANUFACTURERS_NOTIFICATION_SUCCESS_TITLE,
															translations.MANUFACTURERS_NOTIFICATION_SUCCESS_TEXT,
															'success'
														);
													}
												}
											});
									}
								});
						}
					};
					$scope.dtOptions = DTOptionsBuilder.newOptions().withPaginationType('full_numbers').withDisplayLength(10);
					$scope.dtColumnDefs = [
						DTColumnDefBuilder.newColumnDef(0),
						DTColumnDefBuilder.newColumnDef(1)
					];
					$scope.manufacturersManager.list();
				});

		}
	]);
})(window, angular, void 0);
