;
(function(window, angular, undefined) {

	angular.module('homer')

	.controller('NewsUpdateCtrl', ['$window', '$scope', '$interpolate', '$log',
		'$timeout', '$http', '$modal', '$q', '$translate', 'actionToUrlMask',
		function($window, $scope, $interpolate, $log, $timeout, $http, $modal, $q,
			$translate, actionToUrlMask) {

			$translate([
					'NEWS_UPDATE_VALIDATION_EXCEED_ERROR',
					'NEWS_UPDATE_VALIDATION_EMPTY_ERROR',
					'NEWS_UPDATE_VALIDATION_MINLENGTH_ERROR',
					'NEWS_UPDATE_VALIDATION_UNSUPPORTED_CHARACTERS_ERROR',
					'NEWS_UPDATE_VALIDATION_INVALID_NUMBER_FORMAT_ERROR',
					'NEWS_UPDATE_VALIDATION_NON_NUMBERS_ERROR',
					'NEWS_UPDATE_VALIDATION_NUMBER_EXCEED_ERROR',
					'NEWS_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE',
					'NEWS_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TITLE',
					'NEWS_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TEXT',
					'NEWS_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT',
					'NEWS_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT',
					'NEWS_UPDATE_NOTIFICATION_ERROR_REASON_TEXT',
					'NEWS_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE',
					'NEWS_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT',
					'NEWS_UPDATE_SECTION_TITLE',
					'NEWS_UPDATE_TAB_DESCRIPTION',
					'NEWS_UPDATE_TITLE',
					'NEWS_UPDATE_DESCRIPTION',
					'NEWS_UPDATE_BUTTON_SAVE'
				])
				.then(function(translations) {

					function lengthExceeded(validationObject) {
						return validationObject.value.length > 250 ?
							$interpolate(translations.NEWS_UPDATE_VALIDATION_EXCEED_ERROR)({
								exceed: validationObject.value.length - 250
							}) : false
					}

					$scope.TabLangManager = {
						langList: ['ru', 'en', 'uk'],
						update: function(event) {
							angular.forEach(this, function(value, key) {
								if (this.langList.indexOf(key) > -1) {
									delete this[key]
								}
							}, this)
							this[event.currentTarget.innerHTML.toLowerCase()] = true;
						}
					}

					$scope.TabLangManager.ru = true

					$scope.TabManager = {
						update: function(event) {
							var state = event.currentTarget.children[0].hash;
							$scope.TabManager.state = state;
						},
						init: function() {
							var statesRE = /^\#tab\-(?:params|content|seo|images)$/i,
								state;
							if (state === window.location.hash.match(statesRE)) {
								$scope.TabManager.state = state;
							} else {
								$scope.TabManager.state = '#tab-description';
							}
						}
					};

					$scope.TabManager.init();

					$scope.Summernote = {
						config: {
							lang: 'ru-RU',
							height: 300,
							minHeight: 300,
							maxHeight: 1200
						}
					}

					$scope.Validation = {
						errorStack: [],
						changes: false,
						validatorNameRE: /^(?:(title|content)(Ru|En|Uk))$/
					};

					angular.forEach($scope.TabLangManager.langList, function(value, key,
						list) {
						var capitalized = value.charAt(0).toUpperCase() + value.slice(1);
						angular.forEach(['title', 'content'], function(value, key, list) {
							var validatorName = value + capitalized;
							if (value == 'content') {
								$scope.Validation[validatorName] = {
									value: '',
									validate: function() {
										$scope.Validation['title' + capitalized].validate();
									}
								};
							} else {
								$scope.Validation[validatorName] = {
									value: '',
									invalidMsg: false,
									validate: function(_lengthExceeded) {
										if (_lengthExceeded === lengthExceeded(this)) {
											this.invalidMsg = _lengthExceeded;
										} else {
											this.invalidMsg = false;
										}
									}
								};
							}
						});
					});

					angular.forEach($scope.Validation, function(value, key, list) {

						var validateOld = value.validate;

						if (validateOld) {
							value.validate = function(_lengthExceeded) {
								validateOld.call(value);
								if (!!value.invalidMsg) {
									if ($scope.Validation.errorStack.indexOf(value) == -1) {
										$scope.Validation.errorStack.push(value);
									}
								} else {
									for (var k = 0; k < $scope.Validation.errorStack.length; ++k) {
										if ($scope.Validation.errorStack[k] === value) {
											$scope.Validation.errorStack.splice(k, 1);
										}
									}
								}
								if ($scope.Validation.errorStack.length === 0) {
									$scope.Validation.changes = $scope.NewsUpdateManager.dataMapDiff(
										'bool');
								}
							};
						}
					});

					$scope.NewsUpdateManager = {
						save: function() {

							var spinner = angular.element('.spinner');

							var langRequests = [];
							var nonLangFields = [];
							var mapKeys = [];
							var reasons = [];
							var dataMapDiff = $scope.NewsUpdateManager.dataMapDiff(
								'get');
							var requests = successes = 0;
							var vcg = undefined;
							var va = $scope.Validation;
							var newsUpdateUrl = $interpolate(actionToUrlMask[
								'News.update'])({
								id: this.currentNews.data.news.id
							});

							var errorNotifier = function(reasons) {
								var errors = [];
								var errorTitle = translations.NEWS_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE;
								for (var i = 0; i < reasons.length; ++i) {
									var titleErrors = reasons[i].data['obj.title'];
									if (titleErrors) {
										var lang = reasons[i].config.data.match(
											/\"languagecod\":\"(.+?)\"/)[1].toUpperCase();
										if (titleErrors[0].msg == 'error.required') {
											errors.push(
												$interpolate(translations.NEWS_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT)
												({
													lang: lang
												})
											);
										} else if (angular.isArray(titleErrors[0].msg) && titleErrors[
												0].msg[0] == 'error.minLength') {
											errors.push(
												$interpolate(translations.NEWS_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT)
												({
													lang: lang
												})
											);
										}
									} else if (reasons[i].data == 'TITLE_IS_REQUIRED') {
										tabLang = JSON.parse(reasons[i]['config']['data']);
										errors.push(
											$interpolate(translations.NEWS_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT)
											({
												lang: tabLang.languagecod
											}));
									} else {
										errors.push(
											$interpolate(translations.NEWS_UPDATE_NOTIFICATION_ERROR_REASON_TEXT)
											({
												status: reasons[i].status,
												statusText: reasons[i].statusText
											})
										);
									}
									// }
								}
								for (var j = 0; j < errors.length; ++j) {
									errors[j] = errors[j].replace(/^/, '<li>').replace(/$/, '</li>');
								}
								$window.swal({
									type: 'error',
									title: errorTitle,
									text: $interpolate("<ol>{{errors}}</ol>")({
										errors: errors.join('')
									}),
									html: true
								});
							};

							var updateMapAndNotify = function(response) {

								spinner.removeClass('show')

								if (response == 'success') {
									$scope.NewsUpdateManager.dataMapDiff('update')
									$window.swal(
										translations.NEWS_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE,
										translations.NEWS_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT,
										'success'
									);
								} else if (response == 'reasons') {
									errorNotifier(reasons)
								}
							}

							var processRequest = function(resolve, reject, llength) {
								requests = requests + 1
								if (requests == llength) {
									if (successes == llength) {
										resolve()
									} else {
										reject()
									}
								}
							}

							var performRequests = function(requestType) {

								var numberOfRequests = undefined

								var bothRequest = requestType == 'both'
								var langRequest = requestType == 'langRequests'
								var nonLangRequest = requestType == 'nonLangRequest'

								if (bothRequest) {
									numberOfRequests = langRequests.length + 1
								} else if (langRequest) {
									numberOfRequests = langRequests.length
								} else if (nonLangRequest) {
									numberOfRequests = 1
								}

								$q(function(resolve, reject) {

										var requestIndex = 0

										var performRequest = function(i) {
											if (i < numberOfRequests) {

												var fields = {}
												var lastRequest = i == numberOfRequests - 1

												if (bothRequest && !lastRequest || langRequest) {

													var language = langRequests[i]
													fields['languagecod'] = language.toLowerCase()

													for (var k = 0; k < mapKeys.length; ++k) {
														if ((vcg = mapKeys[k].match(va.validatorNameRE)) && vcg[
																2] == language) {
															fields[vcg[1]] = va[mapKeys[k]].value
														}
													}
												}

												if (bothRequest && lastRequest || nonLangRequest) {

													fields['languagecod'] = 'ru'

													for (var j = 0; j < nonLangFields.length; ++j) {
														var nonLangField = nonLangFields[j]
														if (nonLangField == 'parentId') {
															fields[nonLangField] = va[nonLangField].value.id
														} else {
															fields[nonLangField] = 'value' in va[nonLangField] ? va[
																nonLangField].value : va[nonLangField].state
														}
													}
												}
												console.log("field : " + angular.toJson(fields));
												$http.put(newsUpdateUrl, angular.toJson(fields))
													.then(
														function(response) {
															successes = successes + 1
															processRequest(resolve, reject, numberOfRequests)
															performRequest(
																++requestIndex
															)
														},
														function(reason) {
															reasons.push(reason)
															processRequest(resolve, reject, numberOfRequests)
															performRequest(
																++requestIndex
															)
														}
													)
											}
										}

										performRequest(requestIndex)

									})
									.then(
										function() {
											// All requests are complete
											// And all these requests are successful
											updateMapAndNotify('success')
										},
										function() {
											updateMapAndNotify('reasons')
										}
									)
							}

							spinner.addClass('show')

							for (var i = 0; i < dataMapDiff.length; ++i) {

								var key = Object.keys(dataMapDiff[i])[0]

								mapKeys.push(key)
								vcg = key.match(va.validatorNameRE)

								if (vcg[1] && vcg[2]) {
									if (langRequests.indexOf(vcg[2]) == -1) {
										langRequests.push(vcg[2])
									}
								} else if (!vcg[1] && !vcg[2]) {
									nonLangFields.push(key)
								}
							}

							if (langRequests.length > 0 && nonLangFields.length > 0) {
								performRequests('both')
							} else if (langRequests.length > 0) {
								performRequests('langRequests')
							} else if (nonLangFields.length > 0) {
								performRequests('nonLangRequest')
							}
						},
						init: function(currentNews) {
							var statusOk = function(status) {
								return status >= 200 && status <= 299
							}
							var va = $scope.Validation
							this.currentNews = currentNews

							angular.forEach($scope.TabLangManager.langList, function(lang, key) {

								var names = ['title', 'content']
								var capitalized = lang.charAt(0).toUpperCase() + lang.slice(1)

								if (angular.isObject($scope.NewsUpdateManager.dataMap[
										lang]) == false) {
									$scope.NewsUpdateManager.dataMap[lang] = {}
								}
								angular.forEach(names, function(value, key) {
									if (currentNews.data.data[lang]) {
										$scope.NewsUpdateManager.dataMap[lang][value] = va[
											value + capitalized].value = currentNews.data.data[
											lang][value] || ''
									} else {
										$scope.NewsUpdateManager.dataMap[lang][value] = ''
									}
								})
							})

							if (angular.equals(this.dataMap.parentId, undefined)) {
								this.dataMap.parentId = {}
							}
						},
						dataMapDiff: function(mode) {

							var va = $scope.Validation
							var boolOrGetMode = (mode == 'bool' || mode == 'get')
							var diffList = []
							var vcg = vaName = undefined

							for (var keys = Object.keys(va), i = 0; i < keys.length; ++i) {
								if (vcg = keys[i].match(va.validatorNameRE)) {

									var diffMap = {}

									if (vcg[1] && vcg[2]) {
										if (angular.equals(va[vcg[0]].value, this.dataMap[vcg[2].toLowerCase()]
												[vcg[1]]) == false) {
											if (boolOrGetMode) {
												diffMap[vcg[0]] = va[vcg[0]].value
												diffList.push(diffMap)
											} else {
												this.dataMap[vcg[2].toLowerCase()][vcg[1]] = va[vcg[0]].value
											}
										}
									} else if (vaName = vcg[3] || vcg[5]) {
										if (angular.equals(va[vaName].value, this.dataMap[vaName]) ==
											false) {
											if (boolOrGetMode) {
												diffMap[vaName] = va[vaName].value
												diffList.push(diffMap)
											} else {
												this.dataMap[vaName] = va[vaName].value
											}
										}
									} else if (vaName = vcg[4]) {
										if (angular.equals(va[vaName].state, this.dataMap[vaName]) ==
											false) {
											if (boolOrGetMode) {
												diffMap[vaName] = va[vaName].state
												diffList.push(diffMap)
											} else {
												this.dataMap[vaName] = va[vaName].state
											}
										}
									}
								}
								if (mode == 'bool' && diffList.length > 0) {
									return true
								}
							}
							if (boolOrGetMode) {
								return mode == 'bool' ? false : diffList
							} else {
								va.changes = false
							}
						},

						dataMap: {}

					}
					$scope.NewsUpdateManager.init($scope.currentNews);
				})
		}
	])

})(window, window.angular, void 0);
