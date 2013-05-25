define([
	'backbone',
	'features/showsTab/models/Show'
],
	function(Backbone, Show) {
		'use strict';

		return Backbone.Collection.extend({
			model: Show,

			comparator: function(item) {
				return item.get('name');
			}
		});
	});

