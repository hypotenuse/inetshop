;
(function(window, angular, undefined) {
	
	angular.module('homer').factory('upload', [function() {
		
		return {
			options: {

			// (1) AJAX Options:

				// A string containing the URL to which the request is sent.
				// If undefined or empty, it is set to the action property 
				// of the file upload form if available, or else the 
				// URL of the current page
				url: '/',
				
				// The HTTP request method for the file uploads. Can be POST, 
				// PUT or PATCH and defaults to POST
				type: 'POST',

				// The type of data that is expected back from the server
				dataType: 'json',

			// (2) General Options:

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#dropzone
				// dropZone: angular.element(document),
				
				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#pastezone
				// pasteZone: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#fileinput
				// fileInput: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#replacefileinput
				// replaceFileInput: true,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#paramname
				// paramName: 'files[]',

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#formacceptcharset
				// formAcceptCharset: 'utf-8',
				
				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#singlefileuploads
				// By default, each file of a selection is uploaded using an individual request for XHR type uploads
				// Set this option to false to upload file selections in one request each
				singleFileUploads: true,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#limitmultifileuploads
				// limitMultiFileUploads: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#limitmultifileuploadsize
				// limitMultiFileUploadSize: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#limitmultifileuploadsizeoverhead
				// limitMultiFileUploadSizeOverhead: 512,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#sequentialuploads
				// Set this option to true to issue all file upload requests in a sequential order instead of simultaneous requests
				sequentialUploads: false,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#limitconcurrentuploads
				// To limit the number of concurrent uploads, set this option to an integer value greater than 0.
				limitConcurrentUploads: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#forceiframetransport
				// This can be useful for cross-site file uploads, if the Access-Control-Allow-Origin header 
				// cannot be set for the server-side upload handler which is required for cross-site XHR file uploads.
				// forceIframeTransport: false,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#initialiframesrc
				// initialIframeSrc: 'javascript:false;',

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#redirect
				// redirect: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#redirectparamname
				// redirectParamName: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#postmessage
				// postMessage: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#multipart
				multipart: false,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#maxchunksize
				// To upload large files in smaller chunks, set this option to a preferred 
				// maximum chunk size. If set to 0, null or undefined, or the browser does 
				// not support the required Blob API, files will be uploaded as a whole
				// maxChunkSize: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#uploadedbytes
				// When a non-multipart upload or a chunked multipart upload has been aborted, 
				// this option can be used to resume the upload by setting it to the size of 
				// the already uploaded bytes
				// uploadedBytes: undefined,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#recalculateprogress
				// recalculateProgress: true,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#progressinterval
				// progressInterval: 100,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#bitrateinterval
				// bitrateInterval: 500,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#autoupload
				// By default, files added to the widget are uploaded as soon as the user 
				// clicks on the start buttons 
				autoUpload: false,

				// https://github.com/blueimp/jQuery-File-Upload/wiki/Options#formdata
				// formData: function (form) { return form.serializeArray() },

			// (3) Image Preview/Resize Options:

				disableImageLoad: false,
				disableImagePreview: false,
				disableImageHead: false,
				disableImageMetaDataLoad: false,
				disableImageMetaDataSave: false,
				disableImageResize: true,
				disableExif: false,
				disableExifThumbnail: false,
				disableExifSub: false,
				disableExifGps: false,
				loadImageFileTypes: /^image\/(gif|jpeg|png)$/,
				loadImageMaxFileSize: 10000000,
				loadImageNoRevoke: false,
				imageMaxWidth: 5000000,
				imageMaxHeight: 5000000,
				imageMinWidth: undefined,
				imageMinHeight: undefined,
				imageCrop: false,
				imageOrientation: false,
				imageForceResize: undefined,
				imageQuality: undefined,
				//imageType
				imagePreviewName: 'preview',
				previewMaxWidth: 164,
				previewMaxHeight: 136,
				previewMinWidth: 164,
				previewMinHeight: 136,
				previewCrop: false,
				previewOrientation: true,
				previewThumbnail: true,
				previewCanvas: true,

			// (4) Audio preview options

				loadAudioFileTypes: /^audio\/.*$/,
				loadAudioMaxFileSize: undefined,
				audioPreviewName: 'preview',
				disableAudioPreview: true,

			// (5) Video preview options

				loadVideoFileTypes: /^video\/.*$/,
				loadVideoMaxFileSize: undefined,
				videoPreviewName: 'preview',
				disableVideoPreview: true,

			// (6) Validation options

				disableValidation: false,

				// This option limits the number of files that are allowed to be uploaded using this widget
				// By default, unlimited file uploads are allowed
				maxNumberOfFiles: 768,
				minFileSize: undefined,
				maxFileSize: undefined,
				acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i
			},

			option: function(option, value) {
				this.options[option] = value
			}
		}

	}])

})(window, angular, void 0);