define([
	'marionette',
	'handlebars',
	'components/search-result/views/SearchResultItemView',
	'components/search-result/views/SearchResultsCollectionEmptyView'
],
	function(Marionette, Handlebars, SearchResultItemView, SearchResultsCollectionEmptyView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: SearchResultItemView,
			className: 'search-results',
			emptyView: SearchResultsCollectionEmptyView,

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };
				this.emptyMessage = options.emptyMessage || 'There are no results';

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
				this.setEmptyMessage(this.emptyMessage);
			},

			setEmptyMessage: function(msg) {
				this.emptyMessage = msg;
				this.$el.find('.search-results-empty-view').html(this.emptyMessage);
			}
		});
	});