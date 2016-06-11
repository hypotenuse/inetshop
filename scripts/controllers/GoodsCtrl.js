
;
(function(window, angular, undefined) {

	angular.module('homer')

		.controller('GoodsCtrl', ['$scope', '$compile', '$window', '$timeout', '$log', '$http', '$modal', '$location', '$translate', '$interpolate', 'actionToUrlMask',
			'DTOptionsBuilder', 'DTColumnBuilder',
		function($scope, $compile, $window, $timeout, $log, $http, $modal, $location, $translate, $interpolate, actionToUrlMask, DTOptionsBuilder, DTColumnBuilder) {
			
			var GC = this, $$scope = $scope

			var requestDelay = 400

			var updateDatatable = function() {
				GC.dtInstance.reloadData()
			}

			var getFilterParameters = function() {
				var categoryId = $scope.select.categories.model.id
				var manufacturerId = $scope.select.manufacturers.model.id
				var costfrom = $scope.cost.from.model
				var costto = $scope.cost.to.model
				
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

			GC.goodsManager = {

				init: function(translations, manufacturers, categories) {
					
					var itemNo = { name: translations.GOODS_LIST_SELECTBOX_ITEM_NO }

					var promiseCostFrom = promiseCostTo = null

					$scope.select = {
						manufacturers: {
							list: [
								angular.copy(itemNo)
							],
							disabled: false,
							change: updateDatatable
						}, 
						categories: {
							list: [
								angular.copy(itemNo)
							],
							disabled: false,
							change: updateDatatable
						} 
					}

					$scope.cost = {
						from: {
							model: '',
							change: function() {
								$timeout.cancel(promiseCostFrom)
								if(allowedNumber(this.model)) {
									promiseCostFrom = $timeout(function() { updateDatatable() }, requestDelay)
								}
							}
						},
						to: {
							model: '',
							change: function() {
								$timeout.cancel(promiseCostTo)
								if(allowedNumber(this.model)) {
									promiseCostTo = $timeout(function() { updateDatatable() }, requestDelay)
								}
							}
						}
					}

					$scope.formula = {
						recalculateButtonText: translations.GOODS_LIST_BUTTON_RECALCULATE,
						disabled: false,
						model: '',
						invalidMsg: false,
						change: function() {
							if (!/^(\+|\-)[1-9][0-9]{0,2}$/.test(this.model)) {
								this.invalidMsg = translations.GOODS_LIST_VALIDATION_NOT_SIGNED_NUMBER_ERROR
								this.disabled = true
							}
							else {
								this.invalidMsg = this.disabled = false
							}
						},
						loading: function(state) {
							if (angular.equals(state, true)) {
								this.disabled = true
								this.recalculateButtonText = translations.GOODS_LIST_BUTTON_LOADING
							}
							else {
								this.disabled = false
								this.recalculateButtonText = translations.GOODS_LIST_BUTTON_RECALCULATE
							}
						}
					}

					$scope.formula.change()

					$scope.select.manufacturers.model = $scope.select.manufacturers.list[0]
					$scope.select.categories.model = $scope.select.categories.list[0]

					if (angular.isArray(manufacturers)) {
						angular.forEach(manufacturers, function(manufacturer, key) {
							var parsedManufacturer = {
								id: manufacturer[0],
								name: manufacturer[1]
							}
							$scope.select.manufacturers.list.push(parsedManufacturer)
						})
					}

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
							$scope.select.categories.list.push({
								id: category.id,
								name: category.title,
								path: '<h6>' + pathPrepared + '</h6>'
							})
						})
					}

					GC.dtInstance = {}
					GC.dtOptions = DTOptionsBuilder.newOptions()

					if ($translate.use() == 'ru_RU' || $translate.proposedLanguage() == 'ru_RU') {
						GC.dtOptions.withLanguageSource('/assets/admin/scripts/controllers/locale-ru_RU.json')
					}

					GC.dtOptions.withOption('createdRow', function(row, data, dataIndex) {
						$compile(angular.element(row).contents())($scope)
					})

					.withOption('ajax', {
						url: actionToUrlMask['Goods.list'],
						type: 'GET',
						data: function(params) {
							angular.extend(params, getFilterParameters())
						}
					})

					.withOption('fnInitComplete', function() {

						var searchFilter = angular.element('div.dataTables_filter input')
						var promise = null
						searchFilter.off('keyup.DT search.DT input.DT paste.DT cut.DT')

						searchFilter.on('input', function() {
							
							var searchFilterValue = searchFilter.val()
							$timeout.cancel(promise)

							promise = $timeout(function() {
								GC.dtInstance.DataTable.search(searchFilterValue)
								updateDatatable()
							}, requestDelay)
						})
					})

					.withOption('autoWidth', false)

					.withDataProp('data')
						.withOption('processing', true)
						.withOption('serverSide', true)
						.withPaginationType('full_numbers')

					GC.dtColumns = [

						DTColumnBuilder.newColumn('0').withTitle(translations.GOODS_LIST_ID).renderWith(function(data, type, full) {
							return full[0]
						})
						.withClass('datatable-title'),
						
						DTColumnBuilder.newColumn('1').withTitle(translations.GOODS_LIST_NAME).renderWith(function(data, type, full) {
							return $interpolate('<a href="/admin/goods/{{id}}">{{title}}</a>')({
								id: full[0],
								title: full[1]
							}) 
						})
						.withClass('datatable-title'),

						DTColumnBuilder.newColumn('2').withTitle(translations.GOODS_LIST_PARTNUMBER).renderWith(function(data, type, full) {
							return isNull(full[2]) ? 
								translations.GOODS_LIST_NO : full[2]
						})
						.withClass('datatable-title')
						.notSortable(),

						DTColumnBuilder.newColumn('3').withTitle(translations.GOODS_LIST_MANUFACTURER).renderWith(function(data, type, full) {
							return isNull(full[4]) ? 
								translations.GOODS_LIST_NO : full[4]
						})
						.withClass('datatable-title')
						.notSortable(),

						DTColumnBuilder.newColumn('4').withTitle(translations.GOODS_LIST_COST).renderWith(function(data, type, full) {
							return angular.equals(full[5], 0) ? 
								translations.GOODS_LIST_NO : full[5]
						})
						.withClass('datatable-title')
						.notSortable(),

						DTColumnBuilder.newColumn('5').withTitle(translations.GOODS_LIST_WARRANTY).renderWith(function(data, type, full) {
							return isNull(full[6]) ? 
								translations.GOODS_LIST_NO : full[6]
						})
						.withClass('datatable-title')
						.notSortable(),
						
						DTColumnBuilder.newColumn('6').withTitle(translations.GOODS_LIST_ACTION).renderWith(function(data, type, full) {
							return $interpolate(
								'<button class="btn btn-default btn-sm" type="button" ng-click="GC.goodsManager.remove($event, {{id}})"> \
									<i class="fa fa-trash-o"></i> \
									<span class="bold" translate>GOODS_LIST_BUTTON_DELETE</span> \
								 </button>')
							({ id: full[0] })
						})
						.withClass('datatable-title')
						.notSortable()

					]

				},

				add: function(event) {
					$modalInstance = $modal.open({
						templateUrl: '/assets/admin/views/modal/good.add.html',
						size: 'lg',
						controller: ['$scope', function($scope) {
							$scope.Validation = {
								value: '',
								invalidMsg: false,
								validate: function() {
									if (this.value.length == 0) {
										this.invalidMsg = $$scope.translations.GOODS_MODAL_ADD_VALIDATION_EMPTY_ERROR
									}
									else if (this.value.length < 2) {
										this.invalidMsg = $$scope.translations.GOODS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR
									}
									else if (this.value.length > 250) {
										this.invalidMsg = $interpolate($$scope.translations.GOODS_MODAL_ADD_VALIDATION_EXCEED_ERROR)({
											exceed: this.value.length - 250
										})
									}
									else {
										this.invalidMsg = false
									}
								}
							}
							
							$scope.Validation.validate()

							$scope.goodsModalManager = {
								ok: function() {
									$http.post(actionToUrlMask['Goods.add'], angular.toJson({'title': $scope.Validation.value}))
										.then(function(data) {
											$modalInstance.close()
											$location.path('/admin/goods/' + data.data)
										})
								},
								cancel: function() {
									$modalInstance.dismiss('cancel')
								}
							}
						}]
					})
				},

				remove: function(event, id) {
					window.swal({
						title: $scope.translations.GOODS_NOTIFICATION_WARNING_TITLE,
						text: $scope.translations.GOODS_NOTIFICATION_WARNING_TEXT,
						type: 'warning',
						showCancelButton: true,
						confirmButtonText: $scope.translations.GOODS_NOTIFICATION_WARNING_BUTTON_CONFIRM,
						cancelButtonText: $scope.translations.GOODS_NOTIFICATION_WARNING_BUTTON_CANCEL,
						closeOnConfirm: true,
						closeOnCancel: true
					},
					function (isConfirm) {
						if (isConfirm) {
							var url = actionToUrlMask['Goods.delete'].replace(/\/\:id/i, '\/' + id)
							$http.delete(url)
							.then(
								function(data) {
									if (angular.isFunction(GC.dtInstance.rerender)) {
										GC.dtInstance.rerender()
									}
									else {
										angular.element('.spinner').addClass('show')
										$window.location.reload()
									}
								}, 
								function(data) { 
									window.swal(
										$scope.translations.GOODS_NOTIFICATION_ERROR_TITLE,
										$scope.translations.GOODS_NOTIFICATION_ERROR_TEXT, 
										'error'
									)
								}
							)
						}
					})
				},

				recalculate: function() {

					var spinner = angular.element('.spinner')
					var parameters = {}
					var formula = $scope.formula
					
					angular.extend(parameters, getFilterParameters())
					
					angular.extend(parameters, { 
						formula: formula.model, 
						search: GC.dtInstance.DataTable.search()
					})

					formula.loading(true)
					spinner.addClass('show')

					$http.post(actionToUrlMask['Goods.updatePrices'], angular.toJson(parameters))
					.then(
						function(response) {
							if (response.data == 'SUCCESS') {
								updateDatatable()
							}
							formula.loading(false)
							spinner.removeClass('show')
						},
						function(badResponse) {
							if (badResponse.data == 'INCORRECT_FORMULA') {
								window.swal(
									$scope.translations.GOODS_NOTIFICATION_RECALCULATION_ERROR_TITLE,
									$scope.translations.GOODS_NOTIFICATION_RECALCULATION_ERROR_TEXT,
									'error'
								)
							}
							formula.loading(false)
							spinner.removeClass('show')
						}
					)
				}
			}

			GC.goodsManager.init($scope.translations, $scope.manufacturers, $scope.categories)

		}])

})(window, window.angular, void 0);