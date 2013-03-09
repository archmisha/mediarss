define([
	'marionette',
	'handlebars',
	'features/tvShowsTab/views/TrackedShowItemView'
],
	function(Marionette, Handlebars, TrackedShowItemView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: TrackedShowItemView,

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			}
		});
	});
