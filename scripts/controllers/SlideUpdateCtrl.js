(function(window, angular, undefined) {

	angular.module('homer').controller('SlideUpdateCtrl', [
		'$window', '$timeout', '$interpolate', '$scope', '$http', '$modal', '$q', '$translate', 'actionToUrlMask', 'fileReader', '$state',
		function($window, $timeout, $interpolate, $scope, $http, $modal, $q, $translate, actionToUrlMask, fileReader, $state) {

			$translate([
					'SLIDE_UPDATE_VALIDATION_EMPTY_ERROR',
					'SLIDE_UPDATE_VALIDATION_MINLENGTH_ERROR',
					'SLIDE_UPDATE_VALIDATION_EXCEED_ERROR',
					'SLIDE_UPDATE_VALIDATION_URL',
					'SLIDE_NOTIFICATION_WARNING_TITLE',
					'SLIDE_NOTIFICATION_WARNING_TEXT',
					'SLIDE_NOTIFICATION_WARNING_BUTTON_CONFIRM',
					'SLIDE_NOTIFICATION_WARNING_BUTTON_CANCEL',
					'SLIDE_NOTIFICATION_SUCCESS_TITLE',
					'SLIDE_NOTIFICATION_SUCCESS_TEXT',
					'SLIDE_NOTIFICATION_UPDATE_SUCCESS_TITLE',
					'SLIDE_NOTIFICATION_UPDATE_SUCCESS_TEXT',
					'SLIDE_NOTIFICATION_UPDATE_ERROR_TITLE',
					'SLIDE_NOTIFICATION_UPDATE_ERROR_TEXT'
				])
				.then(function(translations) {

					function getNumber() {
						return Math.floor(Math.random() * (999999 - 100000)) + 100000;
					}

					$scope.SlideUpdateManager = {
						save: function() {
							var fd = new FormData();
							fd.append('picture', $scope.picture);
							fd.append('url', $scope.Validation.value);

							$http.post($interpolate(actionToUrlMask['Slides.update'])({
								id: this.currentSlide.data.id
							}), fd, {
								transformRequest: angular.identity,
								headers: {
									'Content-Type': undefined,
									'Csrf-Token': 'nocheck'
								}
							}).then(function(data) {
								$state.go($state.$current, null, {
									reload: true
								});
								$scope.imageSrc = null;
								return window.swal(
									translations.SLIDE_NOTIFICATION_UPDATE_SUCCESS_TITLE,
									translations.SLIDE_NOTIFICATION_UPDATE_SUCCESS_TEXT,
									'success'
								);
							}).catch(function(err) {
								$state.go($state.$current, null, {
									reload: true
								});
								$scope.imageSrc = null;
								console.log("error occured : " + angular.toJson(err, ' '));
								return window.swal(
									translations.SLIDE_NOTIFICATION_UPDATE_ERROR_TITLE,
									translations.SLIDE_NOTIFICATION_UPDATE_ERROR_TEXT,
									'error'
								);
							});

						},
						init: function(currentSlide) {
							var statusOk = function(status) {
								return status >= 200 && status <= 299;
							};

							this.currentSlide = currentSlide;
							$scope.randomNum = getNumber();


							$scope.Validation = {
								value: currentSlide.data.url,
								invalidMsg: false,
								validate: function() {
									var myRegExp =
										/^(?:(?:https?|ftp):\/\/)(?:\S+(?::\S*)?@)?(?:(?!10(?:\.\d{1,3}){3})(?!127(?:\.\d{1,3}){3})(?!169\.254(?:\.\d{1,3}){2})(?!192\.168(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?$/i;
									if (this.value.length === 0) {
										this.invalidMsg = false;
									} else if (this.value.length < 5) {
										this.invalidMsg = translations.SLIDE_UPDATE_VALIDATION_MINLENGTH_ERROR;
									} else if (this.value.length > 250) {
										this.invalidMsg = $interpolate(translations.SLIDE_UPDATE_VALIDATION_EXCEED_ERROR)({
											exceed: this.value.length - 250
										});
									} else if (!myRegExp.test(this.value)) {
										this.invalidMsg = translations.SLIDE_UPDATE_VALIDATION_URL;
									} else {
										this.invalidMsg = false;
									}
								}
							};


						}

					};

					$scope.currentSlide.data.picture = $scope.currentSlide.data.picture + '?rnd=' + getNumber();
					$scope.SlideUpdateManager.init($scope.currentSlide);

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



				});

		}
	]);


})(window, window.angular, void 0);
