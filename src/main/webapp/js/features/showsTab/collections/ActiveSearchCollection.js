define([
	'backbone',
	'components/search-result/models/SearchResult'
],
	function(Backbone, SearchResult) {
		'use strict';

		return Backbone.Collection.extend({
			model: SearchResult
		});
	});

