(function(window, angular, undefined) {

	angular.module('homer')

	.controller('SalesCtrl', ['$scope', '$state', '$http', '$modal', '$location', '$interpolate', '$translate', 'actionToUrlMask',
		'DTOptionsBuilder', 'DTColumnDefBuilder',
		function($scope, $state, $http, $modal, $location, $interpolate, $translate, actionToUrlMask, DTOptionsBuilder, DTColumnDefBuilder) {

			$translate([
					'SALES_MODAL_ADD_VALIDATION_EMPTY_ERROR',
					'SALES_MODAL_ADD_VALIDATION_MINLENGTH_ERROR',
					'SALES_MODAL_ADD_VALIDATION_EXCEED_ERROR',
					'SALES_NOTIFICATION_WARNING_TITLE',
					'SALES_NOTIFICATION_WARNING_TEXT',
					'SALES_NOTIFICATION_WARNING_BUTTON_CONFIRM',
					'SALES_NOTIFICATION_WARNING_BUTTON_CANCEL',
					'SALES_NOTIFICATION_SUCCESS_TITLE',
					'SALES_NOTIFICATION_SUCCESS_TEXT'
				])
				.then(function(translations) {

					$scope.translations = translations;

					$scope.salesManager = {
						list: function() {
							var requestOk = function(data) {
								var parsedsales = [];
								angular.forEach(data.data.data, function(sale, key) {
									var parsedsale = {};
									angular.forEach(sale, function(value, key) {
										if (key == '0') parsedsale.id = value;
										if (key == '1') parsedsale.title = value;
									});
									parsedsales.push(parsedsale);
								});
								parsedsales.sort(function(first, second) {
									return first.id > second.id ? 1 : ~0;
								});

								if ($translate.use() === 'ru_RU') {
									$scope.dtOptions.withPaginationType('full_numbers')
										.withLanguageSource('/assets/admin/scripts/controllers/locale-ru_RU.json');
								}

								$scope.sales = parsedsales;

							};
							var requestFail = function(data) {
								$log.warn('Something went wrong: (status ' + data.status + data.statusText + ')');
							};

							$http.get(actionToUrlMask['Sales.list']).then(requestOk, requestFail);

						},
						add: function() {
							$modalInstance = $modal.open({
								templateUrl: '/assets/admin/views/modal/sale.add.html',
								controller: ['$scope', function($scope) {
									$scope.Validation = {
										value: '',
										invalidMsg: false,
										validate: function() {
											if (this.value.length === 0) {
												this.invalidMsg = translations.SALES_MODAL_ADD_VALIDATION_EMPTY_ERROR;
											} else if (this.value.length < 2) {
												this.invalidMsg = translations.SALES_MODAL_ADD_VALIDATION_MINLENGTH_ERROR;
											} else if (this.value.length > 250) {
												this.invalidMsg = $interpolate(translations.SALES_MODAL_ADD_VALIDATION_EXCEED_ERROR)({
													exceed: this.value.length - 250
												});
											} else {
												this.invalidMsg = false;
											}
										}
									};
									$scope.salesColor = "#ffffff";
									$scope.Validation.validate();

									$scope.modalManager = {
										ok: function() {
											$http.post(actionToUrlMask['Sales.add'], angular.toJson({
													'title': $scope.Validation.value,
													'titlecolorbackgrnd': $scope.salesColor
												}))
												.then(function(data) {
													$modalInstance.close();
													$location.path('/admin/sales/' + data.data);
												});
										},
										cancel: function() {
											$modalInstance.dismiss('cancel');
										}
									};
								}]
							});
						},

						edit: function(sId) {
							$location.path($interpolate('/admin/sales/{{id}}')({
								id: sId
							}));
						},
						delete: function(sId) {
							window.swal({
									title: translations.SALES_NOTIFICATION_WARNING_TITLE,
									text: translations.SALES_NOTIFICATION_WARNING_TEXT,
									type: 'warning',
									showCancelButton: true,
									confirmButtonText: translations.SALES_NOTIFICATION_WARNING_BUTTON_CONFIRM,
									cancelButtonText: translations.SALES_NOTIFICATION_WARNING_BUTTON_CANCEL,
									closeOnConfirm: false,
									closeOnCancel: true
								},
								function(isConfirm) {
									if (isConfirm) {

										$http.delete($interpolate(actionToUrlMask['Sales.delete'])({
												id: sId
											}))
											.then(function(data) {
												for (var i = 0, lCats = $scope.sales.length; i < lCats; ++i) {
													if ($scope.sales[i].id === sId) {
														$scope.sales.splice(i, 1);
														$state.go($state.$current, null, {
															reload: true
														});
														return window.swal(
															translations.SALES_NOTIFICATION_SUCCESS_TITLE,
															translations.SALES_NOTIFICATION_SUCCESS_TEXT,
															'success'
														);
													}
												}
											});
									}
								});
						}
					};



					$scope.dtOptions = DTOptionsBuilder.newOptions().withDisplayLength(10);
					$scope.dtColumnDefs = [
						DTColumnDefBuilder.newColumnDef(0),
						DTColumnDefBuilder.newColumnDef(1)
					];

					$scope.salesManager.list();

				});

		}
	]);
})(window, angular, void 0);
