;
(function(window, angular, undefined) {

  angular.module('homer').controller('NewsFileUploadCtrl', [

    '$scope', '$http', '$log', '$interpolate', '$timeout', '$translate',
    'actionToUrlMask', 'upload',
    function($scope, $http, $log, $interpolate, $timeout, $translate,
      actionToUrlMask, upload) {

      $translate([
          'NEWS_UPDATE_NOTIFICATION_ERROR_UPLOAD_ERROR_TITLE',
          'NEWS_UPDATE_NOTIFICATION_ERROR_REASON_TEXT',
          'NEWS_UPDATE_UPLOAD_SUCCESSFULLY_UPLOADED'
        ])
        .then(function(translations) {

          var news = $scope.currentNews.data.news
          var catid = news.id

          upload.option('url', $interpolate(actionToUrlMask[
            'News.addPicture'])({
            id: catid
          }))
          $scope.options = upload.options

          // https://github.com/blueimp/jQuery-File-Upload/wiki/Options#done
          $scope.$on('fileuploaddone', function(Event, data) {

            var nocacheid = Math.random().toString().split('.')[1]
            var result = data.result
            var oldFile = data.files[0]

            var ext = oldFile.type.split('/')[1]
            if (ext == 'jpeg') ext = 'jpg'

            $scope.replace(
              [oldFile], [{

                name: translations.NEWS_UPDATE_UPLOAD_SUCCESSFULLY_UPLOADED,

                thumbnailUrl: $interpolate(actionToUrlMask[
                  'News.picture'])({
                  id: catid,
                  extension: ext,
                  nocacheid: nocacheid
                }),
                url: $interpolate(actionToUrlMask[
                  'News.picture'])({
                  id: catid,
                  extension: ext,
                  nocacheid: nocacheid
                }),
                deleteUrl: $interpolate(actionToUrlMask[
                  'News.deletePicture'])({
                  id: catid
                }),
                deleteType: 'GET',
                size: undefined
              }])

            $scope.disableAddFileButton = $scope.disableLoadButton =
              true

          })

          // https://github.com/blueimp/jQuery-File-Upload/wiki/Options#fail
          $scope.$on('fileuploadfail', function(Event, data) {
            if (data.errorThrown == 'error') {
              window.swal(
                $interpolate(translations.NEWS_UPDATE_NOTIFICATION_ERROR_UPLOAD_ERROR_TITLE)
                ({
                  file: data.files[0].name
                }),
                $interpolate(translations.NEWS_UPDATE_NOTIFICATION_ERROR_REASON_TEXT)
                ({
                  status: data.status,
                  statusText: data.statusText
                }),
                'error'
              )
            } else if (data.errorThrown == 'abort') {
              $scope.disableAddFileButton = $scope.disableLoadButton =
                false
            }
          })

          // https://github.com/blueimp/jQuery-File-Upload/wiki/Options#add
          $scope.$on('fileuploadadd', function(Event, data) {
            $scope.disableAddFileButton = true
            while ($scope.queue.length) {
              $scope.queue.shift()
            }
          })

          // https://github.com/blueimp/jQuery-File-Upload/wiki/Options#processalways
          $scope.$on('fileuploadprocessalways', function(Event, data) {
            var processedFile = data.files[data.index]
            if (processedFile.error) {
              $scope.disableLoadButton = true
            } else {
              $scope.disableLoadButton = false
            }
          })

          $scope.disableAddFileButton = $scope.disableLoadButton =
            false
          $scope.cancelUploadButton = false
          $scope.loadingFiles = false
          $scope.queue = []

          if (news.havePicture) {

            $scope.disableAddFileButton = $scope.disableLoadButton =
              true

            var pictureUrl = $scope.currentNews.data.pictureUrl
            var fileName = pictureUrl.match(/\d+?\.\w{3,3}/i)[0]

            $scope.queue.push({
              name: fileName,
              thumbnailUrl: pictureUrl,
              url: pictureUrl,
              deleteUrl: $interpolate(actionToUrlMask[
                'News.deletePicture'])({
                id: catid
              }),
              deleteType: 'GET',
              size: undefined
            })
          }

        })
    }
  ])

})(window, angular, void 0);
