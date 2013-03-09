define([
	'marionette',
	'handlebars',
	'text!features/tvShowsTab/templates/tracked-show.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'tracked-show',

			events: {
				'click .tracked-show-image': 'onTrackedShowRemove'
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
				this.vent = options.vent;
			},

			onTrackedShowRemove: function() {
				this.vent.trigger('tracked-show-remove', this.model);
			}
		});
	});

