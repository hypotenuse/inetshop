;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('AdminsUpdateCtrl', ['$window', '$scope', '$interpolate',
    '$log',
    '$timeout', '$http', '$modal', '$q', '$translate', 'actionToUrlMask',
    function($window, $scope, $interpolate, $log, $timeout, $http, $modal,
      $q,
      $translate, actionToUrlMask) {

      $translate([
          'ADMINS_UPDATE_VALIDATION_EXCEED_ERROR',
          'ADMINS_UPDATE_VALIDATION_EMPTY_ERROR',
          'ADMINS_UPDATE_VALIDATION_MINLENGTH_ERROR',
          'ADMINS_UPDATE_VALIDATION_UNSUPPORTED_CHARACTERS_ERROR',
          'ADMINS_UPDATE_VALIDATION_INVALID_NUMBER_FORMAT_ERROR',
          'ADMINS_UPDATE_VALIDATION_NON_NUMBERS_ERROR',
          'ADMINS_UPDATE_VALIDATION_NUMBER_EXCEED_ERROR',
          'ADMINS_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE',
          'ADMINS_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TITLE',
          'ADMINS_UPDATE_NOTIFICATION_ERROR_NOT_FOUND_TEXT',
          'ADMINS_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT',
          'ADMINS_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT',
          'ADMINS_UPDATE_NOTIFICATION_ERROR_REASON_TEXT',
          'ADMINS_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE',
          'ADMINS_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT',
          'ADMINS_UPDATE_SECTION_TITLE',
          'ADMINS_UPDATE_TAB_DESCRIPTION',
          'ADMINS_UPDATE_TITLE',
          'ADMINS_UPDATE_DESCRIPTION',
          'ADMINS_UPDATE_BUTTON_SAVE',
          'ADMINS_MODAL_ADD_VALIDATION_EMPTY_ERROR',
          'ADMINS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR',
          'ADMINS_MODAL_ADD_VALIDATION_EXCEED_ERROR',
          'ADMINS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR_EMAIL',
          'ADMINS_MODAL_ADD_VALIDATION_EMAIL_INVALID',
          'ADMINS_UPDATE_OLD_PASSWORD_IS_INCORRECT'
        ])
        .then(function(translations) {

          function lengthExceeded(validationObject) {
            return validationObject.value.length > 250 ?
              $interpolate(translations.ADMIN_UPDATE_VALIDATION_EXCEED_ERROR)
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
            validatorNameRE: /^(?:(name|email|old_pass|pass))$/
          };

          angular.forEach(['name', 'email', 'pass', 'old_pass'],
            function(
              value,
              key, list) {
              var validatorName = value;

              switch (validatorName) {
                case 'name':
                  $scope.Validation[validatorName] = {
                    value: '',
                    invalidMsg: false,
                    validate: function() {

                      if (this.value.length == 0) {
                        this.invalidMsg = translations.ADMINS_MODAL_ADD_VALIDATION_EMPTY_ERROR
                      } else if (this.value.length < 2) {
                        this.invalidMsg = translations.ADMINS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR
                      } else if (this.value.length >
                        250) {
                        this.invalidMsg = $interpolate(
                            translations.ADMINS_MODAL_ADD_VALIDATION_EXCEED_ERROR
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
                case 'email':
                  $scope.Validation[validatorName] = {
                    value: '',
                    invalidMsg: false,
                    checkEmail: function(email) {
                      var re = /\S+@\S+\.\S+/;
                      return re.test(email);
                    },
                    validate: function() {
                      if (this.value.length == 0) {
                        this.invalidMsg = translations.ADMINS_MODAL_ADD_VALIDATION_EMPTY_ERROR
                      } else if (this.value.length < 2) {
                        this.invalidMsg = translations.ADMINS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR
                      } else if (this.value.length >
                        250) {
                        this.invalidMsg = $interpolate(
                            translations.ADMINS_MODAL_ADD_VALIDATION_EXCEED_ERROR
                          )
                          ({
                            exceed: this.value.length -
                              250
                          })
                      } else if (!this.checkEmail(this.value)) {
                        this.invalidMsg = translations.ADMINS_MODAL_ADD_VALIDATION_EMAIL_INVALID
                      } else {
                        this.invalidMsg = false
                      }
                    }
                  }
                  break;
                default:
                  $scope.Validation[validatorName] = {
                    value: '',
                    invalidMsg: false,
                    validate: function() {
                      if (this.value.length == 0) {
                        this.invalidMsg = translations.ADMINS_MODAL_ADD_VALIDATION_EMPTY_ERROR
                      } else if (this.value.length < 8) {
                        this.invalidMsg = translations.ADMINS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR_EMAIL
                      } else if (this.value.length >
                        250) {
                        this.invalidMsg = $interpolate(
                            translations.ADMINS_MODAL_ADD_VALIDATION_EXCEED_ERROR
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
                  $scope.Validation.changes = $scope.AdminsUpdateManager
                    .dataMapDiff(
                      'bool');
                }
              };
            }
          });

          $scope.AdminsUpdateManager = {
            save: function() {

              var spinner = angular.element('.spinner');

              var langRequests = [];
              var nonLangFields = [];
              var mapKeys = [];
              var reasons = [];
              var dataMapDiff = $scope.AdminsUpdateManager.dataMapDiff(
                'get');
              var requests = successes = 0;
              var vcg = undefined;
              var va = $scope.Validation;
              var adminsUpdateUrl = $interpolate(actionToUrlMask[
                'Admins.update'])({
                id: this.currentAdmin.data.id
              });

              var errorNotifier = function(reasons) {
                var errors = [];
                var errorTitle = translations.ADMINS_UPDATE_NOTIFICATION_ERROR_NOT_UPDATED_TITLE;
                for (var i = 0; i < reasons.length; ++i) {
                  var titleErrors = reasons[i].data['obj.name'];
                  if (titleErrors) {

                    var lang = 'ru';
                    if (titleErrors[0].msg == 'error.required') {
                      errors.push(
                        $interpolate(translations.ADMINS_UPDATE_NOTIFICATION_ERROR_REQUIRED_TEXT)
                        ({
                          lang: lang
                        })
                      );
                    } else if (angular.isArray(titleErrors[0].msg) &&
                      titleErrors[
                        0].msg[0] == 'error.minLength') {
                      errors.push(
                        $interpolate(translations.ADMINS_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT)
                        ({
                          lang: lang
                        })
                      );
                    }
                  } else if (reasons[i].data ==
                    'NAME_IS_REQUIRED') {
                    tabLang = JSON.parse(reasons[i]['config'][
                      'data'
                    ]);
                    errors.push(
                      $interpolate(translations.ADMINS_UPDATE_NOTIFICATION_ERROR_MINLENGTH_TEXT)
                      ({
                        lang: tabLang.languagecod
                      }));
                  } else if (reasons[i].data ==
                    'OLD_PASSWORD_IS_INCORRECT') {
                    tabLang = JSON.parse(reasons[i]['config'][
                      'data'
                    ]);
                    errors.push(
                      $interpolate(translations.ADMINS_UPDATE_OLD_PASSWORD_IS_INCORRECT)
                      ());
                  } else {
                    errors.push(
                      $interpolate(translations.ADMINS_UPDATE_NOTIFICATION_ERROR_REASON_TEXT)
                      ({
                        status: reasons[i].status,
                        statusText: reasons[i].statusText
                      })
                    );
                  }
                  // }
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
                  $scope.AdminsUpdateManager.dataMapDiff('update')
                  $window.swal(
                    translations.ADMINS_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TITLE,
                    translations.ADMINS_UPDATE_NOTIFICATION_SUCCESS_UPDATED_TEXT,
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

                        $http.put(adminsUpdateUrl, angular.toJson(
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
            init: function(currentAdmin) {
              var statusOk = function(status) {
                return status >= 200 && status <= 299
              }
              var va = $scope.Validation
              this.currentAdmin = currentAdmin


              var names = ['name', 'email', 'pass', 'old_pass']

              if (angular.isObject($scope.AdminsUpdateManager
                  .dataMap) == false) {
                $scope.AdminsUpdateManager.dataMap = {}
              }
              angular.forEach(names, function(value, key) {
                $scope.AdminsUpdateManager.dataMap[
                    value
                  ] = va[
                    value].value =
                  currentAdmin.data[value] || '';
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
          $scope.AdminsUpdateManager.init($scope.currentAdmin);
        })
    }
  ])

})(window, window.angular, void 0);
