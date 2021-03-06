;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('OrderStatusesUpdateCtrl', ['$window', '$scope', '$interpolate',
    '$log',
    '$timeout', '$http', '$modal', '$q', '$translate', 'actionToUrlMask',
    '$location',
    function($window, $scope, $interpolate, $log, $timeout, $http, $modal,
      $q,
      $translate, actionToUrlMask, $location) {

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
          'NOTIFICATION_SUCCESS_SAVE',
        ])
        .then(function(translations) {

          function lengthExceeded(validationObject) {
            return validationObject.value.length > 250 ?
              $interpolate(translations.VALIDATION_EXCEED_ERROR)
              ({
                exceed: validationObject.value.length - 250
              }) : false
          }

          $scope.TabLangManager = {
            langList: ['ru', 'en', 'uk'],
            update: function(event) {
              angular.forEach(this, function(value, key) {
                if (this.langList.indexOf(key) > -1) {
                  delete this[key]
                }
              }, this)
              this[event.currentTarget.innerHTML.toLowerCase()] =
                true;
            }
          }

          $scope.TabLangManager.ru = true

          $scope.TabManager = {
            update: function(event) {
              var state = event.currentTarget.children[0].hash;
              $scope.TabManager.state = state;
            },
            init: function() {
              var statesRE =
                /^\#tab\-(?:params|title)$/i,
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
            validatorNameRE: /^(?:(title)(Ru|En|Uk)|(sendmessageClient|sendmessageAdmin))$/
          };

          angular.forEach(['sendmessageClient',
            'sendmessageAdmin'
          ], function(value) {
            $scope.Validation[value] = {
              validate: angular.noop
            }
          })


          angular.forEach($scope.TabLangManager.langList, function(
            value, key,
            list) {
            var capitalized = value.charAt(0).toUpperCase() + value
              .slice(1);
            angular.forEach(['title'], function(value,
              key, list) {
              var validatorName = value + capitalized;
              $scope.Validation[validatorName] = {
                value: '',
                invalidMsg: false,
                validate: function(_lengthExceeded) {
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
              };

            });
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
                  $scope.Validation.changes = $scope.OrderStatusesUpdateManager
                    .dataMapDiff(
                      'bool');
                }
              };
            }
          });

          $scope.OrderStatusesUpdateManager = {
            cancel: function() {
              $location.url('/admin/orderstatuses');
            },
            save: function() {

              var spinner = angular.element('.spinner');

              var langRequests = [];
              var nonLangFields = [];
              var mapKeys = [];
              var reasons = [];
              var dataMapDiff = $scope.OrderStatusesUpdateManager.dataMapDiff(
                'get');
              var requests = successes = 0;
              var vcg = undefined;
              var va = $scope.Validation;
              var orderUpdateUrl = $interpolate(actionToUrlMask[
                'Orderstatuses.update'])({
                id: this.currentOrderStatus.data.orderStatus.id
              });

              var errorNotifier = function(reasons) {
                var errors = [];
                var errorTitle = translations.NOTIFICATION_ERROR_NOT_UPDATED_TITLE;
                for (var i = 0; i < reasons.length; ++i) {
                  var titleErrors = reasons[i].data['obj.title'];
                  if (titleErrors) {
                    var lang = reasons[i].config.data.match(
                      /\"languagecod\":\"(.+?)\"/)[1].toUpperCase();
                    if (titleErrors[0].msg == 'error.required') {
                      errors.push(
                        $interpolate(translations.NOTIFICATION_ERROR_REQUIRED_TEXT)
                        ({
                          lang: lang
                        })
                      );
                    } else if (angular.isArray(titleErrors[0].msg) &&
                      titleErrors[
                        0].msg[0] == 'error.minLength') {
                      errors.push(
                        $interpolate(translations.NOTIFICATION_ERROR_MINLENGTH_TEXT)
                        ({
                          lang: lang
                        })
                      );
                    }
                  } else {
                    errors.push(
                      $interpolate(translations.NOTIFICATION_ERROR_REASON_TEXT)
                      ({
                        status: reasons[i].status,
                        statusText: reasons[i].statusText
                      })
                    );
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
                  $scope.OrderStatusesUpdateManager.dataMapDiff(
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

                        if (bothRequest && !lastRequest ||
                          langRequest) {

                          var language = langRequests[i]
                          fields['languagecod'] = language.toLowerCase()

                          for (var k = 0; k < mapKeys.length; ++
                            k) {
                            if ((vcg = mapKeys[k].match(va.validatorNameRE)) &&
                              vcg[
                                2] == language) {
                              fields[vcg[1]] = va[mapKeys[k]]
                                .value
                            }
                          }
                        }

                        if (bothRequest && lastRequest ||
                          nonLangRequest) {

                          fields['languagecod'] = 'ru'

                          for (var j = 0; j < nonLangFields.length; ++
                            j) {
                            var nonLangField = nonLangFields[
                              j]

                            fields[nonLangField] = 'value' in
                              va[nonLangField] ? va[
                                nonLangField].value : va[
                                nonLangField].state

                          }
                        }
                        fields['sendmessageAdmin'] = va[
                          'sendmessageAdmin'].value;
                        fields['sendmessageClient'] = va[
                          'sendmessageClient'].value;

                        $http.put(orderUpdateUrl, angular.toJson(
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

                if (vcg[1] && vcg[2]) {
                  if (langRequests.indexOf(vcg[2]) == -1) {
                    langRequests.push(vcg[2])
                  }
                } else if (!vcg[1] && !vcg[2]) {
                  nonLangFields.push(key)
                }
              }

              if (langRequests.length > 0 && nonLangFields.length >
                0) {
                performRequests('both')
              } else if (langRequests.length > 0) {
                performRequests('langRequests')
              } else if (nonLangFields.length > 0) {
                performRequests('nonLangRequest')
              }
            },
            init: function(currentOrderStatus) {
              var statusOk = function(status) {
                return status >= 200 && status <= 299
              }
              var va = $scope.Validation
              this.currentOrderStatus = currentOrderStatus
              $scope.sendmessageClient = currentOrderStatus.data.orderStatus[
                'sendmessageClient'];
              $scope.sendmessageAdmin = currentOrderStatus.data.orderStatus[
                'sendmessageAdmin'];

              angular.forEach($scope.TabLangManager.langList,
                function(lang, key) {

                  var names = ['title', 'sendmessageAdmin',
                    'sendmessageClient'
                  ]
                  var capitalized = lang.charAt(0).toUpperCase() +
                    lang.slice(1)

                  if (angular.isObject($scope.OrderStatusesUpdateManager
                      .dataMap[
                        lang]) == false) {
                    $scope.OrderStatusesUpdateManager.dataMap[
                      lang] = {}
                  }
                  angular.forEach(names, function(value, key) {

                    if (value == 'title') {
                      if (currentOrderStatus.data.data[lang]) {
                        $scope.OrderStatusesUpdateManager.dataMap[
                            lang][
                            value
                          ] = va[
                            value + capitalized].value =
                          currentOrderStatus.data.data[
                            lang][value] || ''
                      } else {
                        $scope.OrderStatusesUpdateManager.dataMap[
                          lang][
                          value
                        ] = ''
                      }
                    } else {
                      va[value].value =
                        currentOrderStatus.data.orderStatus[
                          value] || false;
                    }
                  })
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

                  if (vcg[1] && vcg[2]) {
                    if (angular.equals(va[vcg[0]].value, this.dataMap[
                          vcg[2].toLowerCase()]
                        [vcg[1]]) == false) {
                      if (boolOrGetMode) {
                        diffMap[vcg[0]] = va[vcg[0]].value
                        diffList.push(diffMap)
                      } else {
                        this.dataMap[vcg[2].toLowerCase()][vcg[1]] =
                          va[vcg[0]].value
                      }
                    }
                  } else if (vaName = vcg[3] || vcg[5]) {
                    if (angular.equals(va[vaName].value, this.dataMap[
                        vaName]) ==
                      false) {
                      if (boolOrGetMode) {
                        diffMap[vaName] = va[vaName].value
                        diffList.push(diffMap)
                      } else {
                        this.dataMap[vaName] = va[vaName].value
                      }
                    }
                  } else if (vaName = vcg[4]) {
                    if (angular.equals(va[vaName].state, this.dataMap[
                        vaName]) ==
                      false) {
                      if (boolOrGetMode) {
                        diffMap[vaName] = va[vaName].state
                        diffList.push(diffMap)
                      } else {
                        this.dataMap[vaName] = va[vaName].state
                      }
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
          $scope.OrderStatusesUpdateManager.init($scope.currentOrderStatus);
        })
    }
  ])

})(window, window.angular, void 0);
