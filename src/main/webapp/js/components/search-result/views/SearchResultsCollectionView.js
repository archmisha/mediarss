define([
	'marionette',
	'handlebars',
	'components/search-result/views/SearchResultItemView'
],
	function(Marionette, Handlebars, SearchResultItemView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: SearchResultItemView,
			className: 'search-results',

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			}
		});
	});

