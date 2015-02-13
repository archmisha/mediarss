define([
	'backbone',
	'features/adminTab/models/User'

],
	function(Backbone, User) {
		'use strict';

		return Backbone.Collection.extend({
			model: User,

			url: 'rest/admin/users',

			parse: function(data) {
				var result = [];

				if (data.success == undefined) {
					data.forEach(function(item) {
						result.push(new User(item));
					});
				}

				return result;
			}
		});
	});

