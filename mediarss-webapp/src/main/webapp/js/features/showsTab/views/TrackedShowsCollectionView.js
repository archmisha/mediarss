define([
	'marionette',
	'handlebars',
	'features/showsTab/views/TrackedShowItemView',
	'features/showsTab/views/TrackedShowsCollectionLoadingView'
],
	function(Marionette, Handlebars, TrackedShowItemView, TrackedShowsCollectionLoadingView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: TrackedShowItemView,
			emptyView: TrackedShowsCollectionLoadingView,

			constructor: function(options) {
				this.vent = options.vent;
				this.itemViewOptions = { vent: options.vent };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);

				this.collection.bind("change reset add remove", this._updateLoadingLabel, this);
			},

			_updateLoadingLabel: function() {
				// after any change we know it is already loaded
				this.$el.find('.tracked-shows-empty-label-container').html('No tracked shows');
			}
		});
	});
