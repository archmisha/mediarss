define([
	'backbone'
], function(Backbone) {
	'use strict';

	return Backbone.Model.extend({

		defaults: {
			id: -1,
			language: null,
			type: null,
			name: null
		}
	});
});
