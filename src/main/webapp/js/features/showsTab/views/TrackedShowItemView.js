define([
	'marionette',
	'handlebars',
	'text!features/showsTab/templates/tracked-show.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'tracked-show',

			ui: {
				ended: '.tracked-show-ended'
			},

			events: {
				'click .tracked-show-image': 'onTrackedShowRemove'
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
				this.vent = options.vent;
			},

			onTrackedShowRemove: function() {
				this.vent.trigger('tracked-show-remove', this.model);
			},

			onRender: function() {
				if (this.model.get('ended')) {
					this.ui.ended.show();
				}
			}
		});
	});

