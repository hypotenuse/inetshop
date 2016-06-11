(function(window, angular, undefined) {

	angular.module('homer')

		.controller('SaleUpdateCtrl', ['$window', '$timeout', '$interpolate', '$scope', '$http', '$modal', '$q', '$translate', 'actionToUrlMask',
			'DTOptionsBuilder', 'DTColumnBuilder', '$compile',
		function($window, $timeout, $interpolate, $scope, $http, $modal, $q, $translate, actionToUrlMask, DTOptionsBuilder, DTColumnBuilder, $compile) {

			$scope.SaleGoodsManager = {
				init: function(currentSale, translations, manufacturers, categories) {
					
					var itemNo = { name: translations.GOODS_LIST_SELECTBOX_ITEM_NO }
					
					var requestDelay = 400

					var saleTypes = ['inSale', 'notInSale']

					var updateDatatable = function(datatableInstance) {
						datatableInstance.reloadData()
					}

					var isNull = function(arg) {
						return angular.equals(arg, null)
					}

					var empty = function(arg) {
						return angular.equals(arg, '')
					}

					var allowedNumber = function(arg) {
						return /(?:^\-?\d{1,7}$)|(?:^$)/.test(arg)
					}

					this.cost = {}
					this.select = {}

					angular.forEach(saleTypes, function(type) {
						this.select[type] = {}
						angular.forEach(['manufacturers', 'categories'], function(selectboxType) {
							var model = angular.copy(itemNo)
							this.select[type][selectboxType] = {}
							this.select[type][selectboxType].list = [model]
							this.select[type][selectboxType].model = model
							this.select[type][selectboxType].disabled = false
							this.select[type][selectboxType].change = function() {
								updateDatatable($scope.SaleGoodsManager.datatable[type].instance)
							}
						}, this)
					}, this)

					if (angular.isArray(manufacturers)) {
						angular.forEach(saleTypes, function(type) {
							angular.forEach(manufacturers, function(manufacturer, key) {
								this.select[type].manufacturers.list.push({
									id: manufacturer[0],
									name: manufacturer[1]
								})
							}, this)
						}, this)
					}

					if (angular.isArray(categories)) {
						angular.forEach(saleTypes, function(type) {
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
								this.select[type].categories.list.push({
									id: category.id,
									name: category.title,
									path: '<h6>' + pathPrepared + '</h6>'
								})
							}, this)
						}, this)
					}

					this.cost.costChange = function(saleType) {
						$timeout.cancel(this.promise)
						if(allowedNumber(this.model)) {
							this.promise = $timeout(function() {
								updateDatatable($scope.SaleGoodsManager.datatable[saleType].instance)
							}, requestDelay)
						}
					}

					angular.forEach(saleTypes, function(type) {
						this.cost[type] = {}
						angular.forEach(['from', 'to'], function(rangeType) {
							this.cost[type][rangeType] = {}
							this.cost[type][rangeType].model = ''
							this.cost[type][rangeType].change = function() {
								$scope.SaleGoodsManager.cost.costChange.call(this, type)
							}
						}, this)
					}, this)

					this.datatable = {
						utils: {
							translate: function(options) {
								if ($translate.use() == 'ru_RU' || $translate.proposedLanguage() == 'ru_RU') {
									options.withLanguageSource('/assets/admin/scripts/controllers/locale-ru_RU.json')
								}
							},
							compileCreatedRow: function(options) {
								options.withOption('createdRow', function(row, data, dataIndex) {
									$compile(angular.element(row).contents())($scope)
								})
							},
							getFilterParameters: function(type) {

								var manufacturerId = $scope.SaleGoodsManager.select[type].manufacturers.model.id
								var costfrom = $scope.SaleGoodsManager.cost[type].from.model
								var costto = $scope.SaleGoodsManager.cost[type].to.model
								var categoryId = $scope.SaleGoodsManager.select[type].categories.model.id
								
								var params = {}

								if (angular.isDefined(categoryId)) {
									params.category = categoryId
								}
								if (angular.isDefined(manufacturerId)) {
									params.manufacturer = manufacturerId
								}
								if (allowedNumber(costfrom) && !empty(costfrom)) {
									params.costfrom = Number(costfrom)
								}
								if (allowedNumber(costto) && !empty(costto)) {
									params.costto = Number(costto)
								}
								return params
							},
							getColumns: function() {
								var columns = [
									DTColumnBuilder.newColumn('0').withTitle(translations.GOODS_LIST_NAME).renderWith(function(data, type, full) {
										return $interpolate('<a href="/admin/goods/{{id}}">{{title}}</a>')({
											id: full[0],
											title: full[1]
										}) 
									})
									.withClass('datatable-title'),

									DTColumnBuilder.newColumn('1').withTitle(translations.GOODS_LIST_PARTNUMBER).renderWith(function(data, type, full) {
										return isNull(full[2]) ? translations.GOODS_LIST_NO : full[2]
									})
									.withClass('datatable-title')
									.notSortable(),

									DTColumnBuilder.newColumn('2').withTitle(translations.GOODS_LIST_COST).renderWith(function(data, type, full) {
										return angular.equals(full[5], 0) ? translations.GOODS_LIST_NO : full[5]
									})
									.withClass('datatable-title')
									.notSortable(),

									DTColumnBuilder.newColumn(null).withTitle('').notSortable().withClass('select-checkbox').renderWith(function() {
										return ''
									})
								]
								return columns
							},
							initSearchFilter: function(id, instance) {
								
								var searchFilterSelector = $interpolate('div{{id}}.dataTables_filter input')({ id: id })
								var searchFilter = angular.element(searchFilterSelector)
								var promise = null

								searchFilter.off('keyup.DT search.DT input.DT paste.DT cut.DT')
								searchFilter.on('input', function() {
									var searchFilterValue = searchFilter.val()
									$timeout.cancel(promise)
									promise = $timeout(function() {
										instance.DataTable.search(searchFilterValue)
										updateDatatable(instance)
									}, requestDelay)
								})
							},
							init: function(self, filterId, initMethod, tableType) {

								var utils = $scope.SaleGoodsManager.datatable.utils
								
								self.itemsSelected = []
								self.instance = {}
								self.options = DTOptionsBuilder.newOptions()

								utils.translate(self.options)
								utils.compileCreatedRow(self.options)

								self.options.withSelect({
									style: 'multi',
									selector: 'td:last-child'
								})
								self.options.withOption('fnInitComplete', function() {
									
									var dataTableInstance = self.instance.DataTable
									utils.initSearchFilter(filterId, self.instance)

									dataTableInstance.on('select', function(event, instance, type, indexes) {
										$scope.$apply(function() {
											if (type == 'row') {
												var itemSelected = dataTableInstance.rows(indexes).data()[0][0]
												if (self.itemsSelected.indexOf(itemSelected) == -1) {
													self.itemsSelected.push(itemSelected)
												}
											}
										})
									})
									dataTableInstance.on('deselect', function(event, instance, type, indexes) {
										$scope.$apply(function() {
											if (type == 'row') {
												var itemDeselected = dataTableInstance.rows(indexes).data()[0][0]
												var itemIndex = self.itemsSelected.indexOf(itemDeselected)
												if (itemIndex > -1) {
													self.itemsSelected.splice(itemIndex, 1)
												}
											}
										})
									})
								})
								self.options.withOption('drawCallback', function() {

									var eq = ':eq({{index}})'
									var api = this.api()
									var currentItems = api.rows({page: 'current'}).data()

									if (self.itemsSelected.length > 0) {
										angular.forEach(currentItems, function(item, key) {
											if (self.itemsSelected.indexOf(item[0]) > -1) {
												api.row($interpolate(eq)({index: key}), {page: 'current'}).select()
											}
										})
									}
								})

								self.options.withOption('ajax', {
									type: 'GET',
									url: $interpolate(actionToUrlMask[initMethod])({
										id: currentSale.data.sale.id
									}),
									data: function(params) {
										angular.extend(params, utils.getFilterParameters(tableType))
									}
								})

								self.options.withOption('autoWidth', false)
								self.options.withOption('processing', true)
								self.options.withOption('serverSide', true)
								self.options.withDataProp('data')
								self.options.withPaginationType('full_numbers')

								self.columns = utils.getColumns()
							}
						},
						inSale: {
							init: function() {
								$scope.SaleGoodsManager.datatable.utils.init(this, '#inSale_filter', 'Sales.inSaleGoodsList', 'inSale')
							}
						},
						notInSale: {
							init: function() {
								$scope.SaleGoodsManager.datatable.utils.init(this, '#notInSale_filter', 'Sales.notInSaleGoodsList', 'notInSale')
							}
						}
					}

					this.message = {
						duration: 10400,
						inSale: {
							promise: null,
							show: false,
							notificate: function(type) {
								var self = this

								if (angular.isString(type)) {
									
									$timeout.cancel(this.promise)
									this.notificate(false)

									this.type = type
									if (angular.equals(type, 'add')) {
										this.message = translations.SALE_UPDATE_GOODS_ADDED
									}
									else if (angular.equals(type, 'remove')) {
										this.message = translations.SALE_UPDATE_GOODS_REMOVED
									}
									else if (angular.equals(type, 'error')) {
										this.message = translations.SALE_UPDATE_GOODS_NOT_ADDED
									}
									this.promise = $timeout(function() {
										self.notificate(false)
									},
									$scope.SaleGoodsManager.message.duration)
									this.show = true
								}
								else if (angular.equals(type, false)) {
									delete this.message
									delete this.type
									this.show = false
								}
							}
						} 
					}

					this.action = {
						success: function(type, selectedGoods) {
							$scope.SaleGoodsManager.message.inSale.notificate(type)
							angular.element('.spinner').removeClass('show')
							while(selectedGoods.length) {
								selectedGoods.shift()
							}
							updateDatatable($scope.SaleGoodsManager.datatable.inSale.instance)
							updateDatatable($scope.SaleGoodsManager.datatable.notInSale.instance)
						},
						failed: function() {
							$scope.SaleGoodsManager.message.inSale.notificate('error')
							angular.element('.spinner').removeClass('show')
						},
						addGoods: function() {
							var self = this
							var selectedGoods = $scope.SaleGoodsManager.datatable.notInSale.itemsSelected
							var data = angular.toJson({ goods: selectedGoods })
							angular.element('.spinner').addClass('show')
							$http.post($interpolate(actionToUrlMask['Sales.addGoods'])({ id: currentSale.data.sale.id }), data)
							.then(
								function() {
									self.success('add', selectedGoods)
								},
								this.failed
							)
						},
						removeGoods: function() {
							var self = this
							var selectedGoods = $scope.SaleGoodsManager.datatable.inSale.itemsSelected
							var data = { goods: selectedGoods }
							angular.element('.spinner').addClass('show')
							$http.delete($interpolate(actionToUrlMask['Sales.removeGoods'])({ id: currentSale.data.sale.id }), { 
								data: data,
								headers: {
									'Content-Type': 'application/json'
								} 
							})
							.then(
								function() {
									self.success('remove', selectedGoods)
								},
								this.failed
							)
						}
					}

					this.datatable.inSale.init()
					this.datatable.notInSale.init()

				}
			}

			$scope.SaleGoodsManager.init($scope.currentSale, $scope.translations, $scope.manufacturers, $scope.categories)

			$translate([
					'SALE_UPDATE_VALIDATION_EXCEED_ERROR',
					'SALE_UPDATE_VALIDATION_EMPTY_ERROR',
					'SALE_UPDATE_VALIDATION_MINLENGTH_ERROR',
					'SALE_UPDATE_VALIDATION_UNSUPPORTED_CHARACTERS_ERROR',
					'SALE_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE',
					'SALE_UPDATE_NOTIFICATION_ERROR_INIT_ERROR_TITLE',
					'SALE_UPDATE_NOTIFICATION_ERROR_PARENT_EQUAL_TO_ITSELF_TEXT',
					'SALE_UPDATE_NOTIFICATION_ERROR_PARENT_EQUAL_TO_IT_CHILD_TEXT',
					'SALE_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TEXT',
					'SALE_UPDATE_NOTIFICATION_ERROR_NO_LIST_TEXT',
					'SALE_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT',
					'SALE_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT',
					'SALE_UPDATE_NOTIFICATION_ERROR_REASON_TEXT',
					'SALE_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE',
					'SALE_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT',
					'SALE_UPDATE_SELECTBOX_NO_PARENT'
				])
				.then(function(translations) {

					function lengthExceeded(validationObject) {
						return validationObject.value.length > 250 ?
							$interpolate(translations.SALE_UPDATE_VALIDATION_EXCEED_ERROR)({
								exceed: validationObject.value.length - 250
							}) : false;
					}

					$scope.TabLangManager = {
						langList: ['ru', 'en', 'uk'],
						update: function(event) {
							angular.forEach(this, function(value, key) {
								if (this.langList.indexOf(key) > -1) {
									delete this[key];
								}
							}, this);
							this[event.currentTarget.innerHTML.toLowerCase()] = true;
						}
					};

					$scope.TabLangManager.ru = true;

					$scope.TabManager = {
						update: function(event) {
							var state = event.currentTarget.children[0].hash;
							$scope.TabManager.state = state;
						},
						init: function() {
							var statesRE = /^\#tab\-(?:params|description|goods\-in\-sales)$/i,
								state;
							if (state = window.location.hash.match(statesRE)) {
								$scope.TabManager.state = state;
							} else {
								$scope.TabManager.state = '#tab-params';
							}
						}
					};

					$scope.TabManager.init();

					$scope.Validation = {
						errorStack: [],
						changes: false,
						validatorNameRE: /^(?:(title|text)(Ru|En|Uk))|(titlecolorbackgrnd)$/
					};

					$scope.onColorChange = function(color) {
						console.log("color change : " + color);
						$scope.colorbackground = color;
						$scope.Validation.changes = true;
					};

					angular.forEach($scope.TabLangManager.langList, function(value, key, list) {
						var capitalized = value.charAt(0).toUpperCase() + value.slice(1);

						angular.forEach(['title', 'text'], function(value, key, list) {
							var validatorName = value + capitalized;
							if (value == 'text') {
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
									$scope.Validation.changes = $scope.SaleUpdateManager.dataMapDiff('bool');
								}
							};
						}
					});

					$scope.SaleUpdateManager = {
						save: function() {

							var spinner = angular.element('.spinner');
							var langRequests = [];
							var nonLangFields = [];
							var mapKeys = [];
							var reasons = [];
							var dataMapDiff = $scope.SaleUpdateManager.dataMapDiff('get');
							var requests = successes = 0;
							var vcg = undefined;
							var va = $scope.Validation;
							var saleUpdateUrl = $interpolate(actionToUrlMask['Sales.update'])({
								id: this.currentSale.data.sale.id
							});

							var errorNotifier = function(reasons) {
								var errors = [];
								var errorTitle = translations.SALE_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE;

								if (reasons[0].data == 'TITLE_IS_REQUIRED') {
									errors.push(
										$interpolate(translations.SALE_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT)({
											lang: reasons[0].config.data.match(/\"languagecod\":\"(.+?)\"/)[1].toUpperCase()
										})
									);
								} else {
									for (var i = 0; i < reasons.length; ++i) {
										var titleErrors = reasons[i].data['obj.title'];

										if (titleErrors) {
											var lang = reasons[i].config.data.match(/\"languagecod\":\"(.+?)\"/)[1].toUpperCase();
											if (titleErrors[0].msg == 'error.required') {
												errors.push(
													$interpolate(translations.SALE_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT)({
														lang: lang
													})
												);
											} else if (angular.isArray(titleErrors[0].msg) && titleErrors[0].msg[0] == 'error.minLength') {
												errors.push(
													$interpolate(translations.SALE_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT)({
														lang: lang
													})
												);
											}
										} else {
											errors.push(
												$interpolate(translations.SALE_UPDATE_NOTIFICATION_ERROR_REASON_TEXT)({
													status: reasons[i].status,
													statusText: reasons[i].statusText
												})
											);
										}
										// }
									}
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
									$scope.Validation.changes = false;
									// $scope.SaleUpdateManager.dataMapDiff('update')
									// $state.go($state.$current, null, {
									// 	reload: true
									// });
									$window.swal(
										translations.SALE_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE,
										translations.SALE_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT,
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
														} else if (nonLangField == 'titlecolorbackgrnd') {
															fields[nonLangField] = va[nonLangField].value
														} else {
															fields[nonLangField] = 'value' in va[nonLangField] ? va[nonLangField].value : va[nonLangField].state
														}
													}
												}
												console.log(" $scope.titlecolorbackgrnd : " + $scope.colorbackground);
												fields['titlecolorbackgrnd'] = $scope.colorbackground;
												console.log("field : " + angular.toJson(fields));
												$http.put(saleUpdateUrl, angular.toJson(fields))
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
						init: function(currentSale) {
							var statusOk = function(status) {
								return status >= 200 && status <= 299
							}
							var va = $scope.Validation
							this.currentSale = currentSale
							va.titlecolorbackgrnd = this.dataMap.titlecolorbackgrnd = currentSale.data.sale.titlecolorbackgrnd || ''
							$scope.titlecolorbackgrnd = currentSale.data.sale.titlecolorbackgrnd
							angular.forEach($scope.TabLangManager.langList, function(lang, key) {

								var names = ['title', 'text']
								var capitalized = lang.charAt(0).toUpperCase() + lang.slice(1)

								if (angular.isObject($scope.SaleUpdateManager.dataMap[lang]) == false) {
									$scope.SaleUpdateManager.dataMap[lang] = {}
								}
								angular.forEach(names, function(value, key) {
									if (currentSale.data.data[lang]) {
										$scope.SaleUpdateManager.dataMap[lang][value] = va[value + capitalized].value = currentSale.data.data[
											lang][value] || ''
									} else {
										$scope.SaleUpdateManager.dataMap[lang][value] = ''
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
										if (angular.equals(va[vcg[0]].value, this.dataMap[vcg[2].toLowerCase()][vcg[1]]) == false) {
											if (boolOrGetMode) {
												diffMap[vcg[0]] = va[vcg[0]].value
												diffList.push(diffMap)
											} else {
												this.dataMap[vcg[2].toLowerCase()][vcg[1]] = va[vcg[0]].value
											}
										}
									} else if (vaName = vcg[3] || vcg[5]) {
										if (angular.equals(va[vaName].value, this.dataMap[vaName]) == false) {
											if (boolOrGetMode) {
												diffMap[vaName] = va[vaName].value
												diffList.push(diffMap)
											} else {
												this.dataMap[vaName] = va[vaName].value
											}
										}
									} else if (vaName = vcg[4]) {
										if (angular.equals(va[vaName].state, this.dataMap[vaName]) == false) {
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

					$scope.SaleUpdateManager.init($scope.currentSale)

				})

		}
	])


})(window, window.angular, void 0);
