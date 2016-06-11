;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('OrderstatusmessagesCtrl', ['$scope', '$http', '$modal',
    '$location',
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

          $scope.orderStatusMessagesManager = {

            list: function() {
              var requestOk = function(data) {
                var parsedOrderStatusMessages = []
                angular.forEach(data.data.data, function(item,
                  key) {
                  var message = {}
                  message.id = item.id;
                  message.title = item.orderstatusmessagetitle;
                  parsedOrderStatusMessages.push(message)
                })
                parsedOrderStatusMessages.sort(function(first,
                  second) {
                  return first.id > second.id ? 1 : ~0
                })
                $scope.statusMessages = parsedOrderStatusMessages
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

              $http.get(actionToUrlMask['Orderstatusmessages.list'])
                .then(requestOk, requestFail)
            },

            add: function() {
              $modalInstance = $modal.open({
                templateUrl: '/assets/admin/views/modal/orderstatusmessages.add.html',
                controller: ['$scope', function($scope) {

                  $http.get(
                    actionToUrlMask[
                      'Orderstatuses.list']
                  ).then(function(response) {
                    $scope.orderStatuses = response.data
                      .data;
                  });

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

                  $scope.ValidationText = {
                    value: '',
                    invalidMsg: false,
                    validate: function() {
                      if (this.value.length == 0) {
                        this.invalidMsg = translations.VALIDATION_EMPTY_ERROR
                      } else if (this.value.length < 2) {
                        this.invalidMsg = translations.VALIDATION_MINLENGTH_ERROR
                      } else {
                        this.invalidMsg = false
                      }
                    }
                  }
                  $scope.ValidationText.validate()

                  $scope.ValidationForclient = {
                    value: '',
                    invalidMsg: false,
                    validate: function() {}
                  }
                  $scope.ValidationForclient.validate()

                  $scope.ValidationOrderStatus = {
                    value: '',
                    invalidMsg: false,
                    validate: function() {
                      if (this.value.length == 0) {
                        this.invalidMsg = translations.VALIDATION_EMPTY_ERROR
                      } else {
                        this.invalidMsg = false
                      }
                    }
                  }
                  $scope.ValidationOrderStatus.validate()

                  $scope.modalManager = {
                    ok: function() {
                      var orderstatusid = parseInt(
                        $scope.ValidationOrderStatus
                        .value)
                      $http.post(actionToUrlMask[
                            'Orderstatusmessages.add'],
                          angular
                          .toJson({
                            'messagetitle': $scope.ValidationTitle
                              .value,
                            'messagetext': $scope.ValidationText
                              .value,
                            'forclient': ($scope.ValidationForclient
                              .value === "true"),
                            'orderstatusid': orderstatusid,
                          }))
                        .then(function(data) {
                          $modalInstance.close()
                          $location.path(
                            '/admin/orderstatusmessages/' +
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

          $scope.statusMessagesManager = {
            edit: function(catId) {
              $location.path($interpolate(
                '/admin/Orderstatusmessages/{{id}}')({
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
                        'Orderstatusmessages.delete'])({
                        id: catId
                      }))
                      .then(
                        function(data) {
                          for (var i = 0, lCats = $scope.statusMessages
                              .length; i <
                            lCats; ++i) {
                            if ($scope.statusMessages[i].id ==
                              catId) {
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
          $scope.orderStatusMessagesManager.list()

        })

    }
  ])

})(window, angular, void 0);
