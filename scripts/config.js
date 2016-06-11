/**
 * HOMER - Responsive Admin Theme (Modifiend by hypotenuse)
 * version 1.7
 *
 */
(function(window, angular, undefined) {

	angular.module('homer')
		.config(function($stateProvider, $urlRouterProvider, $compileProvider,
			$locationProvider) {

			var goodsListResolve = {
				'translations': ['$translate', function($translate) {
					return $translate([
							'GOODS_LIST_NO',
							'GOODS_LIST_ID',
							'GOODS_LIST_NAME',
							'GOODS_LIST_PARTNUMBER',
							'GOODS_LIST_MANUFACTURER',
							'GOODS_LIST_COST',
							'GOODS_LIST_COST_FROM_PLACEHOLDER',
							'GOODS_LIST_COST_TO_PLACEHOLDER',
							'GOODS_LIST_WARRANTY',
							'GOODS_LIST_ACTION',
							'GOODS_LIST_SELECTBOX_CATEGORY_PLACEHOLDER',
							'GOODS_LIST_SELECTBOX_MANUFACTURER_PLACEHOLDER',
							'GOODS_LIST_SELECTBOX_ITEM_NO',
							'GOODS_LIST_BUTTON_RECALCULATE',
							'GOODS_LIST_BUTTON_LOADING',
							'GOODS_LIST_VALIDATION_NOT_SIGNED_NUMBER_ERROR',
							'GOODS_NOTIFICATION_WARNING_TITLE',
							'GOODS_NOTIFICATION_WARNING_TEXT',
							'GOODS_NOTIFICATION_WARNING_BUTTON_CONFIRM',
							'GOODS_NOTIFICATION_WARNING_BUTTON_CANCEL',
							'GOODS_NOTIFICATION_SUCCESS_TITLE',
							'GOODS_NOTIFICATION_SUCCESS_TEXT',
							'GOODS_NOTIFICATION_ERROR_TITLE',
							'GOODS_NOTIFICATION_ERROR_TEXT',
							'GOODS_NOTIFICATION_RECALCULATION_ERROR_TITLE',
							'GOODS_NOTIFICATION_RECALCULATION_ERROR_TEXT',
							'GOODS_MODAL_ADD_VALIDATION_EMPTY_ERROR',
							'GOODS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR',
							'GOODS_MODAL_ADD_VALIDATION_EXCEED_ERROR'
						])
						.then(function(translations) {
							return translations
						})
				}],
				'manufacturers': ['$http', 'actionToUrlMask', function($http,
					actionToUrlMask) {
					return $http.get(actionToUrlMask['Manufacturers.list'])
						.then(function(response) {
							return response.data.data
						})
				}],
				'categories': ['$http', 'actionToUrlMask', function($http,
					actionToUrlMask) {
					return $http.get(actionToUrlMask['Categories.list.all'])
						.then(function(response) {
							return response.data
						})
				}]
			}

			var goodsListController = [
				'$scope', 'translations', 'manufacturers', 'categories',
				function($scope, translations, manufacturers, categories) {
					$scope.translations = translations
					$scope.manufacturers = manufacturers
					$scope.categories = categories
				}
			]

			// Optimize load start with remove binding information inside the DOM element
			$compileProvider.debugInfoEnabled(true)

			$locationProvider.html5Mode({
				enabled: true,
				requireBase: true
			});

			// Set default state
			$urlRouterProvider.otherwise('/admin')

			$stateProvider

				.state('/admin', {
				url: '/admin',
				templateUrl: '/assets/admin/views/goods.list.html',
				data: {
					pageTitle: 'Goods Dashboard'
				},
				resolve: goodsListResolve,
				controller: goodsListController
			})

			.state('/admin/goods', {
				url: '/admin/goods',
				templateUrl: '/assets/admin/views/goods.list.html',
				data: {
					pageTitle: 'Goods list'
				},
				resolve: goodsListResolve,
				controller: goodsListController
			})

			.state('/admin/goods/id', {
				url: '/admin/goods/:id',
				templateUrl: '/assets/admin/views/goods.update.html',
				data: {
					pageTitle: 'Goods Update'
				},
				resolve: {
					'manufacturers': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Manufacturers.list'])
							.then(function(response) {
								return response.data.data
							})
					}],
					'categories': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Categories.list.all'])
							.then(function(response) {
								return response.data
							})
					}],
					'goodData': ['$http', '$stateParams', 'actionToUrlMask', function(
						$http, $stateParams, actionToUrlMask) {
						var goodViewUrl = actionToUrlMask['Goods.view'].replace(/\/\:id/i,
							'\/' + $stateParams.id)
						return $http.get(goodViewUrl)
							.then(
								function(response) {
									return [response, goodViewUrl]
								},
								function(badResponse) {
									return [badResponse, goodViewUrl]
								}
							)
					}]
				},
				controller: ['$scope', '$stateParams', 'goodData', 'manufacturers',
					'categories',
					function($scope, $stateParams, goodData, manufacturers, categories) {
						$scope.goodRefId = $stateParams.id
						$scope.goodData = goodData[0]
						$scope.goodViewUrl = goodData[1]
						$scope.manufacturers = manufacturers
						$scope.categories = categories
					}
				]
			})

			.state('/admin/customers', {
				url: '/admin/customers',
				templateUrl: '/assets/admin/views/customers.list.html',
				data: {
					pageTitle: 'Customers list'
				}
			})

			.state('/admin/rawgoods', {
				url: '/admin/rawgoods',
				templateUrl: '/assets/admin/views/rawgoods.list.html',
				data: {
					pageTitle: 'Rawgoods list'
				},
				resolve: {
					'manufacturers': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Manufacturers.list'])
							.then(function(response) {
								return response.data.data
							})
					}],
					'categories': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Categories.list.all'])
							.then(function(response) {
								return response.data
							})
					}],
					'suppliers': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Suppliers.list'])
							.then(function(response) {
								return response.data
							})
					}]
				},
				controller: ['$scope', '$stateParams', 'manufacturers',
					'categories', 'suppliers',
					function($scope, $stateParams, manufacturers, categories, suppliers) {
						$scope.manufacturers = manufacturers
						$scope.categories = categories
						$scope.suppliers = suppliers
					}
				]
			})

			.state('/admin/prices', {
				url: '/admin/prices',
				templateUrl: '/assets/admin/views/prices.list.html',
				data: {
					pageTitle: 'Prices list'
				}
			})

			.state('/admin/costscales', {
				url: '/admin/costscales',
				templateUrl: '/assets/admin/views/costscales.list.html',
				data: {
					pageTitle: 'Costscales list'
				}
			})

			.state('/admin/admins', {
				url: '/admin/admins',
				templateUrl: '/assets/admin/views/admins.list.html',
				data: {
					pageTitle: 'Admins list'
				}
			})

			.state('/admin/admins/id', {
				url: '/admin/admins/:id',
				templateUrl: '/assets/admin/views/admins.update.html',
				data: {
					pageTitle: 'Admin edit'
				},
				resolve: {
					'currentAdmin': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask['Admins.update'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response;
									},
									function(badResponse) {
										return badResponse;
									}
								);
						}
					]
				},
				controller: ['$scope', 'currentAdmin', function($scope,
					currentAdmin) {
					$scope.currentAdmin = currentAdmin;
				}]
			})

			.state('/admin/settings', {
				url: '/admin/settings',
				templateUrl: '/assets/admin/views/settings.list.html',
				data: {
					pageTitle: 'Settings list'
				}
			})

			.state('/admin/settings/id', {
				url: '/admin/settings/:id',
				templateUrl: '/assets/admin/views/settings.update.html',
				data: {
					pageTitle: 'Settings edit'
				},
				resolve: {
					'currentSettings': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask['Settings.update'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response;
									},
									function(badResponse) {
										return badResponse;
									}
								);
						}
					]
				},
				controller: ['$scope', 'currentSettings', function($scope,
					currentSettings) {
					$scope.currentSettings = currentSettings;
				}]
			})

			.state('/admin/excelsettings', {
				url: '/admin/excelsettings',
				templateUrl: '/assets/admin/views/excelsettings.list.html'
			})

			.state('/admin/excelsettings/new', {
				url: '/admin/excelsettings/new',
				templateUrl: '/assets/admin/views/excelsettings.update.html',
				resolve: {
					'suppliers': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Suppliers.list'])
							.then(function(response) {
								return response.data.data
							})
					}],
					'currencies': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Currencies.list'])
							.then(function(response) {
								return response.data.data
							})
					}]
				},
				controller: ['$scope', 'suppliers', 'currencies', function($scope,
					suppliers, currencies) {
					$scope.suppliers = suppliers
					$scope.currencies = currencies
				}]
			})

			.state('/admin/excelsettings/id', {
				url: '/admin/excelsettings/:id',
				templateUrl: '/assets/admin/views/excelsettings.update.html',
				resolve: {
					'suppliers': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Suppliers.list'])
							.then(function(response) {
								return response.data.data
							})
					}],
					'currencies': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Currencies.list'])
							.then(function(response) {
								return response.data.data
							})
					}],
					'excelsettings': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate, actionToUrlMask) {
							return $http.get(
									$interpolate(actionToUrlMask['ExcelSettings.view'])({
										id: $stateParams.id
									})
								)
								.then(function(response) {
									return response.data
								})
						}
					]
				},
				controller: ['$scope', 'suppliers', 'currencies', 'excelsettings',
					function($scope, suppliers, currencies, excelsettings) {
						$scope.suppliers = suppliers
						$scope.currencies = currencies
						$scope.excelsettings = excelsettings
					}
				]
			})

			.state('/admin/suppliers', {
				url: '/admin/suppliers',
				templateUrl: '/assets/admin/views/suppliers.list.html',
				data: {
					pageTitle: 'Suppliers list'
				}
			})

			.state('/admin/suppliers/id', {
				url: '/admin/suppliers/:id',
				templateUrl: '/assets/admin/views/suppliers.update.html',
				data: {
					pageTitle: 'Suppliers edit'
				},
				resolve: {
					'currentSuppliers': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask[
									'Suppliers.update'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response;
									},
									function(badResponse) {
										return badResponse;
									}
								);
						}
					]
				},
				controller: ['$scope', 'currentSuppliers', function($scope,
					currentSuppliers) {
					$scope.currentSuppliers = currentSuppliers;
				}]
			})

			.state('/admin/orderstatusmessages', {
				url: '/admin/orderstatusmessages',
				templateUrl: '/assets/admin/views/orderstatusmessages.list.html',
				data: {
					pageTitle: 'Order status messages list'
				}
			})

			.state('/admin/orderstatusmessages/id', {
				url: '/admin/orderstatusmessages/:id',
				templateUrl: '/assets/admin/views/orderstatusmessages.update.html',
				data: {
					pageTitle: 'Order status messages edit'
				},
				resolve: {
					'currentOrderStatusMessages': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask[
									'Orderstatusmessages.update'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response;
									},
									function(badResponse) {
										return badResponse;
									}
								);
						}
					]
				},
				controller: ['$scope', 'currentOrderStatusMessages', function($scope,
					currentOrderStatusMessages) {
					$scope.currentOrderStatusMessages = currentOrderStatusMessages;
				}]
			})

			.state('/admin/orderstatuses', {
				url: '/admin/orderstatuses',
				templateUrl: '/assets/admin/views/orderstatuses.list.html',
				data: {
					pageTitle: 'Order status list'
				}
			})

			.state('/admin/orderstatuses/id', {
				url: '/admin/orderstatuses/:id',
				templateUrl: '/assets/admin/views/orderstatuses.update.html',
				data: {
					pageTitle: 'Order status edit'
				},
				resolve: {
					'currentOrderStatuses': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask[
									'Orderstatuses.update'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response;
									},
									function(badResponse) {
										return badResponse;
									}
								);
						}
					]
				},
				controller: ['$scope', 'currentOrderStatuses', function($scope,
					currentOrderStatuses) {
					$scope.currentOrderStatus = currentOrderStatuses;
				}]
			})

			.state('/admin/currencies', {
				url: '/admin/currencies',
				templateUrl: '/assets/admin/views/currencies.list.html',
				data: {
					pageTitle: 'Currencies list'
				}
			})

			.state('/admin/currencies/id', {
				url: '/admin/currencies/:id',
				templateUrl: '/assets/admin/views/currencies.update.html',
				data: {
					pageTitle: 'Currencies edit'
				},
				resolve: {
					'currentCurrencies': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask[
									'Currencies.update'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response;
									},
									function(badResponse) {
										return badResponse;
									}
								);
						}
					]
				},
				controller: ['$scope', 'currentCurrencies', function($scope,
					currentCurrencies) {
					$scope.currentCurrencies = currentCurrencies;
				}]
			})


			.state('/admin/news', {
				url: '/admin/news',
				templateUrl: '/assets/admin/views/news.list.html',
				data: {
					pageTitle: 'News list'
				}
			})

			.state('/admin/news/id', {
				url: '/admin/news/:id',
				templateUrl: '/assets/admin/views/news.update.html',
				data: {
					pageTitle: 'News edit'
				},
				resolve: {
					'currentNews': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask['News.update'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response;
									},
									function(badResponse) {
										return badResponse;
									}
								);
						}
					]
				},
				controller: ['$scope', 'currentNews', function($scope,
					currentNews) {
					$scope.currentNews = currentNews;
				}]
			})


			.state('/admin/categories', {
				url: '/admin/categories',
				templateUrl: '/assets/admin/views/categories.list.html',
				data: {
					pageTitle: 'Categories list'
				}
			})

			.state('/admin/categories/id', {
				url: '/admin/categories/:id',
				templateUrl: '/assets/admin/views/subcategories.list.html',
				data: {
					pageTitle: 'Subcategories list'
				},
				resolve: {
					'currentCategory': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask['Categories.view'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response
									},
									function(badResponse) {
										return badResponse
									}
								)
						}
					],
					'childCategories': ['$http', '$interpolate', 'currentCategory',
						'actionToUrlMask',
						function($http, $interpolate, currentCategory,
							actionToUrlMask) {
							var currentCaterogyLoaded = false
							try {
								currentCaterogyLoaded = angular.isNumber(currentCategory.data.category
									.id);
							} catch (ex) {}
							if (currentCaterogyLoaded) {
								return $http.get($interpolate(actionToUrlMask[
										'Categories.list.parent'])({
										parentId: currentCategory.data.category.id
									}))
									.then(
										function(response) {
											return response
										},
										function(badResponse) {
											return badResponse
										}
									)
							} else {
								return false
							}
						}
					]
				},
				controller: ['$scope', 'currentCategory', 'childCategories', function(
					$scope, currentCategory, childCategories) {
					$scope.currentCategory = currentCategory
					$scope.childCategories = childCategories
				}]
			})

			.state('/admin/categories/id/edit', {
				url: '/admin/categories/:id/edit',
				templateUrl: '/assets/admin/views/categories.update.html',
				data: {
					pageTitle: 'Category edit'
				},
				resolve: {
					'currentCategory': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask['Categories.view'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response
									},
									function(badResponse) {
										return badResponse
									}
								)
						}
					],
					'selectCategories': ['$http', '$interpolate', 'currentCategory',
						'actionToUrlMask',
						function($http, $interpolate, currentCategory,
							actionToUrlMask) {
							var currentCaterogyLoaded = false
							try {
								currentCaterogyLoaded = angular.isNumber(currentCategory.data.category
									.id)
							} catch (ex) {}
							if (currentCaterogyLoaded) {
								return $http.get($interpolate(actionToUrlMask['Categories.select'])
										({
											id: currentCategory.data.category.id
										}))
									.then(
										function(response) {
											return response
										},
										function(badResponse) {
											return badResponse
										}
									)
							} else {
								return false
							}
						}
					]
				},
				controller: ['$scope', 'currentCategory', 'selectCategories', function(
					$scope, currentCategory, selectCategories) {
					$scope.currentCategory = currentCategory
					$scope.selectCategories = selectCategories
				}]
			})

			.state('/admin/manufacturers', {
				url: '/admin/manufacturers',
				templateUrl: '/assets/admin/views/manufacturers.list.html',
				data: {
					pageTitle: 'Manufacturers list'
				}
			})

			.state('/admin/manufacturers/id/edit', {
				url: '/admin/manufacturers/:id/edit',
				templateUrl: '/assets/admin/views/manufacturers.update.html',
				data: {
					pageTitle: 'Manufacturer edit'
				},
				resolve: {
					'currentManufacturer': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask['Manufacturers.view'])
									({
										id: $stateParams.id
									}))
								.then(
									function(response) {
										return response;
									},
									function(badResponse) {
										return badResponse;
									}
								);
						}
					]
				},
				controller: ['$scope', 'currentManufacturer', function($scope,
					currentManufacturer) {
					$scope.currentManufacturer = currentManufacturer;
				}]
			})

			.state('/admin/sales', {
				url: '/admin/sales',
				templateUrl: '/assets/admin/views/sales.list.html',
				data: {
					pageTitle: 'Sales list'
				}
			})

			.state('/admin/sales/id', {
				url: '/admin/sales/:id',
				templateUrl: '/assets/admin/views/sales.update.html',
				data: {
					pageTitle: 'Sale edit'
				},
				resolve: {
					'translations': ['$translate', function($translate) {
						return $translate([
								'GOODS_LIST_NO',
								'GOODS_LIST_ID',
								'GOODS_LIST_NAME',
								'GOODS_LIST_PARTNUMBER',
								'GOODS_LIST_COST',
								'GOODS_LIST_COST_FROM_PLACEHOLDER',
								'GOODS_LIST_COST_TO_PLACEHOLDER',
								'GOODS_LIST_SELECTBOX_CATEGORY_PLACEHOLDER',
								'GOODS_LIST_SELECTBOX_MANUFACTURER_PLACEHOLDER',
								'GOODS_LIST_SELECTBOX_ITEM_NO',
								'SALE_UPDATE_GOODS_ADDED',
								'SALE_UPDATE_GOODS_REMOVED',
								'SALE_UPDATE_GOODS_NOT_ADDED'
							])
							.then(function(translations) {
								return translations
							})
					}],
					'manufacturers': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Manufacturers.list'])
							.then(function(response) {
								return response.data.data
							})
					}],
					'categories': ['$http', 'actionToUrlMask', function($http,
						actionToUrlMask) {
						return $http.get(actionToUrlMask['Categories.list.all'])
							.then(function(response) {
								return response.data
							})
					}],
					'currentSale': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate, actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask['Sales.view'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response
									},
									function(badResponse) {
										return badResponse
									}
								)
						}
					]
				},
				controller: ['$scope', 'translations', 'manufacturers', 'categories',
					'currentSale',
					function($scope, translations, manufacturers, categories, currentSale) {
						$scope.translations = translations
						$scope.manufacturers = manufacturers
						$scope.categories = categories
						$scope.currentSale = currentSale
					}
				]
			})

			.state('/admin/slides', {
				url: '/admin/slides',
				templateUrl: '/assets/admin/views/slides.list.html',
				data: {
					pageTitle: 'Slides list'
				}
			})

			.state('/admin/slides/id', {
				url: '/admin/slides/:id',
				templateUrl: '/assets/admin/views/slides.update.html',
				data: {
					pageTitle: 'Slide edit'
				},
				resolve: {
					'currentSlide': ['$http', '$stateParams', '$interpolate',
						'actionToUrlMask',
						function($http, $stateParams, $interpolate,
							actionToUrlMask) {
							return $http.get($interpolate(actionToUrlMask['Slides.view'])({
									id: $stateParams.id
								}))
								.then(
									function(response) {
										return response;
									},
									function(badResponse) {
										return badResponse;
									}
								);
						}
					]
				},
				controller: ['$scope', 'currentSlide', function($scope, currentSlide) {
					$scope.currentSlide = currentSlide;
				}]
			});

		})

	.run(function($rootScope, $state) {
		$rootScope.$state = $state;
	});

})(window, window.angular, void 0);
