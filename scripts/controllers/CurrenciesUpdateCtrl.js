;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('CurrenciesUpdateCtrl', ['$window', '$scope', '$interpolate',
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
          'NOTIFICATION_ERROR_CURRENCIES_MAIN'
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
                /^\#tab\-(?:params|description)$/i,
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
            validatorNameRE: /^(?:(title|cros|main))$/
          };

          angular.forEach(['cros'], function(value) {
            $scope.Validation[value] = {
              validate: angular.noop
            }
          })

          angular.forEach(['title', 'main', 'cros'],
            function(
              value,
              key, list) {
              var validatorName = value;

              switch (validatorName) {
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
                case 'cros':
                  $scope.Validation[validatorName] = {
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
                  break;
                case 'main':
                  $scope.Validation[validatorName] = {
                    value: '',
                    invalidMsg: false,
                    validate: function() {}
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
                  $scope.Validation.changes = $scope.CurrenciesUpdateManager
                    .dataMapDiff(
                      'bool');
                }
              };
            }
          });

          $scope.CurrenciesUpdateManager = {
            cancel: function() {
              $location.url('/admin/currencies');
            },
            save: function() {

              var spinner = angular.element('.spinner');

              var langRequests = [];
              var nonLangFields = [];
              var mapKeys = [];
              var reasons = [];
              var dataMapDiff = $scope.CurrenciesUpdateManager.dataMapDiff(
                'get');
              var requests = successes = 0;
              var vcg = undefined;
              var va = $scope.Validation;

              var currenciesUpdateUrl = $interpolate(
                actionToUrlMask[
                  'Currencies.update'])({
                id: this.currentCurrencies.data.id
              });

              var errorNotifier = function(reasons) {
                var errors = [];
                var errorTitle = translations.NOTIFICATION_ERROR_NOT_UPDATED_TITLE;
                for (var i = 0; i < reasons.length; ++i) {
                  var titleErrors = reasons[i].data['obj.name'];
                  if (titleErrors) {
                    var lang = 'ru';
                    if (angular.isArray(titleErrors[0].msg) &&
                      titleErrors[
                        0].msg[0] == 'error.minLength') {
                      errors.push(
                        $interpolate(translations.NOTIFICATION_ERROR_MINLENGTH_TEXT)
                        ({
                          lang: tabLang.languagecod
                        })
                      );
                    }
                  } else if (reasons[i].data ==
                    'Main currency already exist') {
                    tabLang = JSON.parse(reasons[i]['config'][
                      'data'
                    ]);
                    errors.push(
                      $interpolate(translations.NOTIFICATION_ERROR_CURRENCIES_MAIN)
                      ({
                        lang: tabLang.languagecod
                      }));
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
                  $scope.CurrenciesUpdateManager.dataMapDiff(
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

                        fields['main'] = va[
                          'main'].value == "" ? false : va[
                          'main'].value;
                        $http.put(currenciesUpdateUrl,
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
            init: function(currentCurrencies) {
              var statusOk = function(status) {
                return status >= 200 && status <= 299
              }
              var va = $scope.Validation
              this.currentCurrencies = currentCurrencies

              var names = ['title', 'cros', 'main']

              if (angular.isObject($scope.CurrenciesUpdateManager
                  .dataMap) == false) {
                $scope.CurrenciesUpdateManager.dataMap = {}
              }
              angular.forEach(names, function(value, key) {
                $scope.CurrenciesUpdateManager.dataMap[
                    value
                  ] = va[
                    value].value =
                  currentCurrencies.data[value] || '';
                va[value].validate()
              })
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
          $scope.CurrenciesUpdateManager.init($scope.currentCurrencies);
        })
    }
  ])

})(window, window.angular, void 0);
