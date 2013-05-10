define([
	'marionette',
	'handlebars',
	'features/tvShowsTab/views/TrackedShowItemView',
	'features/tvShowsTab/views/TrackedShowsCollectionLoadingView'
],
	function(Marionette, Handlebars, TrackedShowItemView, TrackedShowsCollectionLoadingView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: TrackedShowItemView,
			emptyView: TrackedShowsCollectionLoadingView,

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			}
		});
	});
