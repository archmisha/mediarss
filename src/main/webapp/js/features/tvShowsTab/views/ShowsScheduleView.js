define([
	'marionette',
	'handlebars',
	'text!features/tvShowsTab/templates/shows-schedule.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'shows-schedule-list',

			constructor: function(options) {
				this.schedule = options.schedule;
				Marionette.Layout.prototype.constructor.apply(this, arguments);
			},

			templateHelpers: function() {
				return {
					'schedule': this.schedule
				};
			}
		});
	});
