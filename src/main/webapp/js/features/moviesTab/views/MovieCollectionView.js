define([
	'marionette',
	'handlebars',
	'features/moviesTab/views/MovieItemView',
	'features/moviesTab/views/MovieCollectionLoadingView'
],
	function(Marionette, Handlebars, MovieItemView, MovieCollectionLoadingView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: MovieItemView,
			className: 'movies-list',
			emptyView: MovieCollectionLoadingView,

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent, isAdmin: options.isAdmin };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);

				this.collection.bind("change reset add remove", this._updateLoadingLabel, this);
			},

			_updateLoadingLabel: function() {
				this.$el.find('.movies-list-empty-label-container').html('No movies');
			}
		});
	});

