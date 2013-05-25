define([
	'marionette',
	'handlebars',
	'text!features/showsTab/templates/shows-schedule.tpl',
	'features/showsTab/models/ShowsSchedule',
],
	function(Marionette, Handlebars, template, ShowsSchedule) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'shows-schedule-list',

			ui: {
				loadingComponent: '.shows-schedule-loading'
			},

			constructor: function(options) {
				this.schedule = options.schedule;
				this.vent = options.vent;
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.model = new ShowsSchedule();
				this.vent.on('shows-schedule-update', this.setSchedule, this);
			},

			setSchedule: function(schedule) {
				this.model.clear().set({schedule: schedule});
				this.render();
				// must call after the render call
				this.ui.loadingComponent.hide();
			}
		});
	});
