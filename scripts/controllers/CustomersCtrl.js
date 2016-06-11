;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('CustomersCtrl', ['$scope', '$http', '$modal', '$location',
    '$interpolate', '$translate', 'actionToUrlMask', 'DTOptionsBuilder',
    'DTColumnDefBuilder', '$state', '$log',
    function($scope, $http, $modal, $location, $interpolate, $translate,
      actionToUrlMask, DTOptionsBuilder, DTColumnDefBuilder, $state, $log
    ) {
      $translate([
          'VALIDATION_EMPTY_ERROR',
          'VALIDATION_MINLENGTH_ERROR',
          'VALIDATION_EXCEED_ERROR',
          'VALIDATION_NON_NUMBERS_ERROR',
          'NOTIFICATION_WARNING_TITLE',
          'NOTIFICATION_WARNING_TEXT',
          'NOTIFICATION_WARNING_BUTTON_CONFIRM',
          'NOTIFICATION_WARNING_BUTTON_CANCEL',
          'NOTIFICATION_SUCCESS_TITLE',
          'NOTIFICATION_SUCCESS_TEXT',
          'NOTIFICATION_ERROR_TITLE',
          'NOTIFICATION_ERROR_REASON_TEXT',
          'NOTIFICATION_DELETE_SUCCESS_TITLE',
          'NOTIFICATION_DELETE_SUCCESS_TEXT'
        ])
        .then(function(translations) {

          $scope.translations = translations

          $scope.customersManager = {

            list: function() {
              var requestOk = function(data) {
                var customers = []
                angular.forEach(data.data.data, function(item,
                  key) {
                  var customer = {}
                  customer.id = item.id;
                  customer.name = item.name;
                  customer.email = item.email;
                  customer.phone = item.phone;
                  customer.spent = item.spent;
                  customer.lastvisit = item.lastvisit;
                  customers.push(customer)
                })

                customers.sort(function(first, second) {
                  return first.id > second.id ? 1 : ~0
                })
                $scope.customers = customers
                if ($translate.use() === 'ru_RU') {
                  $scope.dtOptions.withPaginationType(
                      'full_numbers')
                    .withLanguageSource(
                      '/assets/admin/scripts/controllers/locale-ru_RU.json'
                    );
                }
              }
              var requestFail = function(data) {
                $log.warn('Something went wrong: (status ' + data
                  .status + data.statusText +
                  ')')
              }

              $http.get(actionToUrlMask['Customers.list'])
                .then(requestOk, requestFail)
            },


          }

          $scope.dtOptions = DTOptionsBuilder.newOptions().withPaginationType(
            'full_numbers').withDisplayLength(10);
          $scope.dtColumnDefs = [
            DTColumnDefBuilder.newColumnDef(0),
            DTColumnDefBuilder.newColumnDef(1)
          ];
          $scope.customersManager.list()

        })

    }
  ])

})(window, angular, void 0);
