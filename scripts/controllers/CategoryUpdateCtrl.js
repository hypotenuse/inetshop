
;
(function(window, angular, undefined) {

	angular.module('homer')

		.controller('CategoryUpdateCtrl', ['$window', '$timeout', '$interpolate', '$scope', '$http', '$modal', '$q', '$translate', 'actionToUrlMask', function($window, $timeout, $interpolate, $scope, $http, $modal, $q, $translate, actionToUrlMask) {

			$translate([
				'CATEGORY_UPDATE_VALIDATION_EXCEED_ERROR',
				'CATEGORY_UPDATE_VALIDATION_EMPTY_ERROR',
				'CATEGORY_UPDATE_VALIDATION_MINLENGTH_ERROR',
				'CATEGORY_UPDATE_VALIDATION_UNSUPPORTED_CHARACTERS_ERROR',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_INIT_ERROR_TITLE',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_PARENT_EQUAL_TO_ITSELF_TEXT',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_PARENT_EQUAL_TO_IT_CHILD_TEXT',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TEXT',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_NO_LIST_TEXT',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT',
				'CATEGORY_UPDATE_NOTIFICATION_ERROR_REASON_TEXT',
				'CATEGORY_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE',
				'CATEGORY_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT',
				'CATEGORY_UPDATE_SELECTBOX_NO_PARENT'
			])
			.then(function(translations) {

				function lengthExceeded(validationObject) {
					return validationObject.value.length > 250 ? 
						$interpolate(translations.CATEGORY_UPDATE_VALIDATION_EXCEED_ERROR)({
							exceed: validationObject.value.length - 250
						}) : false
				}
				
				$scope.TabLangManager = {
					langList: ['ru', 'en', 'uk'],
					update: function(event) {
						angular.forEach(this, function(value, key) {
							if(this.langList.indexOf(key) > -1) {
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
						var statesRE = /^\#tab\-(?:params|description|seo|image|menuimage)$/i, state
						if(state = window.location.hash.match(statesRE)) {
							$scope.TabManager.state = state
						}
						else {
							$scope.TabManager.state = '#tab-params'
						}
					}
				}

				$scope.TabManager.init()

				$scope.Validation = {
					slug: {
						value: '',
						invalidMsg: false,
						validate: function(_lengthExceeded) {
							if (this.value.length == 0) {
								this.invalidMsg = translations.CATEGORY_UPDATE_VALIDATION_EMPTY_ERROR
							}
							else if (this.value.length < 2) {
								this.invalidMsg = translations.CATEGORY_UPDATE_VALIDATION_MINLENGTH_ERROR
							}
							else if (_lengthExceeded = lengthExceeded(this)) {
								this.invalidMsg = _lengthExceeded
							}
							else if (/^(?:[a-z]|[0-9]|\-)+$/.test(this.value) == false) {
								this.invalidMsg = translations.CATEGORY_UPDATE_VALIDATION_UNSUPPORTED_CHARACTERS_ERROR
							}
							else {
								this.invalidMsg = false
							}
						}
					},
					parentId: {
						value: {},
						invalidMsg: false,
						validate: angular.noop
					},
					errorStack: [],
					changes: false,
					validatorNameRE: /^(?:(title|description|metatitle|metadescription)(Ru|En|Uk))|(slug)|(onhome)|(parentId)$/
				}
				
				angular.forEach(['onhome'], function(value) {
					$scope.Validation[value] = {
						state: false,
						validate: angular.noop
					}
				})

				angular.forEach($scope.TabLangManager.langList, function(value, key, list) {
					var capitalized = value.charAt(0).toUpperCase() + value.slice(1)
					angular.forEach(['title', 'description', 'metatitle', 'metadescription'], function(value, key, list) {
						var validatorName = value + capitalized
						if (value == 'description') {
							$scope.Validation[validatorName] = { 
								value: '',
								validate: function() {
									$scope.Validation['title' + capitalized].validate()
								}
							}
						}
						else {
							$scope.Validation[validatorName] = {
								value: '',
								invalidMsg: false,
								validate: function(_lengthExceeded) {
									if (_lengthExceeded = lengthExceeded(this)) {
										this.invalidMsg = _lengthExceeded
									}
									else {
										this.invalidMsg = false
									}
								}
							}
						}
					})
				})

				angular.forEach($scope.Validation, function(value, key, list) {

					var validateOld = value.validate

					if (validateOld) {
						value.validate = function(_lengthExceeded) {
							validateOld.call(value)
							if (!!value.invalidMsg) {
								if ($scope.Validation.errorStack.indexOf(value) == -1) {
									$scope.Validation.errorStack.push(value)
								}
							}
							else {
								for (var k = 0; k < $scope.Validation.errorStack.length; ++k) {
									if ($scope.Validation.errorStack[k] === value) {
										$scope.Validation.errorStack.splice(k, 1)
									}
								}
							}
							if ($scope.Validation.errorStack.length == 0) {
								$scope.Validation.changes = $scope.CategoryUpdateManager.dataMapDiff('bool')
							}
						}
					}
				})

				$scope.CategoryUpdateManager = {
					save: function() {
						
						var spinner = angular.element('.spinner')

						var langRequests = []
						var nonLangFields = []
						var mapKeys = []
						var reasons = []
						var dataMapDiff = $scope.CategoryUpdateManager.dataMapDiff('get')
						var requests = successes = 0
						var vcg = undefined
						var va = $scope.Validation
						var categoryUpdateUrl = $interpolate(actionToUrlMask['Categories.update'])({ id: this.currentCategory.data.category.id })
						
						var CATEGORY_PARENT_EQUAL_TO_ITSELF = 'CATEGORY_PARENT_EQUAL_TO_ITSELF'
						var CATEGORY_PARENT_EQUAL_TO_IT_CHILD = 'CATEGORY_PARENT_EQUAL_TO_IT_CHILD'

						var errorNotifier = function(reasons) {
							var errors = []
							var errorTitle = translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE
							for (var i = 0; i < reasons.length; ++i) {
								if (reasons[i].data.msg == CATEGORY_PARENT_EQUAL_TO_ITSELF) {
									errors.push(translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_PARENT_EQUAL_TO_ITSELF_TEXT)
								}
								else if (reasons[i].data.msg == CATEGORY_PARENT_EQUAL_TO_IT_CHILD) {
									errors.push(translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_PARENT_EQUAL_TO_IT_CHILD_TEXT)
								}
								else {
									var titleErrors = reasons[i].data['obj.title']
									if (titleErrors) {
										var lang = reasons[i].config.data.match(/\"languagecod\":\"(.+?)\"/)[1].toUpperCase()
										if (titleErrors[0].msg == 'error.required') {
											errors.push(
												$interpolate(translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT)({
													lang: lang
												})
											)
										}
										else if (angular.isArray(titleErrors[0].msg) && titleErrors[0].msg[0] == 'error.minLength') {
											errors.push(
												$interpolate(translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT)({
													lang: lang
												})
											)
										}
									}
									else {
										errors.push(
											$interpolate(translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_REASON_TEXT)({
												status: reasons[i].status,
												statusText: reasons[i].statusText
											})
										)
									}
								}
							}
							for (var i = 0; i < errors.length; ++i) {
								errors[i] = errors[i].replace(/^/, '<li>').replace(/$/, '</li>')
							}
							$window.swal({
								type: 'error',
								title: errorTitle,
								text: $interpolate("<ol>{{errors}}</ol>")({ errors: errors.join('') }),
								html: true
							})
						}

						var updateMapAndNotify = function(response) {
							
							spinner.removeClass('show')

							if (response == 'success') {
								$scope.CategoryUpdateManager.dataMapDiff('update')
								$window.swal(
									translations.CATEGORY_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE,
									translations.CATEGORY_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT,
									'success'
								)
							}
							else if (response == 'reasons') {
								errorNotifier(reasons)
							}
						}

						var processRequest = function(resolve, reject, llength) {
							requests = requests + 1
							if (requests == llength) {
								if (successes == llength) {
									resolve()
								}
								else {
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
							}
							else if (langRequest) {
								numberOfRequests = langRequests.length
							}
							else if (nonLangRequest) {
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
												if ((vcg = mapKeys[k].match(va.validatorNameRE)) && vcg[2] == language) {
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
												}
												else {
													fields[nonLangField] = 'value' in va[nonLangField] ? va[nonLangField].value : va[nonLangField].state
												}
											}
										}

										$http.put(categoryUpdateUrl, angular.toJson(fields))
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
							}
							else if (!vcg[1] && !vcg[2]) {
								nonLangFields.push(key)
							}
						}

						if (langRequests.length > 0 && nonLangFields.length > 0) {
							performRequests('both')
						}
						else if (langRequests.length > 0) {
							performRequests('langRequests')
						}
						else if (nonLangFields.length > 0) {
							performRequests('nonLangRequest')
						}
					},
					init: function(currentCategory, selectCategories) {
						var statusOk = function(status) {
							return status >= 200 && status <= 299
						}
						if (angular.equals(selectCategories, false)) {
							window.swal(
								translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_INIT_ERROR_TITLE,
								translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TEXT,
								'error'
							)
						}
						else if (false == statusOk(selectCategories.status)) {
							window.swal(
								translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_INIT_ERROR_TITLE,
								translations.CATEGORY_UPDATE_NOTIFICATION_ERROR_NO_LIST_TEXT,
								'error'
							)
						}
						else {
							var va = $scope.Validation
							this.currentCategory = currentCategory
							this.selectCategories = selectCategories
							va.slug.value = this.dataMap.slug = currentCategory.data.category.slug || ''
							va.onhome.state = this.dataMap.onhome = currentCategory.data.category.onhome

							angular.forEach($scope.TabLangManager.langList, function(lang, key) {
								
								var names = ['title', 'description', 'metatitle', 'metadescription']
								var capitalized = lang.charAt(0).toUpperCase() + lang.slice(1)

								if (angular.isObject($scope.CategoryUpdateManager.dataMap[lang]) == false) {
									$scope.CategoryUpdateManager.dataMap[lang] = {}
								}
								angular.forEach(names, function(value, key) {
									if(currentCategory.data.data[lang]) {
										$scope.CategoryUpdateManager.dataMap[lang][value] = va[value + capitalized].value = currentCategory.data.data[lang][value] || ''
									}
									else {
										$scope.CategoryUpdateManager.dataMap[lang][value] = ''
									}
								})
							})
							$scope.select = {
								disabled: false,
								categories: [{ id: 0, name: translations.CATEGORY_UPDATE_SELECTBOX_NO_PARENT, path: '' }]
							}
							if (angular.equals(this.currentCategory.data.category.parent, null)) {
								va.parentId.value = this.dataMap.parentId = $scope.select.categories[0]
							}
							angular.forEach(selectCategories.data, function(category, key) {
								
								var pathtoroot = angular.fromJson(category.pathtoroot)
								var pathPrepared = ''

								if (pathtoroot.length > 0) {
									angular.forEach(pathtoroot, function(path, key) {
										pathPrepared = pathPrepared + path.title + ((Number(key) + 1 == pathtoroot.length) ? ' / ' + category.title : ' / ')
									})
								}
								else {
									pathPrepared = '/'
								}
								$scope.select.categories.push({
									id: category.id,
									name: category.title,
									path: '<h6>' + pathPrepared + '</h6>'
								})

								if (currentCategory.data.category.parent == category.id) {
									va.parentId.value = this.dataMap.parentId = $scope.select.categories[key + 1]
								}

							}, this)

							if (angular.equals(this.dataMap.parentId, undefined)) {
								this.dataMap.parentId = {}
							}
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
									if (angular.equals(va[vcg[0]].value, this.dataMap[vcg[2].toLowerCase()][vcg[1]]) == false) {
										if (boolOrGetMode) {
											diffMap[vcg[0]] = va[vcg[0]].value
											diffList.push(diffMap)
										}
										else {
											this.dataMap[vcg[2].toLowerCase()][vcg[1]] = va[vcg[0]].value
										}
									}
								}
								else if (vaName = vcg[3] || vcg[5]) {
									if (angular.equals(va[vaName].value, this.dataMap[vaName]) == false) {
										if (boolOrGetMode) {
											diffMap[vaName] = va[vaName].value
											diffList.push(diffMap)
										}
										else {
											this.dataMap[vaName] = va[vaName].value
										}
									}
								}
								else if (vaName = vcg[4]) {
									if (angular.equals(va[vaName].state, this.dataMap[vaName]) == false) {
										if (boolOrGetMode) {
											diffMap[vaName] = va[vaName].state
											diffList.push(diffMap)
										}
										else {
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
						}
						else {
							va.changes = false
						}
					},

					dataMap: {}
				
				}

				$scope.CategoryUpdateManager.init($scope.currentCategory, $scope.selectCategories)

			})

		}])

})(window, window.angular, void 0);
