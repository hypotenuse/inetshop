;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('CostscalesCtrl', ['$scope', '$http', '$modal', '$location',
    '$interpolate', '$translate', 'actionToUrlMask', '$state', '$log',
    function($scope, $http, $modal, $location, $interpolate, $translate,
      actionToUrlMask, $state, $log) {

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
          'START_SHOULD_BE_SMALLER_THEN_FINISH',
          'SCALE_RANGE_CLUTTERS_WITH_EXISTING_RANGE'
        ])
        .then(function(translations) {
          $scope.add = false;

          $scope.translations = translations

          $scope.costscalesManager = {

            list: function() {
              var requestOk = function(data) {
                $scope.costscales = data.data.data;
                angular.forEach($scope.costscales, function(item,
                  key) {
                  item.edit = true;
                  item.save = false;
                });
              }
              var requestFail = function(data) {
                $log.warn('Something went wrong: (status ' + data
                  .status + data.statusText +
                  ')')
              }

              $http.get(actionToUrlMask['Costscales.list'])
                .then(requestOk, requestFail)
            },

            add: function() {
              var cost = {}
              cost.id = 0;
              cost.start = 0;
              cost.finish = 0;
              cost.percent = 0;
              cost.invalidMsg = false;
              $scope.costscales.push(cost);
              $scope.add = true;
            }
          }

          $scope.costsManager = {
            save: function() {
              angular.forEach($scope.costscales, function(item,
                key) {
                if (item.id == 0) {
                  $http.post(actionToUrlMask[
                      'Costscales.add'], angular.toJson({
                      'start': parseInt(item.start),
                      'finish': parseInt(item.finish),
                      'percent': parseInt(item.percent),
                    }))
                    .success(function(data, status,
                      headers, config) {
                      $scope.add = false;
                      $state.go($state.$current, null, {
                        reload: true
                      });
                      swal(
                        translations.NOTIFICATION_SUCCESS_SAVE,
                        translations.NOTIFICATION_SUCCESS_UPDATED_TEXT,
                        'success'
                      );
                    })
                    .error(function(data, status,
                      headers, config) {
                      if (data ==
                        "SCALE_RANGE_CLUTTERS_WITH_EXISTING_RANGE"
                      ) {
                        swal({
                          type: 'error',
                          title: translations.NOTIFICATION_ERROR_TITLE,
                          text: translations.SCALE_RANGE_CLUTTERS_WITH_EXISTING_RANGE,
                        });
                      } else {
                        if (data.obj) {
                          switch (data.obj[0].msg + "") {
                            case 'START_SHOULD_BE_SMALLER_THEN_FINISH':
                              swal({
                                type: 'error',
                                title: translations.NOTIFICATION_ERROR_TITLE,
                                text: translations.START_SHOULD_BE_SMALLER_THEN_FINISH,
                              });
                              break;
                          }
                        }
                      }
                    })
                }
              })
            },
            editItem: function(cost) {
              cost.edit = false;
              cost.save = true;
            },
            saveItem: function(cost) {
              var updateUrl = $interpolate(
                actionToUrlMask[
                  'Costscales.update'])({
                id: cost.id
              });

              fields = {
                'id': parseInt(cost.id),
                'start': parseInt(cost.start),
                'finish': parseInt(cost.finish),
                'percent': parseInt(cost.percent),
              }
              $http.put(updateUrl,
                  angular.toJson(
                    fields))
                .success(function(data, status,
                  headers, config) {
                  $scope.add = false;
                  $state.go($state.$current, null, {
                    reload: true
                  });
                  cost.edit = true;
                  cost.save = false;
                  swal(
                    translations.NOTIFICATION_SUCCESS_SAVE,
                    translations.NOTIFICATION_SUCCESS_UPDATED_TEXT,
                    'success'
                  );
                })
                .error(function(data, status,
                  headers, config) {
                  if (data ==
                    "SCALE_RANGE_CLUTTERS_WITH_EXISTING_RANGE"
                  ) {
                    swal({
                      type: 'error',
                      title: translations.NOTIFICATION_ERROR_TITLE,
                      text: translations.SCALE_RANGE_CLUTTERS_WITH_EXISTING_RANGE,
                    });
                  } else {
                    if (data.obj) {
                      switch (data.obj[0].msg + "") {
                        case 'START_SHOULD_BE_SMALLER_THEN_FINISH':
                          swal({
                            type: 'error',
                            title: translations.NOTIFICATION_ERROR_TITLE,
                            text: translations.START_SHOULD_BE_SMALLER_THEN_FINISH,
                          });
                          break;
                      }
                    }
                  }
                })
            },
            change: function(key) {
              id = $scope.costscales.length - 1;
              if (parseInt($scope.costscales[id][key]) < 0) {
                $scope.costscales[id][key] *= -1;
              }
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
                        'Costscales.delete'])({
                        id: catId
                      }))
                      .then(
                        function(data) {
                          for (var i = 0, lCats = $scope.costscales
                              .length; i <
                            lCats; ++i) {
                            if ($scope.costscales[i].id == catId) {
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

          $scope.costscalesManager.list()

        })

    }
  ])

})(window, angular, void 0);
