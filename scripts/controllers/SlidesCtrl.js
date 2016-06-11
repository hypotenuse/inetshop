(function(window, angular, undefined) {

	angular.module('homer').controller('SlidesCtrl', ['$scope', '$state', '$http', '$modal', '$location', '$interpolate', '$translate',
		'actionToUrlMask', '$log', 'DTOptionsBuilder', 'DTColumnDefBuilder', 'fileReader',
		function($scope, $state, $http, $modal, $location, $interpolate, $translate, actionToUrlMask, $log, DTOptionsBuilder,
			DTColumnDefBuilder, fileReader) {

			$translate([
					'SLIDES_MODAL_ADD_VALIDATION_EMPTY_ERROR',
					'SLIDES_MODAL_ADD_VALIDATION_MINLENGTH_ERROR',
					'SLIDES_MODAL_ADD_VALIDATION_EXCEED_ERROR',
					'SLIDES_MODAL_ADD_VALIDATION_URL',
					'SLIDES_NOTIFICATION_WARNING_TITLE',
					'SLIDES_NOTIFICATION_WARNING_TEXT',
					'SLIDES_NOTIFICATION_WARNING_BUTTON_CONFIRM',
					'SLIDES_NOTIFICATION_WARNING_BUTTON_CANCEL',
					'SLIDES_NOTIFICATION_SUCCESS_TITLE',
					'SLIDES_NOTIFICATION_SUCCESS_TEXT',
					'SLIDES_NOTIFICATION_ADD_SUCCESS_TITLE',
					'SLIDES_NOTIFICATION_ADD_SUCCESS_TEXT',
					'SLIDES_NOTIFICATION_ADD_ERROR_TITLE',
					'SLIDES_NOTIFICATION_ADD_ERROR_TEXT'
				])
				.then(function(translations) {

					$scope.translations = translations;

					$scope.slidesManager = {
						list: function() {
							var requestOk = function(data) {
								var parsedslides = [];
								angular.forEach(data.data.data, function(slide, key) {
									var parsedslide = {};
									angular.forEach(slide, function(value, key) {
										if (key === 'id') parsedslide.id = value;
										if (key === 'url') parsedslide.url = value;
										if (key === 'picture') parsedslide.picture = value;
									});
									parsedslides.push(parsedslide);
								});
								parsedslides.sort(function(first, second) {
									return first.id > second.id ? 1 : ~0;
								});

								$scope.slides = parsedslides;

							};
							var requestFail = function(data) {
								$log.warn('Something went wrong: (status ' + data.status + data.statusText + ')');
							};

							$http.get(actionToUrlMask['Slides.list']).then(requestOk, requestFail);

						},
						add: function() {

							$modalInstance = $modal.open({
								templateUrl: '/assets/admin/views/modal/slide.add.html',
								controller: ['$scope', 'fileReader', function($scope, fileReader) {
									$scope.Validation = {
										value: '',
										invalidMsg: false,
										validate: function() {
											var myRegExp =
												/^(?:(?:https?|ftp):\/\/)(?:\S+(?::\S*)?@)?(?:(?!10(?:\.\d{1,3}){3})(?!127(?:\.\d{1,3}){3})(?!169\.254(?:\.\d{1,3}){2})(?!192\.168(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?$/i;

											if (this.value.length < 5) {
												this.invalidMsg = translations.SLIDES_MODAL_ADD_VALIDATION_MINLENGTH_ERROR;
											} else if (this.value.length > 250) {
												this.invalidMsg = $interpolate(translations.SLIDES_MODAL_ADD_VALIDATION_EXCEED_ERROR)({
													exceed: this.value.length - 250
												});
											} else if (!myRegExp.test(this.value)) {
												this.invalidMsg = translations.SLIDES_MODAL_ADD_VALIDATION_URL;
											} else {
												this.invalidMsg = false;
											}
										}
									};

									$scope.getFile = function() {
										$scope.progress = 0;
										fileReader.readAsDataUrl($scope.file, $scope)
											.then(function(result) {
												$scope.imageSrc = result;
											});
									};

									$scope.removeImage = function() {
										$scope.imageSrc = null;
									};

									$scope.$on("fileProgress", function(e, progress) {
										$scope.progress = progress.loaded / progress.total;
									});
									// start

									$scope.modalManager = {
										ok: function() {
											var fd = new FormData();
											fd.append('picture', $scope.picture);
											fd.append('url', $scope.Validation.value);

											$http.post(actionToUrlMask['Slides.add'], fd, {
												transformRequest: angular.identity,
												headers: {
													'Content-Type': undefined,
													'Csrf-Token': 'nocheck'
												}
											}).then(function(data) {
												$modalInstance.close();
												$state.go($state.$current, null, {
													reload: true
												});
												return window.swal(
													translations.SLIDES_NOTIFICATION_ADD_SUCCESS_TITLE,
													translations.SLIDES_NOTIFICATION_ADD_SUCCESS_TEXT,
													'success'
												);
											}).catch(function(err) {
												console.log("error occured : " + angular.toJson(err, ' '));
												return window.swal(
													translations.SLIDES_NOTIFICATION_ADD_ERROR_TITLE,
													translations.SLIDES_NOTIFICATION_ADD_ERROR_TEXT,
													'error'
												);
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
							$location.path($interpolate('/admin/slides/{{id}}')({
								id: sId
							}));
						},

						delete: function(sId) {
							window.swal({
									title: translations.SLIDES_NOTIFICATION_WARNING_TITLE,
									text: translations.SLIDES_NOTIFICATION_WARNING_TEXT,
									type: 'warning',
									showCancelButton: true,
									confirmButtonText: translations.SLIDES_NOTIFICATION_WARNING_BUTTON_CONFIRM,
									cancelButtonText: translations.SLIDES_NOTIFICATION_WARNING_BUTTON_CANCEL,
									closeOnConfirm: false,
									closeOnCancel: true
								},
								function(isConfirm) {
									if (isConfirm) {

										$http.delete($interpolate(actionToUrlMask['Slides.delete'])({
												id: sId
											}))
											.then(function(data) {
												for (var i = 0, ISlds = $scope.slides.length; i < ISlds; ++i) {
													if ($scope.slides[i].id === sId) {
														$scope.slides.splice(i, 1);
														$state.go($state.$current, null, {
															reload: true
														});
														return window.swal(
															translations.SLIDES_NOTIFICATION_SUCCESS_TITLE,
															translations.SLIDES_NOTIFICATION_SUCCESS_TEXT,
															'success'
														);
													}
												}
											});
									}
								});
						}
					};

					$scope.slidesManager.list();

				});

		}
	]);
})(window, angular, void 0);
