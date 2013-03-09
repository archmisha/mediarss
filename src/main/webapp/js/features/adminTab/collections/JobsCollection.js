define([
	'backbone',
	'features/adminTab/models/Job'

],
	function(Backbone, Job) {
		'use strict';

		return Backbone.Collection.extend({
			model: Job,

			url: 'rest/jobs',

			parse: function(data) {
				var result = [];

				if (data.success == undefined) {
					data.forEach(function(item) {
						result.push(new Job(item));
					});
				}

				return result;
			}
		});
	});

