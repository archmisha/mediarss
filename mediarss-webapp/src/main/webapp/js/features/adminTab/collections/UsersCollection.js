define([
	'backbone',
	'features/adminTab/models/User'

],
	function(Backbone, User) {
		'use strict';

		return Backbone.Collection.extend({
			model: User,

			url: 'rest/user/users',

			parse: function(data) {
				var result = [];

				if (data.success == undefined) {
					data.users.forEach(function (item) {
						result.push(new User(item));
					});
				}

				return result;
			}
		});
	});

