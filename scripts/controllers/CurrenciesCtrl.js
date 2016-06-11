;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('СurrenciesCtrl', ['$scope', '$http', '$modal', '$location',
    '$interpolate', '$translate', 'actionToUrlMask', 'DTOptionsBuilder',
    'DTColumnDefBuilder', '$state',
    function($scope, $http, $modal, $location, $interpolate, $translate,
      actionToUrlMask, DTOptionsBuilder, DTColumnDefBuilder, $state) {

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
          'NOTIFICATION_ERROR_CURRENCIES_MAIN'
        ])
        .then(function(translations) {

          $scope.translations = translations

          $scope.currenciesManager = {

            list: function() {
              var requestOk = function(data) {
                var parsedCurrencies = []
                angular.forEach(data.data.data, function(item,
                  key) {
                  var parsedCurrencie = {}
                  parsedCurrencie.id = item.id;
                  parsedCurrencie.title = item.title;
                  parsedCurrencies.push(parsedCurrencie)
                })
                parsedCurrencies.sort(function(first, second) {
                  return first.id > second.id ? 1 : ~0
                })
                $scope.currencies = parsedCurrencies
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

              $http.get(actionToUrlMask['Currencies.list'])
                .then(requestOk, requestFail)
            },

            add: function() {
              $modalInstance = $modal.open({
                templateUrl: '/assets/admin/views/modal/currencies.add.html',
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

                  $scope.ValidationCross = {
                    value: '',
                    invalidMsg: false,
                    validate: function() {
                      if (!isNaN(parseFloat(this.value)) &&
                        isFinite(this.value)) {
                        this.invalidMsg = false
                      } else {
                        this.invalidMsg = translations.VALIDATION_NON_NUMBERS_ERROR
                      }
                      if (parseFloat(this.value) > 100) {
                        this.invalidMsg = translations.VALIDATION_NUMBER_EXCEED_ERROR
                      }
                    }
                  }
                  $scope.ValidationCross.validate()

                  $scope.ValidationMain = {
                    value: '',
                    invalidMsg: false,
                    checkMain: function() {
                      if (this.value) {
                        $scope.ValidationCross
                          .value = 1;
                        $scope.ValidationCross.validate();
                      }
                    },
                    validate: function() {}
                  }
                  $scope.ValidationMain.validate()

                  $scope.modalManager = {
                    ok: function() {
                      $http.post(actionToUrlMask[
                          'Currencies.add'], angular.toJson({
                          'title': $scope.ValidationTitle
                            .value,
                          'main': $scope.ValidationMain
                            .value ? true : false,
                          'cros': $scope.ValidationCross
                            .value,
                        }))
                        .success(function(data, status,
                          headers, config) {
                          $modalInstance.close()
                          $state.go($state.$current,
                            null, {
                              reload: true
                            });
                        })
                        .error(function(data, status,
                          headers, config) {
                          if (data ==
                            "Main currency already exist"
                          ) {
                            swal(
                              translations.NOTIFICATION_ERROR_TITLE,
                              translations.NOTIFICATION_ERROR_CURRENCIES_MAIN,
                              'error'
                            );
                          }
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

          $scope.currencyManager = {
            edit: function(catId) {
              $location.path($interpolate(
                '/admin/currencies/{{id}}')({
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
                        'Currencies.delete'])({
                        id: catId
                      }))
                      .then(
                        function(data) {
                          for (var i = 0, lCats = $scope.currencies
                              .length; i <
                            lCats; ++i) {
                            if ($scope.currencies[i].id == catId) {
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
          $scope.currenciesManager.list()

        })

    }
  ])

})(window, angular, void 0);
