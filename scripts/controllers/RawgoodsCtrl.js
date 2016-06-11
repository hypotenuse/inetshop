;
(function(window, angular, undefined) {

  angular.module('homer')
    .controller('RawgoodsCtrl', ['$scope', '$http', '$modal', '$location',
      '$interpolate', '$translate', 'actionToUrlMask', '$state', '$log',
      'Upload', '$interval', 'DTOptionsBuilder', 'DTColumnDefBuilder',
      '$state', '$log', 'DTColumnBuilder',
      function($scope, $http, $modal, $location, $interpolate, $translate,
        actionToUrlMask, $state, $log, Upload, $interval, DTOptionsBuilder,
        DTColumnDefBuilder, $state, $log, DTColumnBuilder) {
        $scope.setting = '';
        var listGoodsTable;
        var listSuppliersTable;

        $.fn.DataTable.ext.pager.numbers_length = 5;
        angular.element(document).ready(function() {
          angular.element(document).on('click',
            '.goods',
            function() {
              if ($scope.priceId) {

                var linkToUrl = $interpolate(
                  actionToUrlMask[
                    'Rawgoods.linkTo'])({
                  id: $scope.priceId,
                  goods: angular.element(this).attr(
                    'data-id'),
                });

                $http.get(linkToUrl)
                  .success(function(data, status, headers, config) {
                    swal(
                      $scope.translations.NOTIFICATION_SUCCESS_SAVE,
                      $scope.translations.NOTIFICATION_SUCCESS_UPDATED_TEXT,
                      'success'
                    );
                  })
                  .error(function(data, status, header, config) {});
              } else {
                swal(
                  $scope.translations.NOTIFICATION_ERROR_TITLE,
                  $scope.translations.NOTIFICATION_ERROR_SELECT_PRICE,
                  'error'
                );
              }
            });
          angular.element(document).on('click',
            '.detail',
            function() {
              $scope.priceId = angular.element(this).attr(
                'data-id');
              angular.element('tbody tr').removeClass(
                'active');
              angular.element(this).addClass(
                'active');

              var rawgoodsUrl = $interpolate(
                actionToUrlMask[
                  'Rawgoods.view'])({
                id: $scope.priceId
              });

              $http.get(rawgoodsUrl)
                .success(function(data, status, headers, config) {
                  $scope.detail = data;
                })
                .error(function(data, status, header, config) {});
            });
        });


        $translate([
            'VALIDATION_EMPTY_ERROR',
            'VALIDATION_MINLENGTH_ERROR',
            'VALIDATION_EXCEED_ERROR',
            'VALIDATION_NON_NUMBERS_ERROR',
            'VALIDATION_NUMBER_EXCEED_ERROR',
            'NOTIFICATION_WARNING_TITLE',
            'NOTIFICATION_WARNING_TEXT',
            'NOTIFICATION_WARNING_BUTTON_CONFIRM',
            'NOTIFICATION_WARNING_BUTTON_CANCEL',
            'NOTIFICATION_SUCCESS_TITLE',
            'NOTIFICATION_SUCCESS_TEXT',
            'NOTIFICATION_ERROR_TITLE',
            'NOTIFICATION_ERROR_REASON_TEXT',
            'NOTIFICATION_DELETE_SUCCESS_TITLE',
            'NOTIFICATION_DELETE_SUCCESS_TEXT',
            'NOTIFICATION_SUCCESS_SAVE',
            'NOTIFICATION_SUCCESS_UPDATED_TEXT',
            'NOTIFICATION_ERROR_PRICE_FORMAT',
            'GOODS_LIST_SELECTBOX_ITEM_NO',
            'CONNECTION',
            'NOTIFICATION_ERROR_SELECT_PRICE'
          ])
          .then(function(translations) {
            $scope.translations = translations
            $scope.rawgoodsManager = {
              list: function() {
                var requestOk = function(data) {
                  $scope.rawGoods = data.data;
                }
                var requestFail = function(data) {
                  $log.warn('Something went wrong: (status ' + data
                    .status + data.statusText +
                    ')')
                }
                $http.get(actionToUrlMask['Rawgoods.list'])
                  .then(requestOk, requestFail)
              },
              save: function() {
                var spinner = angular.element('.spinner');
                spinner.addClass('show')

              },
              getFilterParametersGoods: function() {
                try {
                  var categoryId = $scope.selectGoods.categories.model
                    .id
                  var manufacturerId = $scope.selectGoods.manufacturers
                    .model
                    .id
                  var params = {}

                  if (angular.isDefined(categoryId)) {
                    params.category = categoryId
                  }
                  if (angular.isDefined(manufacturerId)) {
                    params.manufacturer = manufacturerId
                  }
                } catch (e) {
                  var params = {}
                }
                return params
              },
              getFilterParametersSuppliers: function() {
                try {
                  var supplierId = $scope.selectSuppliers.suppliers.model
                    .id
                  var params = {}

                  if (angular.isDefined(supplierId)) {
                    params.supplierId = supplierId
                  }
                } catch (e) {
                  var params = {}
                }
                return params
              },
              initTableGoods: function() {
                angular.element('.spinner').addClass('show');
                listGoodsTable = angular.element('#listGoodsTable').DataTable({
                  "language": {
                    "url": "/assets/admin/scripts/controllers/locale-ru_RU.json"
                  },
                  "ajax": {
                    url: actionToUrlMask['Goods.list'],
                    type: 'GET',
                    data: function(params) {
                      angular.extend(params,
                        $scope.rawgoodsManager.getFilterParametersGoods()
                      )
                    },
                  },
                  columns: [{
                    data: 1
                  }, {
                    data: 2
                  }, {
                    data: 3
                  }, {
                    "orderable": false,
                  }],
                  "processing": true,
                  "serverSide": true,
                  "bDestroy": true,
                  "searchDelay": 1000,
                  "ordering": false,
                  "fnRowCallback": function(nRow, aData,
                    iDisplayIndex, iDisplayIndexFull) {
                    angular.element('td:eq(3)', nRow).html(
                      '<a href="#connect" class="goods" data-id="' +
                      aData[
                        0] + '">' +
                      translations.CONNECTION + '</a>');

                    return nRow;
                  }
                });
                angular.element('#listGoodsTable').dataTable().fnSetFilteringDelay(
                  1000);
                angular.element('.spinner').removeClass('show');
              },
              tableGoods: function(manufacturers, categories) {
                var selectDatatableGoods = function() {
                  $scope.rawgoodsManager.initTableGoods();
                }
                var itemNo = {
                  name: translations.GOODS_LIST_SELECTBOX_ITEM_NO
                }
                $scope.selectGoods = {
                  manufacturers: {
                    list: [
                      angular.copy(itemNo)
                    ],
                    disabled: false,
                    change: selectDatatableGoods
                  },
                  categories: {
                    list: [
                      angular.copy(itemNo)
                    ],
                    disabled: false,
                    change: selectDatatableGoods
                  }
                }
                $scope.selectGoods.manufacturers.model = $scope.selectGoods
                  .manufacturers
                  .list[0]
                $scope.selectGoods.categories.model = $scope.selectGoods
                  .categories
                  .list[0]

                if (angular.isArray(manufacturers)) {
                  angular.forEach(manufacturers, function(
                    manufacturer, key) {
                    var parsedManufacturer = {
                      id: manufacturer[0],
                      name: manufacturer[1]
                    }
                    $scope.selectGoods.manufacturers.list.push(
                      parsedManufacturer)
                  })
                }

                if (angular.isArray(categories)) {
                  angular.forEach(categories, function(category,
                    key) {
                    var pathtoroot = angular.fromJson(
                      category.pathtoroot)
                    var pathPrepared = ''
                    if (pathtoroot.length > 0) {
                      angular.forEach(pathtoroot, function(
                        path,
                        key) {
                        pathPrepared = pathPrepared +
                          path.title +
                          ((Number(key) == pathtoroot.length -
                              1) ? ' / ' + category.title :
                            ' / ')
                      })
                    } else {
                      pathPrepared = '/'
                    }
                    $scope.selectGoods.categories.list.push({
                      id: category.id,
                      name: category.title,
                      path: '<h6>' + pathPrepared +
                        '</h6>'
                    })
                  })
                }

                $scope.rawgoodsManager.initTableGoods();
              },
              initTableSuppliers: function() {
                $scope.detail = null;
                $scope.priceId = null;
                angular.element('.spinner').addClass('show');
                listSuppliersTable = angular.element(
                  '#listSuppliersTable').DataTable({
                  "language": {
                    "url": "/assets/admin/scripts/controllers/locale-ru_RU.json"
                  },
                  "ajax": {
                    url: actionToUrlMask['Rawgoods.list'],
                    type: 'GET',
                    data: function(params) {
                      angular.extend(params,
                        $scope.rawgoodsManager.getFilterParametersSuppliers()
                      )
                    }
                  },
                  columns: [{
                    data: "title"
                  }, {
                    data: "brand"
                  }],
                  "processing": true,
                  "serverSide": true,
                  "bDestroy": true,
                  "searchDelay": 1000,
                  "pageLength": 5,
                  "ordering": false,
                  "fnRowCallback": function(nRow, aData,
                    iDisplayIndex, iDisplayIndexFull) {
                    angular.element(nRow).addClass("detail");
                    angular.element(nRow).attr("data-id", aData[
                      "id"]);
                    return nRow;
                  }
                });
                angular.element('#listSuppliersTable').dataTable().fnSetFilteringDelay(
                  1000);
                angular.element('.spinner').removeClass('show');
              },
              tableSuppliers: function(suppliers) {
                var itemNo = {
                  name: translations.GOODS_LIST_SELECTBOX_ITEM_NO
                }
                var selectDatatableSuppliers = function() {
                  $scope.rawgoodsManager.initTableSuppliers();
                }
                $scope.selectSuppliers = {
                  suppliers: {
                    list: [
                      angular.copy(itemNo)
                    ],
                    disabled: false,
                    change: selectDatatableSuppliers
                  }
                }
                $scope.selectSuppliers.suppliers.model = $scope.selectSuppliers
                  .suppliers
                  .list[0]

                if (angular.isArray(suppliers)) {
                  angular.forEach(suppliers, function(
                    supplier, key) {
                    var parsedSuppliers = {
                      id: supplier.id,
                      name: supplier.title
                    }
                    $scope.selectSuppliers.suppliers.list.push(
                      parsedSuppliers)
                  })
                }
                $scope.rawgoodsManager.initTableSuppliers();
              },
            }

            $scope.rawgoodsManager.tableGoods($scope.manufacturers,
              $scope.categories);
            $scope.rawgoodsManager.tableSuppliers($scope.suppliers.data);
            $scope.rawgoodsManager.list();
          })
      }
    ])

})(window, angular, void 0);
