define([
	'backbone'
], function(Backbone) {
	'use strict';

	return Backbone.Model.extend({

		defaults: {
			id: -1,
			loggedIn: false,
			email: '',
			firstName: '',
			lastName: '',
			lastLogin: null,
			lastShowsFeedGenerated: null,
			lastMoviesFeedGenerated: null
		}
	});
});
