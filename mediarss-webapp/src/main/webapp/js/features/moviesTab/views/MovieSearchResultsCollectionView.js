define([
	'marionette',
	'handlebars',
	'features/moviesTab/views/MovieSearchResultItemView',
	'components/search-result/views/SearchResultsCollectionEmptyView'
],
	function(Marionette, Handlebars, MovieSearchResultItemView, SearchResultsCollectionEmptyView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: MovieSearchResultItemView,
			className: 'movie-search-results-list',
			emptyView: SearchResultsCollectionEmptyView,

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };
				this.emptyMessage = options.emptyMessage || 'There are no results';

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
				this.$el.find('.search-results-empty-view').html(this.emptyMessage);
			}
		});
	});