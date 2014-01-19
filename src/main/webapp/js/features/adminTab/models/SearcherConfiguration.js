define([
	'backbone'
], function(Backbone) {
	'use strict';

	return Backbone.Model.extend({

		defaults: {
			id: -1,
			name: '',
			domains: []
		}
	});
});
