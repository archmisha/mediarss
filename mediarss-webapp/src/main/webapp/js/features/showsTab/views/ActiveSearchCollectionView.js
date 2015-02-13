define([
	'marionette',
	'handlebars',
	'features/showsTab/views/ActiveSearchItemView'
],
	function(Marionette, Handlebars, ActiveSearchItemView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: ActiveSearchItemView,

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			}
		});
	});
