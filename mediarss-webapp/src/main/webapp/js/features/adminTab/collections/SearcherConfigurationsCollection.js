define([
	'backbone',
	'features/adminTab/models/SearcherConfiguration'

],
	function(Backbone, SearcherConfiguration) {
		'use strict';

		return Backbone.Collection.extend({
			model: SearcherConfiguration,

			url: 'rest/torrents/searcher-configurations',

			parse: function(data) {
				var result = [];

				if (data.success == undefined) {
					data.forEach(function(item) {
						result.push(new SearcherConfiguration(item));
					});
				}

				return result;
			}
		});
	});

