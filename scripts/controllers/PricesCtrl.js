;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('PricesCtrl', ['$scope', '$http', '$modal', '$location',
    '$interpolate', '$translate', 'actionToUrlMask', '$state', '$log',
    'Upload', '$interval',
    function($scope, $http, $modal, $location, $interpolate, $translate,
      actionToUrlMask, $state, $log, Upload, $interval) {
      $scope.setting = '';

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
          'NOTIFICATION_ERROR_PRICE_FORMAT'
        ])
        .then(function(translations) {
          $scope.translations = translations
          $scope.pricesManager = {

            list: function() {
              $scope.pricesManager.updateStatus();
              var requestOk = function(data) {
                $scope.exelSettings = data.data.data;
                $scope.setting = $scope.exelSettings[0][0];
              }
              var requestFail = function(data) {
                $log.warn('Something went wrong: (status ' + data
                  .status + data.statusText +
                  ')')
              }

              $http.get(actionToUrlMask['ExcelSettings.list'])
                .then(requestOk, requestFail)
            },
            save: function() {
              var spinner = angular.element('.spinner');
              spinner.addClass('show')

              Upload.upload({
                url: '/adm/prices',
                method: "POST",
                data: {
                  'settingId': $scope.setting,
                  'price': $scope.file,
                },
                headers: {
                  "Content-Type": "application/x-www-form-urlencoded",
                  "Csrf-Token": 'nocheck'
                },
              }).success(function(data, status, headers, config) {
                spinner.removeClass('show')
                swal(
                  translations.NOTIFICATION_SUCCESS_SAVE,
                  translations.NOTIFICATION_SUCCESS_UPDATED_TEXT,
                  'success'
                );
                $state.go($state.$current, null, {
                  reload: true
                });
              }).error(function(data, status) {
                spinner.removeClass('show')
                swal({
                  type: 'error',
                  title: translations.NOTIFICATION_ERROR_TITLE,
                  text: translations.NOTIFICATION_ERROR_PRICE_FORMAT
                });
              });
            },
            recalculatePrice: function() {
              $http.get('/adm/prices/recalculate')
                .success(function(data, status, headers, config) {
                  $scope.recalculatePriceStatus = data;
                })
                .error(function(data, status, header, config) {});
            },
            recalculateStatusPrice: function() {
              $http.get('/adm/prices/recalculate/status')
                .success(function(data, status, headers, config) {
                  $scope.recalculatePriceStatus = data;
                })
                .error(function(data, status, header, config) {});
            },
            recalculatePriceInterval: function() {
              $scope.pricesManager.recalculatePrice();
              $interval(function() {
                $scope.pricesManager.recalculateStatusPrice();
              }, 5000)
            },
            updateStatus: function() {
              $http.get('/adm/prices')
                .success(function(data, status, headers, config) {
                  $scope.status = data;
                })
                .error(function(data, status, header, config) {});
            },
            onFileSelect: function($file) {
              $scope.file = $file;
            }
          }

          $scope.pricesManager.list();
          $interval(function() {
            $scope.pricesManager.updateStatus();
          }, 5000)
        })

    }
  ])

})(window, angular, void 0);
