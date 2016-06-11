;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('OrderStatusesCtrl', ['$scope', '$http', '$modal', '$location',
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

          $scope.orderStatusManager = {

            list: function() {
              var requestOk = function(data) {
                var parsedOrderStatus = []
                angular.forEach(data.data.data, function(item,
                  key) {
                  var orderStatus = {}
                  orderStatus.id = item[0];
                  orderStatus.title = item[1];
                  parsedOrderStatus.push(orderStatus)
                })
                parsedOrderStatus.sort(function(first, second) {
                  return first.id > second.id ? 1 : ~0
                })
                $scope.orderStatus = parsedOrderStatus
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

              $http.get(actionToUrlMask['Orderstatuses.list'])
                .then(requestOk, requestFail)
            },

            add: function() {
              $modalInstance = $modal.open({
                templateUrl: '/assets/admin/views/modal/orderStatuses.add.html',
                controller: ['$scope', function($scope) {
                  $scope.ValidationTitle = {
                    value: '',
                    invalidMsg: false,
                    validate: function() {
                      if (this.value.length == 0) {
                        this.invalidMsg = translations.VALIDATION_EMPTY_ERROR
                      } else if (this.value.length < 2) {
                        this.invalidMsg = translations.VALIDATION_MINLENGTH_ERROR
                      } else if (this.value.length >
                        250) {
                        this.invalidMsg = $interpolate(
                            translations.VALIDATION_EXCEED_ERROR
                          )
                          ({
                            exceed: this.value.length -
                              250
                          })
                      } else {
                        this.invalidMsg = false
                      }
                    }
                  }
                  $scope.ValidationTitle.validate()


                  $scope.modalManager = {
                    ok: function() {
                      $http.post(actionToUrlMask[
                            'Orderstatuses.add'],
                          angular
                          .toJson({
                            'title': $scope.ValidationTitle
                              .value,
                          }))
                        .then(function(data) {
                          $modalInstance.close()
                          $location.path(
                            '/admin/orderstatuses/' +
                            data.data
                          )
                        })
                    },
                    cancel: function() {
                      $modalInstance.dismiss('cancel')
                    }
                  }
                }]
              })
            }
          }

          $scope.statusManager = {
            edit: function(catId) {
              $location.path($interpolate(
                '/admin/orderstatuses/{{id}}')({
                id: catId
              }))
            },
            delete: function(catId) {
              window.swal({
                  title: translations.NOTIFICATION_WARNING_TITLE,
                  text: translations.NOTIFICATION_WARNING_TEXT,
                  type: 'warning',
                  showCancelButton: true,
                  confirmButtonText: translations.NOTIFICATION_WARNING_BUTTON_CONFIRM,
                  cancelButtonText: translations.NOTIFICATION_WARNING_BUTTON_CANCEL,
                  closeOnConfirm: false,
                  closeOnCancel: true
                },
                function(isConfirm) {
                  if (isConfirm) {
                    $http.delete($interpolate(actionToUrlMask[
                        'Orderstatuses.delete'])({
                        id: catId
                      }))
                      .then(
                        function(data) {
                          for (var i = 0, lCats = $scope.orderStatus
                              .length; i <
                            lCats; ++i) {
                            if ($scope.orderStatus[i].id == catId) {
                              $state.go($state.$current, null, {
                                reload: true
                              });
                              window.swal(
                                translations.NOTIFICATION_DELETE_SUCCESS_TITLE,
                                translations.NOTIFICATION_DELETE_SUCCESS_TEXT,
                                'success'
                              )
                            }
                          }
                        },
                        function(data) {
                          window.swal(
                            translations.NOTIFICATION_ERROR_TITLE,
                            $interpolate(translations.NOTIFICATION_ERROR_REASON_TEXT)
                            ({
                              status: data.status,
                              statusText: data.statusText
                            }),
                            'error'
                          )
                        }
                      )
                  }
                })
            }
          }
          $scope.dtOptions = DTOptionsBuilder.newOptions().withPaginationType(
            'full_numbers').withDisplayLength(10);
          $scope.dtColumnDefs = [
            DTColumnDefBuilder.newColumnDef(0),
            DTColumnDefBuilder.newColumnDef(1)
          ];
          $scope.orderStatusManager.list()

        })

    }
  ])

})(window, angular, void 0);
