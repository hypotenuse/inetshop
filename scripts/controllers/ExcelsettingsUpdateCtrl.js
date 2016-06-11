;
(function(window, angular, undefined) {

	angular.module('homer')

	.controller('ExcelsettingsUpdateCtrl', ['$window', '$scope', '$interpolate', '$log', 
		'$timeout', '$http', '$modal', '$q', '$translate', 'actionToUrlMask', '$state', '$location',
		function($window, $scope, $interpolate, $log, $timeout, $http, $modal, $q, $translate, actionToUrlMask, $state, $location) {

			$translate([
					'VALIDATION_EMPTY_ERROR',
					'VALIDATION_MINLENGTH_ERROR',
					'NOTIFICATION_ERROR_TITLE',
					'NOTIFICATION_ERROR_REASON_TEXT',
					'EXCELSETTINGS_VALIDATION_ERROR_TEXT1',
					'EXCELSETTINGS_VALIDATION_ERROR_TEXT2',
					'EXCELSETTINGS_VALIDATION_ERROR_TEXT3',
					'EXCELSETTINGS_VALIDATION_ERROR_TEXT4',
					'EXCELSETTINGS_VALIDATION_ERROR_TEXT5',
					'EXCELSETTINGS_VALIDATION_ERROR_TEXT6',
					'EXCELSETTINGS_VALIDATION_ERROR_TEXT7',
					'EXCELSETTINGS_VALIDATION_ERROR_TEXT8',
					'EXCELSETTINGS_VALIDATION_ERROR_TEXT9'
				])
				.then(function(translations) {

					var maxNumberRE = /^(?:[0-9]|[1-9][0-9])$/
					var commaNumsRE = /^(?:\d+\,)*\d+$/

					var fields = [
						'supplier', 'titleColumn', 'sheetNumber', 'title', 'costColumn', 'costCurrencyId', 'costRrzCurrencyId', 'costRrzColumn', 'partnumberColumn', 'warrantyColumn', 'brandColumn', 
						'goodidSupplierColumn', 'categoryColumns', 'descriptionColumn', 'imageUrlColumn', 'warrantyInMonths', 'modelColumn', 'amountColumn', 'currencySignColumn'
					]

					var stringify = function(arg) {
						return angular.isNumber(arg) ? String(arg) : String()
					}

					$scope.TabManager = {
						update: function(event) {
							var state = event.currentTarget.children[0].hash
							$scope.TabManager.state = state
						},
						init: function() {
							var statesRE = /^\#tab\-(?:description)$/i, state
							if (state === window.location.hash.match(statesRE)) {
								$scope.TabManager.state = state
							} else {
								$scope.TabManager.state = '#tab-description'
							}
						}
					}

					$scope.TabManager.init()

					$scope.Validation = { errorStack: [] }

					$scope.Validation.currencySign = { 
						value: '', 
						invalidMsg: false,
						disabled: function() {
							return !('id' in $scope.Validation.costCurrencyId.value) || this.value.length == 0
						} 
					}

					$scope.Validation.save = {
						disabled: function() {
							return $scope.Validation.errorStack.length > 0 || (!('id' in $scope.Validation.costCurrencyId.value) && $scope.currencyMaps.length == 0)
						}
					}

					angular.forEach(fields, function(value, key, list) {

							var validatorName = value

							switch (validatorName) {

								case 'title':
									$scope.Validation[validatorName] = {
										value: '',
										invalidMsg: false,
										validate: function() {
											if (this.value.length == 0) {
												this.invalidMsg = translations.VALIDATION_EMPTY_ERROR
											}
											else if (this.value.length < 2) {
												this.invalidMsg = translations.VALIDATION_MINLENGTH_ERROR
											}
											else {
												this.invalidMsg = false
											}
										}
									}
									break;
								
								case 'costCurrencyId':
									$scope.Validation[validatorName] = {
										value: {},
										disabled: false,
										invalidMsg: false,
										validate: function() {
											if (!('id' in this.value) && $scope.currencyMaps.length == 0) {
												this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT1
											}
											else {
												this.invalidMsg = false
											}
										}
									}
									break;

								case 'warrantyInMonths':
									$scope.Validation[validatorName] = {
										value: false
									}
									break;

								case 'supplier':
									$scope.Validation[validatorName] = {
										value: {},
										disabled: false,
										invalidMsg: false,
										validate: function() {
											if (!('title' in this.value)) {
												this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT2
											}
											else {
												this.invalidMsg = false
											}
										}
									}
									break;

								case 'titleColumn':
								case 'sheetNumber':
								case 'costColumn':
								case 'amountColumn':
									$scope.Validation[validatorName] = {
										value: '',
										invalidMsg: false,
										validate: function() {
											if (this.value.length == 0) {
												this.invalidMsg = translations.VALIDATION_EMPTY_ERROR
											}
											else if (maxNumberRE.test(this.value) == false) {
												this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT3
											}
											else {
												this.invalidMsg = false
											}
										}
									}
									break;

								case 'partnumberColumn':
								case 'warrantyColumn':
								case 'brandColumn': 
								case 'goodidSupplierColumn':
								case 'descriptionColumn': 
								case 'imageUrlColumn':
								case 'modelColumn':
									$scope.Validation[validatorName] = {
										value: '',
										invalidMsg: false,
										validate: function() {
											if (this.value.length == 0) {
												this.invalidMsg = false
											}
											else if (this.value.length > 0) {
												if (maxNumberRE.test(this.value) == false) {
													this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT3
												}
												else {
													this.invalidMsg = false
												}
											}
										}
									}
									break;

								case 'costRrzCurrencyId':
									$scope.Validation[validatorName] = {
										value: {},
										invalidMsg: false,
										validate: function(fromValidate) {
											var costRrzColumn = $scope.Validation.costRrzColumn.value
											var costRrzCurrencyId = 'id' in this.value
											if (costRrzCurrencyId && costRrzColumn.length == 0) {
												this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT4
											}
											else if (!costRrzCurrencyId && costRrzColumn.length > 0) {
												this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT5
											}
											else {
												this.invalidMsg = false
											}
											if (!fromValidate) {
												$scope.Validation.costRrzColumn.validate(true)
											}
										}
									}
									break;

								case 'costRrzColumn':
									$scope.Validation[validatorName] = {
										value: '',
										invalidMsg: false,
										validate: function(fromValidate) {
											var costRrzCurrencyId = 'id' in $scope.Validation.costRrzCurrencyId.value
											if (this.value.length > 0 && !costRrzCurrencyId) {
												this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT6
											}
											else if (this.value.length == 0 && costRrzCurrencyId) {
												this.invalidMsg = translations.VALIDATION_EMPTY_ERROR
											}
											else {
												if (this.value.length == 0) {
													this.invalidMsg = false
												}
												else {
													if (maxNumberRE.test(this.value) == false) {
														this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT3
													}
													else {
														this.invalidMsg = false
													}
												}
											}
											if (!fromValidate) { 
												$scope.Validation.costRrzCurrencyId.validate(true)
											}
										}
									}
									break;

								case 'currencySignColumn':
									$scope.Validation[validatorName] = {
										value: '',
										invalidMsg: false,
										validate: function() {
											if (this.value.length == 0 && $scope.currencyMaps.length > 0) {
												this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT7
											}
											else if (this.value.length > 0 && $scope.currencyMaps.length == 0) {
												this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT8
											}
											else {
												if (this.value.length == 0) {
													this.invalidMsg = false
												}
												else {
													if (maxNumberRE.test(this.value) == false) {
														this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT3
													}
													else {
														this.invalidMsg = false
													}
												}
											}
										}
									}
									break;

								case 'categoryColumns':
									$scope.Validation[validatorName] = {
										value: '',
										invalidMsg: false,
										validate: function() {
											if (this.value.length == 0) {
												this.invalidMsg = false
											}
											else if (this.value.length > 0) {
												if (commaNumsRE.test(this.value) == false) {
													this.invalidMsg = translations.EXCELSETTINGS_VALIDATION_ERROR_TEXT9
												}
												else {
													this.invalidMsg = false
												}
											}
										}
									}
									break;
							}
						})


					angular.forEach($scope.Validation, function(value, key, list) {
						var validateOld = value.validate
						if (validateOld) {
							value.validate = function(fromValidate) {
								
								validateOld.apply(value, arguments)

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
							}
						}
					})

					$scope.ExcelsettingsUpdateManager = {
						init: function(suppliers, currencies, excelsettings) {
							this.excelsettings = excelsettings
							var validation = $scope.Validation, costRrzCurrencies
							$scope.currencyMaps = []
							$scope.select = {}
							$scope.select.costRrzCurrencies = costRrzCurrencies = angular.copy(currencies)
							$scope.select.suppliers = suppliers
							$scope.select.currencies = currencies
							if (angular.equals(excelsettings, undefined)) {
								validation.supplier.validate()
								validation.title.validate()
								validation.titleColumn.validate()
								validation.sheetNumber.validate()
								validation.costColumn.validate()
								validation.amountColumn.validate()
							}
							else {
								validation.title.value = excelsettings.title
								validation.warrantyInMonths.value = excelsettings.warrantyInMonths
								validation.titleColumn.value = stringify(excelsettings.titleColumn)
								validation.sheetNumber.value = stringify(excelsettings.sheetNumber)
								validation.costColumn.value = stringify(excelsettings.costColumn)
								validation.costRrzColumn.value = stringify(excelsettings.costRrzColumn)
								validation.partnumberColumn.value = stringify(excelsettings.partnumberColumn)
								validation.warrantyColumn.value = stringify(excelsettings.warrantyColumn)
								validation.brandColumn.value = stringify(excelsettings.brandColumn)
								validation.goodidSupplierColumn.value = stringify(excelsettings.goodidSupplierColumn)
								validation.descriptionColumn.value = stringify(excelsettings.descriptionColumn)
								validation.imageUrlColumn.value = stringify(excelsettings.imageUrlColumn)
								validation.modelColumn.value = stringify(excelsettings.modelColumn)
								validation.amountColumn.value = stringify(excelsettings.amountColumn)
								validation.currencySignColumn.value = stringify(excelsettings.currencySignColumn)

								// Инициализация supplier
								if (angular.isArray(suppliers)) {
									for (var k = 0; k < suppliers.length; ++k) {
										if (suppliers[k].id == excelsettings.supplier) {
											validation.supplier.value = suppliers[k]
											break
										}
									}
								}

								// Инициализация categoryColumns
								if (angular.isArray(excelsettings.categoryColumns)) {
									var categories = []
									angular.forEach(excelsettings.categoryColumns, function(category, key) {
										categories.push(category.column)
									})
									validation.categoryColumns.value = categories.join(',')
								}

								// Инициализация costRrzCurrencyId
								for(var k = 0; k < costRrzCurrencies.length; ++k) {
									if (costRrzCurrencies[k].id == excelsettings.costRrzCurrencyId) {
										validation.costRrzCurrencyId.value = costRrzCurrencies[k]
										break
									}
								}

								// Инициализация currencyMaps
								if (angular.isArray(excelsettings.currencyIdSign)) {
									angular.forEach(excelsettings.currencyIdSign, function(currency, key) {
										for(var k = 0; k < currencies.length; ++k) {
											if (currencies[k].id == currency.currency) {
												$scope.currencyMaps.push({
													id: currency.currency,
													sign: currency.sign,
													title: currencies[k].title
												})
												currencies.splice(k, 1)
												break
											}
										}
									})
								}
								else if (angular.isNumber(excelsettings.costCurrencyId)) {
									for (var k = 0; k < currencies.length; ++k) {
										if (currencies[k].id == excelsettings.costCurrencyId) {
											validation.costCurrencyId.value = currencies[k]
											break
										}
									}
								}
							}
							validation.currencySignColumn.validate()
							validation.costCurrencyId.validate()
							validation.costRrzColumn.validate(true)
							validation.costRrzCurrencyId.validate(true)
						},
						cancel: function() {
							$location.path('/admin/excelsettings')
						},
						save: function() {
							var self = this
							var spinner = angular.element('.spinner')
							var requestData = {}
							var validation = $scope.Validation

							angular.forEach(fields, function(field, key) {
								var value = validation[field].value
								if (angular.isObject(value)) {
									requestData[field] = Number(value.id)
								}
								else if (angular.equals(value, true) || angular.equals(value, false)) {
									requestData[field] = validation[field].value
								}
								else if (angular.isString(value)) {
									if (value.length == 0) {
										requestData[field] = null
									}
									else {
										if (angular.equals(field, 'title')) {
											requestData[field] = value
										}
										else if (angular.equals(field, 'categoryColumns')) {
											var numbers = value.split(',')
											angular.forEach(numbers, function(number, key) {
												numbers[key] = { 'column': Number(number) }
											})
											requestData[field] = numbers
										}
										else if (maxNumberRE.test(value)) {
											requestData[field] = Number(value)
										}
									}
								}
							})

							// Задан список соответствия валют (Удаляем возможный costCurrencyId и отсылаем currencyIdSigns)
							if ($scope.currencyMaps.length > 0) {
								if ('costCurrencyId' in requestData) {
									delete requestData.costCurrencyId
								}
								requestData.currencyIdSigns = []
								angular.forEach($scope.currencyMaps, function(currencyMap, key) {
									requestData.currencyIdSigns.push({
										'sign': currencyMap.sign,
										'currencyId': Number(currencyMap.id)
									})
								})
							}

							requestData = angular.toJson(requestData)
							spinner.addClass('show')

							if (angular.equals(this.excelsettings, undefined)) {
								$http.post(actionToUrlMask['ExcelSettings.add'], requestData).then(function() {
									spinner.removeClass('show')
									self.cancel()
								})
								.catch(function() {
									spinner.removeClass('show')
									window.swal(
										translations.NOTIFICATION_ERROR_TITLE,
										$interpolate(translations.NOTIFICATION_ERROR_REASON_TEXT)({
											status: data.status,
											statusText: data.statusText
										}),
										'error'
									)
								})
							}
							else {
								$http.put($interpolate(actionToUrlMask['ExcelSettings.update'])({ id: this.excelsettings.id }), requestData).then(function() {
									spinner.removeClass('show')
									self.cancel()
								})
								.catch(function(data) {
									spinner.removeClass('show')
									window.swal(
										translations.NOTIFICATION_ERROR_TITLE,
										$interpolate(translations.NOTIFICATION_ERROR_REASON_TEXT)({
											status: data.status,
											statusText: data.statusText
										}),
										'error'
									)
								})
							}

						},
						add: function() {
							var costCurrencyId = $scope.Validation.costCurrencyId
							var currencySign = $scope.Validation.currencySign
							var currencies = $scope.select.currencies
							var currencySignColumn = $scope.Validation.currencySignColumn
							$scope.currencyMaps.push({
								id: costCurrencyId.value.id,
								title: costCurrencyId.value.title,
								sign: currencySign.value
							})
							for (var i = 0; i < currencies.length; ++i) {
								if (currencies[i].id == costCurrencyId.value.id) {
									currencies.splice(i, 1)
									break
								}
							}
							currencySign.value = String()
							costCurrencyId.value = {}
							currencySignColumn.validate()
							costCurrencyId.validate()
						},
						delete: function(id) {
							var currencySignColumn = $scope.Validation.currencySignColumn
							var costCurrencyId = $scope.Validation.costCurrencyId
							var currencies = $scope.select.currencies
							var currencyMaps = $scope.currencyMaps
							for (var i = 0; i < currencyMaps.length; ++i) {
								if (currencyMaps[i].id == id) {
									$scope.currencies.push(currencyMaps[i])
									currencyMaps.splice(i, 1)
									break
								}
							}
							currencySignColumn.validate()
							costCurrencyId.validate()
						}
					}

					$scope.ExcelsettingsUpdateManager.init($scope.suppliers, $scope.currencies, $scope.excelsettings)

				})

		}
	])

})(window, window.angular, void 0);
