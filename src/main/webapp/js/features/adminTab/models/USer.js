define([
	'backbone'
], function(Backbone){
	'use strict';

	return Backbone.Model.extend({

		defaults : {
			email: '',
			firstName: '',
			lastName: '',
			lastLogin: null,
			lastShowsFeedGenerated: null,
			lastMoviesFeedGenerated: null
		}
	});
});
