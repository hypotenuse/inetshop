;
(function(window, angular, undefined) {

	// host, port and protocol should be the same (SOP)
	var base = '';

	angular.module('homer').constant('actionToUrlMask', {

		'Goods.add': base + '/adm/goods',
		'Goods.list': base + '/adm/goods',
		'Goods.view': base + '/adm/goods/:id',
		'Goods.delete': base + '/adm/goods/:id',
		'Goods.update': base + '/adm/goods/:id',
		'Goods.setCategories': base + '/adm/goods/{{id}}/categories',
		'Goods.updatePrices': base + '/adm/goods/priceupdate',

		'Pictures.list': base + '/adm/goods/pictures/:goodId',
		'Pictures.add': base + '/adm/goods/pictures/:goodId',
		'Pictures.view': base + '/goods/pictures/{{id}}.{{extension}}',
		'Pictures.thumb': base + '/thumbs/goods/{{id}}.jpg',
		'Pictures.delete': base + '/adm/pictures/{{id}}',

		'Categories.list': base + '/adm/categories',
		'Categories.list.all': base + '/adm/categories/selectall',
		'Categories.list.parent': base + '/adm/categories?parent={{parentId}}',
		'Categories.delete': base + '/adm/categories/{{id}}',
		'Categories.update': base + '/adm/categories/{{id}}',
		'Categories.add': base + '/adm/categories',
		'Categories.view': base + '/adm/categories/{{id}}',
		'Categories.select': base + '/adm/categories/select/{{id}}',

		'Categories.picture': base + '/categories/pictures/{{id}}.{{extension}}?nocacheid={{nocacheid}}',
		'Categories.addPicture': base + '/adm/categories/addpicture/{{id}}',
		'Categories.deletePicture': base + '/adm/categories/deletepicture/{{id}}',

		'Categories.thumb': base + '/thumbs/categories/{{id}}.jpg?nocacheid={{nocacheid}}',
		'Categories.addCategoryPicture': base + '/adm/categories/addcatpicture/{{id}}',
		'Categories.deleteCategoryPicture': base + '/adm/categories/deletecatpicture/{{id}}',

		'Manufacturers.delete': base + '/adm/manufacturers/{{id}}',
		'Manufacturers.update': base + '/adm/manufacturers/{{id}}',
		'Manufacturers.add': base + '/adm/manufacturers',
		'Manufacturers.list': base + '/adm/manufacturers',
		'Manufacturers.view': base + '/adm/manufacturers/{{id}}',

		'News.list': base + '/adm/news',
		'News.add': base + '/adm/news',
		'News.update': base + '/adm/news/{{id}}',
		'News.delete': base + '/adm/news/{{id}}',
		'News.picture': base + '/news/pictures/{{id}}.{{extension}}',
		'News.addPicture': base + '/adm/news/addpicture/{{id}}',
		'News.deletePicture': base + '/adm/news/deletepicture/{{id}}',

		'Admins.list': base + '/adm/admins',
		'Admins.add': base + '/adm/admins',
		'Admins.update': base + '/adm/admins/{{id}}',
		'Admins.delete': base + '/adm/admins/{{id}}',

		'Costscales.list': base + '/adm/costscales',
		'Costscales.add': base + '/adm/costscales',
		'Costscales.update': base + '/adm/costscales/{{id}}',
		'Costscales.delete': base + '/adm/costscales/{{id}}',

		'Currencies.list': base + '/adm/currencies',
		'Currencies.add': base + '/adm/currencies',
		'Currencies.update': base + '/adm/currencies/{{id}}',
		'Currencies.delete': base + '/adm/currencies/{{id}}',

		'Settings.list': base + '/adm/settings',
		'Settings.add': base + '/adm/settings',
		'Settings.update': base + '/adm/settings/{{id}}',
		'Settings.delete': base + '/adm/settings/{{id}}',

		'Orderstatuses.list': base + '/adm/orderstatuses',
		'Orderstatuses.add': base + '/adm/orderstatuses',
		'Orderstatuses.update': base + '/adm/orderstatuses/{{id}}',
		'Orderstatuses.delete': base + '/adm/orderstatuses/{{id}}',

		'Customers.list': base + '/adm/customers',
		'Rawgoods.list': base + '/adm/rawgoods',
		'Rawgoods.view': base + '/adm/rawgoods/{{id}}',
		'Rawgoods.linkTo': base + '/adm/rawgoods/{{id}}/linkto/{{goods}}',

		'Orderstatusmessages.list': base + '/adm/orderstatusmessages',
		'Orderstatusmessages.add': base + '/adm/orderstatusmessages',
		'Orderstatusmessages.update': base + '/adm/orderstatusmessages/{{id}}',
		'Orderstatusmessages.delete': base + '/adm/orderstatusmessages/{{id}}',

		'Suppliers.list': base + '/adm/suppliers',
		'Suppliers.add': base + '/adm/suppliers',
		'Suppliers.update': base + '/adm/suppliers/{{id}}',
		'Suppliers.delete': base + '/adm/suppliers/{{id}}',
		'SuppliersCurrency.delete': base +
			'/adm/suppliers/{{id}}/currency/{{currencyId}}',

		'Sales.delete': base + '/adm/sales/{{id}}',
		'Sales.update': base + '/adm/sales/{{id}}',
		'Sales.add': base + '/adm/sales',
		'Sales.list': base + '/adm/sales',
		'Sales.view': base + '/adm/sales/{{id}}',
		'Sales.notInSaleGoodsList': base + '/adm/sales/{{id}}/notinsalegoods',
		'Sales.inSaleGoodsList': base + '/adm/sales/{{id}}/goods',
		'Sales.addGoods': base + '/adm/sales/{{id}}/goods',
		'Sales.removeGoods': base + '/adm/sales/{{id}}/goods',

		'Slides.delete': base + '/adm/slides/{{id}}',
		'Slides.update': base + '/adm/slides/{{id}}',
		'Slides.add': base + '/adm/slides',
		'Slides.list': base + '/adm/slides',
		'Slides.view': base + '/adm/slides/{{id}}',

		'ExcelSettings.delete': base + '/adm/excelsetting/{{id}}',
		'ExcelSettings.update': base + '/adm/excelsetting/{{id}}',
		'ExcelSettings.add': base + '/adm/excelsetting',
		'ExcelSettings.list': base + '/adm/excelsetting',
		'ExcelSettings.view': base + '/adm/excelsetting/{{id}}',

		'Prices.add': base + '/adm/prices'

	});

})(window, angular, void 0);
