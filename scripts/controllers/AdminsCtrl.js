;
(function(window, angular, undefined) {

  angular.module('homer')

  .controller('AdminsCtrl', ['$scope', '$http', '$modal', '$location',
    '$interpolate', '$translate', 'actionToUrlMask', '$rootScope',
    'DTOptionsBuilder', 'DTColumnDefBuilder', '$state',
    function($scope, $http, $modal, $location, $interpolate, $translate,
      actionToUrlMask, $rootScope, DTOptionsBuilder, DTColumnDefBuilder,
      $state) {
      $translate([
          'ADMINS_MODAL_ADD_VALIDATION_EMPTY_ERROR',
          'ADMINS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR',
          'ADMINS_MODAL_ADD_VALIDATION_EXCEED_ERROR',
          'ADMINS_MODAL_ADD_VALIDATION_MINLENGTH_ERROR_EMAIL',
          'ADMINS_MODAL_ADD_VALIDATION_EMAIL_INVALID',
          'ADMINS_NOTIFICATION_WARNING_TITLE',
          'ADMINS_NOTIFICATION_WARNING_TEXT',
          'ADMINS_NOTIFICATION_WARNING_BUTTON_CONFIRM',
          'ADMINS_NOTIFICATION_WARNING_BUTTON_CANCEL',
          'ADMINS_NOTIFICATION_SUCCESS_TITLE',
          'ADMINS_NOTIFICATION_SUCCESS_TEXT',
          'ADMINS_NOTIFICATION_ERROR_TITLE',
          'ADMINS_NOTIFICATION_ERROR_REASON_TEXT',
          'ADMINS_UPDATE_DUPLICATED_EMAIL',
          'ADMINS_UPDATE_SECTION_TITLE'
        ])
        .then(function(translations) {

          $scope.translations = translations

          $scope.adminsManagers = {

            list: function() {
              var requestOk = function(data) {
                var parsedAdmins = []

                angular.forEach(data.data.data, function(item,
                  key) {
                  var parsedAdmin = {}
                  parsedAdmin.id = item.id;
                  parsedAdmin.name = item.name;
                  parsedAdmins.push(parsedAdmin)
                })
                parsedAdmins.sort(function(first, second) {
                  return first.id > second.id ? 1 : ~0
                })
                $scope.admins = parsedAdmins;

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

              $http.get(actionToUrlMask['Admins.list'])
                .then(requestOk, requestFail)
            },

            add: function() {
              $modalInstance = $modal.open({
                templateUrl: '/assets/admin/views/modal/admins.add.html',
                controller: ['$scope', function($scope) {
                  $scope.ValidationName = {
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
                  $scope.ValidationName.validate()

                  $scope.ValidationEmail = {
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
                  $scope.ValidationEmail.validate()

                  $scope.ValidationPassword = {
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
                  $scope.ValidationPassword.validate()

                  $scope.modalManager = {
                    ok: function() {
                      $http.post(actionToUrlMask[
                          'Admins.add'], angular.toJson({
                          'name': $scope.ValidationName
                            .value,
                          'email': $scope.ValidationEmail
                            .value,
                          'pass': $scope.ValidationPassword
                            .value,
                        }))
                        .error(function(data) {
                          if (data ==
                            'DUPLICATED_EMAIL') {
                            window.swal(
                              translations.ADMINS_UPDATE_DUPLICATED_EMAIL,
                              '',
                              'error'
                            )
                          }
                        })
                        .then(function(data) {
                          $modalInstance.close()
                          $state.go($state.$current,
                            null, {
                              reload: true
                            });
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

          $scope.adminManager = {
            edit: function(catId) {
              $location.path($interpolate('/admin/admins/{{id}}')({
                id: catId
              }))
            },
            delete: function(catId) {
              window.swal({
                  title: translations.ADMINS_NOTIFICATION_WARNING_TITLE,
                  text: translations.ADMINS_NOTIFICATION_WARNING_TEXT,
                  type: 'warning',
                  showCancelButton: true,
                  confirmButtonText: translations.ADMINS_NOTIFICATION_WARNING_BUTTON_CONFIRM,
                  cancelButtonText: translations.ADMINS_NOTIFICATION_WARNING_BUTTON_CANCEL,
                  closeOnConfirm: false,
                  closeOnCancel: true
                },
                function(isConfirm) {
                  if (isConfirm) {
                    $http.delete($interpolate(actionToUrlMask[
                        'Admins.delete'])({
                        id: catId
                      }))
                      .then(
                        function(data) {

                          for (var i = 0, lCats = $scope.admins
                              .length; i <
                            lCats; ++i) {
                            if ($scope.admins[i].id == catId) {
                              $state.go($state.$current, null, {
                                reload: true
                              });
                              return window.swal(
                                translations.ADMINS_NOTIFICATION_SUCCESS_TITLE,
                                translations.ADMINS_NOTIFICATION_SUCCESS_TEXT,
                                'success'
                              )
                            }
                          }
                        },
                        function(data) {
                          window.swal(
                            translations.ADMINS_NOTIFICATION_ERROR_TITLE,
                            $interpolate(translations.ADMINS_NOTIFICATION_ERROR_REASON_TEXT)
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
          $scope.adminsManagers.list()
        })
    }
  ])

})(window, angular, void 0);
