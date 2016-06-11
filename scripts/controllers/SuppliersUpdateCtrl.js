;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('SuppliersUpdateCtrl', ['$window', '$scope', '$interpolate',
    '$log',
    '$timeout', '$http', '$modal', '$q', '$translate', 'actionToUrlMask',
    '$state', '$location',
    function($window, $scope, $interpolate, $log, $timeout, $http, $modal,
      $q,
      $translate, actionToUrlMask, $state, $location) {

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
          'NOTIFICATION_SUCCESS_SAVE',
          'NOTIFICATION_ERROR_NOT_UPDATED_TITLE',
          'NOTIFICATION_DELETE_SUCCESS_TITLE',
          'NOTIFICATION_DELETE_SUCCESS_TEXT',
        ])
        .then(function(translations) {

          function lengthExceeded(validationObject) {
            return validationObject.value.length > 250 ?
              $interpolate(translations.VALIDATION_EXCEED_ERROR)
              ({
                exceed: validationObject.value.length - 250
              }) : false
          }


          $scope.TabManager = {
            update: function(event) {
              var state = event.currentTarget.children[0].hash;
              $scope.TabManager.state = state;
            },
            init: function() {
              var statesRE =
                /^\#tab\-(?:params|title|info)$/i,
                state;
              if (state === window.location.hash.match(statesRE)) {
                $scope.TabManager.state = state;
              } else {
                $scope.TabManager.state = '#tab-description';
              }
            }
          };

          $scope.TabManager.init();

          $scope.Validation = {
            errorStack: [],
            changes: false,
            validatorNameRE: /^(?:(title|info|rate))$/
          };


          angular.forEach(['title', 'info', 'rate'],
            function(
              value,
              key, list) {
              var validatorName = value;

              switch (validatorName) {
                case 'info':
                  $scope.Validation[validatorName] = {
                    value: '',
                    invalidMsg: false,
                    validate: function() {}
                  }
                  break;
                case 'title':
                  $scope.Validation[validatorName] = {
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
                  break;
              }
            });


          angular.forEach($scope.Validation, function(value, key, list) {

            var validateOld = value.validate;

            if (validateOld) {
              value.validate = function(_lengthExceeded) {
                validateOld.call(value);
                if (!!value.invalidMsg) {
                  if ($scope.Validation.errorStack.indexOf(value) ==
                    -1) {
                    $scope.Validation.errorStack.push(value);
                  }
                } else {
                  for (var k = 0; k < $scope.Validation.errorStack
                    .length; ++k) {
                    if ($scope.Validation.errorStack[k] === value) {
                      $scope.Validation.errorStack.splice(k, 1);
                    }
                  }
                }
                if ($scope.Validation.errorStack.length === 0) {
                  $scope.Validation.changes = $scope.SuppliersUpdateManager
                    .dataMapDiff(
                      'bool');
                }
              };
            }
          });

          $scope.SuppliersUpdateManager = {
            cancel: function() {
              $location.url('/admin/suppliers');
            },
            changeCurrencies: function(id) {
              $scope.Validation.changes = true;
              value = $scope.currencies[id].rate;
              if (!isNaN(parseFloat(value)) && isFinite(value)) {
                $scope.Validation.changes = true;
              } else {
                $scope.Validation.changes = false;
              }
            },
            clearCurrencies: function(index, id) {
              $scope.Validation.changes = true;
              $scope.currencies[index].rate = null;
              $http.delete($interpolate(actionToUrlMask[
                  'SuppliersCurrency.delete'])({
                  id: this.currentSuppliers.data['supplier'].id,
                  currencyId: id
                }))
                .then(
                  function(data) {

                    window.swal(
                      translations.NOTIFICATION_DELETE_SUCCESS_TITLE,
                      '',
                      'success'
                    )

                  },
                  function(data) {}
                )
            },
            save: function() {

              var spinner = angular.element('.spinner');

              var langRequests = [];
              var nonLangFields = [];
              var mapKeys = [];
              var reasons = [];
              var dataMapDiff = $scope.SuppliersUpdateManager.dataMapDiff(
                'get');
              var requests = successes = 0;
              var vcg = undefined;
              var va = $scope.Validation;

              var suppliersUpdateUrl = $interpolate(
                actionToUrlMask[
                  'Suppliers.update'])({
                id: this.currentSuppliers.data['supplier'].id
              });

              var errorNotifier = function(reasons) {
                var errors = [];
                var errorTitle = translations.NOTIFICATION_ERROR_NOT_UPDATED_TITLE;
                for (var i = 0; i < reasons.length; ++i) {
                  var titleErrors = reasons[i].data['obj.title'];
                  if (titleErrors) {

                    var lang = 'ru';
                    if (angular.isArray(titleErrors[0].msg) &&
                      titleErrors[
                        0].msg[0] == 'error.minLength') {
                      errors.push(
                        $interpolate(translations.NOTIFICATION_ERROR_MINLENGTH_TEXT)
                        ({
                          lang: lang
                        })
                      );
                    }
                  } else if (reasons[i].data ==
                    'TITLE_IS_REQUIRED') {
                    tabLang = JSON.parse(reasons[i]['config'][
                      'data'
                    ]);
                    errors.push(
                      $interpolate(translations.NOTIFICATION_ERROR_MINLENGTH_TEXT)
                      ({
                        lang: tabLang.languagecod
                      }));
                  }
                }
                for (var j = 0; j < errors.length; ++j) {
                  errors[j] = errors[j].replace(/^/, '<li>').replace(
                    /$/, '</li>');
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
                  $scope.SuppliersUpdateManager.dataMapDiff(
                    'update')
                  $window.swal(
                    translations.NOTIFICATION_SUCCESS_SAVE,
                    translations.NOTIFICATION_SUCCESS_UPDATED_TEXT,
                    'success'
                  );
                } else if (response == 'reasons') {
                  errorNotifier(reasons)
                }
              }

              var processRequest = function(resolve, reject,
                llength) {
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
                var nonLangRequest = requestType ==
                  'nonLangRequest'

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
                        var lastRequest = i ==
                          numberOfRequests - 1

                        if (bothRequest) {
                          for (var k = 0; k < mapKeys.length; ++
                            k) {
                            if ((vcg = mapKeys[k].match(va.validatorNameRE))) {
                              fields[vcg[1]] = va[mapKeys[k]]
                                .value
                            }
                          }
                        }

                        fields['currencies'] = []
                        angular.forEach($scope.currencies,
                          function(value, key) {
                            if (value.rate) {
                              currentCurrencies = {}
                              currentCurrencies.id = value.id
                              currentCurrencies.rate =
                                value.rate
                              fields['currencies'].push(
                                currentCurrencies)
                            }
                          });

                        $http.put(suppliersUpdateUrl,
                            angular.toJson(
                              fields))
                          .then(
                            function(response) {
                              successes = successes + 1
                              processRequest(resolve, reject,
                                numberOfRequests)
                              performRequest(
                                ++requestIndex
                              )
                            },
                            function(reason) {
                              reasons.push(reason)
                              processRequest(resolve, reject,
                                numberOfRequests)
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
                if (!vcg[1]) {
                  nonLangFields.push(key)
                }
              }
              performRequests('both')
            },
            init: function(currentSuppliers) {
              this.currentSuppliers = currentSuppliers
              $scope.currencies = currentSuppliers.data[
                'currencies']
              var statusOk = function(status) {
                return status >= 200 && status <= 299
              }
              var va = $scope.Validation
              var names = ['title', 'info']

              if (angular.isObject($scope.SuppliersUpdateManager
                  .dataMap) == false) {
                $scope.SuppliersUpdateManager.dataMap = {}
              }
              angular.forEach(names, function(value, key) {
                $scope.SuppliersUpdateManager.dataMap[
                    value
                  ] = va[
                    value].value =
                  currentSuppliers.data[
                    'supplier'][value] || '';
                va[value].validate()
              })
              $scope.Validation.changes = false;
            },
            dataMapDiff: function(mode) {

              var va = $scope.Validation
              var boolOrGetMode = (mode == 'bool' || mode == 'get')
              var diffList = []
              var vcg = vaName = undefined
              for (var keys = Object.keys(va), i = 0; i < keys.length; ++
                i) {
                if (vcg = keys[i].match(va.validatorNameRE)) {

                  var diffMap = {}
                  if (vcg[1]) {
                    if (boolOrGetMode) {
                      diffMap[vcg[0]] = va[vcg[0]].value
                      diffList.push(diffMap)
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
          $scope.SuppliersUpdateManager.init($scope.currentSuppliers);
        })
    }
  ])

})(window, window.angular, void 0);
