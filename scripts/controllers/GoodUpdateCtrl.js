
;
(function(window, angular, undefined) {

	angular.module('homer')

		.controller('GoodUpdateCtrl', ['$window', '$scope', '$interpolate', '$log', '$timeout', '$http', '$modal', '$q', '$translate', 'actionToUrlMask', function($window, $scope, $interpolate, $log, $timeout, $http, $modal, $q, $translate, actionToUrlMask) {
			
			$translate([
				'GOOD_UPDATE_VALIDATION_EXCEED_ERROR',
				'GOOD_UPDATE_VALIDATION_EMPTY_ERROR',
				'GOOD_UPDATE_VALIDATION_MINLENGTH_ERROR',
				'GOOD_UPDATE_VALIDATION_UNSUPPORTED_CHARACTERS_ERROR',
				'GOOD_UPDATE_VALIDATION_INVALID_NUMBER_FORMAT_ERROR',
				'GOOD_UPDATE_VALIDATION_NON_NUMBERS_ERROR',
				'GOOD_UPDATE_VALIDATION_NUMBER_EXCEED_ERROR',
				'GOOD_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE',
				'GOOD_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TITLE',
				'GOOD_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TEXT',
				'GOOD_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT',
				'GOOD_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT',
				'GOOD_UPDATE_NOTIFICATION_ERROR_REASON_TEXT',
				'GOOD_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE',
				'GOOD_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT',
				'GOOD_UPDATE_SELECTBOX_MANUFACTURER_PLACEHOLDER',
				'GOOD_UPDATE_SELECTBOX_CATEGORY_PLACEHOLDER',
				'GOOD_UPDATE_NO'
			])
			.then(function(translations) {

				function lengthExceeded(validationObject) {
					return validationObject.value.length > 250 ? 
						$interpolate(translations.GOOD_UPDATE_VALIDATION_EXCEED_ERROR)({
							exceed: validationObject.value.length - 250
						}) : false
				}
				
				$scope.placeholders = {
					GOOD_UPDATE_SELECTBOX_MANUFACTURER_PLACEHOLDER: translations.GOOD_UPDATE_SELECTBOX_MANUFACTURER_PLACEHOLDER,
					GOOD_UPDATE_SELECTBOX_CATEGORY_PLACEHOLDER: translations.GOOD_UPDATE_SELECTBOX_CATEGORY_PLACEHOLDER
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
						var statesRE = /^\#tab\-(?:params|description|seo|images)$/i, state
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
								this.invalidMsg = translations.GOOD_UPDATE_VALIDATION_EMPTY_ERROR
							}
							else if (this.value.length < 2) {
								this.invalidMsg = translations.GOOD_UPDATE_VALIDATION_MINLENGTH_ERROR
							}
							else if (_lengthExceeded = lengthExceeded(this)) {
								this.invalidMsg = _lengthExceeded
							}
							else if (/^(?:[a-z]|[0-9]|\-)+$/.test(this.value) == false) {
								this.invalidMsg = translations.GOOD_UPDATE_VALIDATION_UNSUPPORTED_CHARACTERS_ERROR
							}
							else {
								this.invalidMsg = false
							}
						}
					},
					partnumber: {
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
					},
					cost: {
						value: 0,
						invalidMsg: false,
						validate: function() {
							if (/^[0-9]{1,8}(?:(\.)[0-9]{1,2})?$/.test(this.value) == false) {
								this.invalidMsg = translations.GOOD_UPDATE_VALIDATION_INVALID_NUMBER_FORMAT_ERROR
							}
							else {
								this.invalidMsg = false
							}
						}
					},
					warranty: {
						value: 0,
						invalidMsg: false,
						validate: function() {
							if (/^[0-9]+$/.test(this.value) == false) {
								this.invalidMsg = translations.GOOD_UPDATE_VALIDATION_NON_NUMBERS_ERROR
							}
							else if (Number(this.value) > 99) {
								this.invalidMsg = translations.GOOD_UPDATE_VALIDATION_NUMBER_EXCEED_ERROR
							}
							else {
								this.invalidMsg = false
							}
						}
					},
					manufacturer: {
						value: {},
						invalidMsg: false,
						validate: angular.noop
					},
					category: {
						value: [],
						invalidMsg: false,
						validate: angular.noop
					},
					errorStack: [],
					changes: false,
					validatorNameRE: /^(?:(title|description|descriptionShort|metatitle|metadescription)(Ru|En|Uk))|(slug|partnumber|cost|warranty)|(import_short_desc|newg|top)|(manufacturer)|(category)$/
				}
				
				angular.forEach(['import_short_desc', 'newg', 'top'], function(value) {
					$scope.Validation[value] = {
						state: false,
						validate: angular.noop
					}
				})

				angular.forEach($scope.TabLangManager.langList, function(value, key, list) {
					var capitalized = value.charAt(0).toUpperCase() + value.slice(1)
					angular.forEach(['title', 'description', 'descriptionShort', 'metatitle', 'metadescription'], function(value, key, list) {
						var validatorName = value + capitalized
						if (value == 'description') {
							$scope.Validation[validatorName] = { 
								value: '',
								validate: function() {
									$scope.Validation['title' + capitalized].validate()
								}
							}
						}
						else if (value == 'descriptionShort') {
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
					var vcg = undefined

					if (validateOld) {
						value.validate = function(_lengthExceeded) {
							var validateWrapper = function() {
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
									$scope.Validation.changes = $scope.GoodUpdateManager.dataMapDiff('bool')
								}
							}
							if ((vcg = key.match($scope.Validation.validatorNameRE)) 
								&& vcg[1] == 'description') {
								// workaround for summernote onchange event 
								// (onchange happends before value is initialized)
								$timeout(function() { validateWrapper() }, 0)
							}
							else {
								validateWrapper()
							}
						}
					}
				})

				$scope.GoodUpdateManager = {
					save: function() {
						
						var spinner = angular.element('.spinner')

						var langRequests = []
						var nonLangFields = []
						var mapKeys = []
						var reasons = []
						var dataMapDiff = $scope.GoodUpdateManager.dataMapDiff('get')
						var requests = successes = 0
						var vcg = undefined
						var va = $scope.Validation

						var errorNotifier = function(reasons) {
							var errors = []
							var errorTitle = translations.GOOD_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE
							for (var i = 0; i < reasons.length; ++i) {
								var titleErrors = reasons[i].data['obj.title']
								if (titleErrors) {
									var lang = reasons[i].config.data.match(/\"languagecod\":\"(.+?)\"/)[1].toUpperCase()
									if (titleErrors[0].msg == 'error.required') {
										errors.push(
											$interpolate(translations.GOOD_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT)({
												lang: lang
											})
										)
									}
									else if (angular.isArray(titleErrors[0].msg) && titleErrors[0].msg[0] == 'error.minLength') {
										errors.push(
											$interpolate(translations.GOOD_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT)({
												lang: lang
											})
										)
									}
								}
								else {
									errors.push(
										$interpolate(translations.GOOD_UPDATE_NOTIFICATION_ERROR_REASON_TEXT)({
											status: reasons[i].status,
											statusText: reasons[i].statusText
										})
									)
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
								$scope.GoodUpdateManager.dataMapDiff('update')
								$window.swal(
									translations.GOOD_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE,
									translations.GOOD_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT,
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
								
								var save = function(fields) {
									$http.put($scope.goodViewUrl, angular.toJson(fields))
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

								var performRequest = function(i) {
									if (i < numberOfRequests) {

										var categoryUpdateFields
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
												if (nonLangField == 'cost' || nonLangField == 'warranty') {
													fields[nonLangField] = Number(va[nonLangField].value)
												}
												else if (nonLangField == 'manufacturer') {
													fields[nonLangField] = va[nonLangField].value.id
												}
												else if (nonLangField == 'category') {
													categoryUpdateFields = {
														categories: []
													}
													for (var ii = 0, categories = va[nonLangField].value; ii < categories.length; ++ii) {
														var category = categories[ii]
														categoryUpdateFields.categories.push(category.id)
													}
												}
												else {
													fields[nonLangField] = 'value' in va[nonLangField] ? va[nonLangField].value : va[nonLangField].state
												}
											}
											if (nonLangFields.indexOf('category') > ~0) {
												$http.post(
													$interpolate(actionToUrlMask['Goods.setCategories'])({ id: $scope.goodRefId }), 
													angular.toJson(categoryUpdateFields)
												)
												.then(
													function(response) {
														save(fields)
													},
													function(reason) {
														reasons.push(reason)
														save(fields)
													}
												)
											}
											else {
												save(fields)
											}
										}
										else {
											save(fields)
										}
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
					init: function(goodData, manufacturers, categories) {
						if (goodData.data && goodData.status >= 200 && goodData.status <= 299) {
							
							var dataGood = goodData.data.good 
							var dataData = goodData.data.data
							var va = $scope.Validation

							va.warranty.value = this.dataMap.warranty = String(dataGood.warranty || 0)
							va.cost.value = this.dataMap.cost = String(dataGood.cost || 0)
							va.slug.value = this.dataMap.slug = dataGood.slug || ''
							va.newg.state = this.dataMap.newg = dataGood.newg
							va.import_short_desc.state = this.dataMap.import_short_desc = dataGood.import_short_desc
							va.top.state = this.dataMap.top = dataGood.top
							va.partnumber.value = this.dataMap.partnumber = dataGood.partnumber || ''

							angular.forEach($scope.TabLangManager.langList, function(lang, key) {
								
								var names = ['title', 'description', 'descriptionShort', 'metatitle', 'metadescription']
								var capitalized = lang.charAt(0).toUpperCase() + lang.slice(1)
								
								if (angular.isObject($scope.GoodUpdateManager.dataMap[lang]) == false) {
									$scope.GoodUpdateManager.dataMap[lang] = {}
								}
								angular.forEach(names, function(value, key) {
									if(dataData[lang]) {
										va[value + capitalized].value = dataData[lang][value] || ''
										$scope.GoodUpdateManager.dataMap[lang][value] = dataData[lang][value] || ''
									}
									else {
										$scope.GoodUpdateManager.dataMap[lang][value] = ''
									}
								})
							})

							$scope.select = {
								disabled: false,
								manufacturers: [{ id: 0, name: translations.GOOD_UPDATE_NO }],
								categories: []
							}
							
							this.dataMap.manufacturer = va.manufacturer.value = $scope.select.manufacturers[0]
							this.dataMap.category = []
							
							if (angular.isArray(categories)) {
								angular.forEach(categories, function(category, key) {
									var pathtoroot = angular.fromJson(category.pathtoroot)
									var pathPrepared = ''
									if (pathtoroot.length > 0) {
										angular.forEach(pathtoroot, function(path, key) {
											pathPrepared = pathPrepared + path.title + ((Number(key) == pathtoroot.length - 1) ? ' / ' + category.title : ' / ')
										})
									}
									else {
										pathPrepared = '/'
									}
									$scope.select.categories.push({
										id: category.id,
										name: category.title,
										path: '<h6>' + pathPrepared + '</h6>',
										pathFull: pathPrepared == '/' ? category.title : pathPrepared
									})
								})
							}

							if (angular.isArray(goodData.data.categories)) {
								angular.forEach(goodData.data.categories, function(category, key) {
									for(var i = 0; i < $scope.select.categories.length; ++i) {
										var icat = $scope.select.categories[i]
										if (icat.id == category.id) {
											this.dataMap.category.push(icat)
											va.category.value.push(icat)
											break
										}
									}
								}, this)
							}

							if (angular.isArray(manufacturers)) {
								angular.forEach(manufacturers, function(manufacturer, key) {
									var parsedManufacturer = {
										id: manufacturer[0],
										name: manufacturer[1]
									}
									$scope.select.manufacturers.push(parsedManufacturer)
									if (parsedManufacturer.id == dataGood.manufacturer) {
										this.dataMap.manufacturer = va.manufacturer.value = parsedManufacturer
									}
								}, this)
							}
						}
						else {
							$window.swal(
								translations.GOOD_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TITLE, 
								$interpolate(translations.GOOD_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TEXT)({
									id: $scope.goodRefId
								}),
								'error'
							)
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
								else if (vaName = vcg[6]) {
									if (boolOrGetMode) {
										if (this.dataMap[vaName].length != va[vaName].value.length) {
											diffMap[vaName] = va[vaName].value
											diffList.push(diffMap)
										}
										else {
											for (var j = 0; j < this.dataMap[vaName].length; ++j) {
												var contains = false
												for (var k = 0; k < va[vaName].value.length; ++k) {
													if (this.dataMap[vaName][j].id == va[vaName].value[k].id) {
														contains = true
														break
													}
												}
												if (!contains) {
													diffMap[vaName] = va[vaName].value
													diffList.push(diffMap)
													break
												}
											}
										}
									}
									else {
										this.dataMap[vaName] = angular.copy(va[vaName].value)
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

				$scope.Summernote = {
					config: {
						lang: 'ru-RU',
						height: 300,
						minHeight: 300,
						maxHeight: 1200
					}
				}

				$scope.GoodUpdateManager.init($scope.goodData, $scope.manufacturers, $scope.categories)

			})
		}
	])

})(window, window.angular, void 0);
