define([
	'backbone'
], function(Backbone){
	'use strict';

	return Backbone.Model.extend({

		defaults : {
			downloaded: false,
			torrentId: null,
			title: ''
		}
	});
});
