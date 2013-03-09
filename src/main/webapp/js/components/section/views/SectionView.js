/*global define*/
define([
	'marionette',
	'handlebars',
	'text!components/section/templates/section.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'section',

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.title = options.title;
				this.description = options.description;
				if (options.descriptionViewDef) {
					this.descriptionView = new options.descriptionViewDef(options);
				}
			},

			regions: {
				descriptionRegion: '.section-description'
			},

			onRender: function() {
				if (this.descriptionView) {
					this.descriptionRegion.show(this.descriptionView);
				}
			},

			templateHelpers: function() {
				return {
					'title': this.title,
					'description': this.description
				};
			}
		});
	});
