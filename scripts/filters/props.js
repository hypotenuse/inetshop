
/*
 * AngularJS default filter with the following expression:
 * "person in people | filter: {name: $select.search, age: $select.search}"
 * performs a AND between 'name: $select.search' and 'age: $select.search'.
 * We want to perform a OR.
 */
(function (window, angular, undefined) {
	angular.module('homer')
		.filter('propsFilter',  function() {
			return function(items, props) {
				var out = [];
				if (angular.isArray(items)) {
					items.forEach(function(item) {
						var itemMatches = false;
						var keys = Object.keys(props);
						for (var i = 0; i < keys.length; i++) {
							var prop = keys[i];
							var text = props[prop].toLowerCase();
							if (item[prop].toString().toLowerCase().indexOf(text) !== -1) {
								itemMatches = true;
								break;
							}
						}
						if (itemMatches) out.push(item);
					});
				} else out = items;
				return out;
			}
		})
})(window, window.angular, void 0);