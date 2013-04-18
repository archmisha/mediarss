define([
	'marionette',
	'handlebars',
	'text!features/tvShowsTab/templates/shows-schedule.tpl',
	'features/tvShowsTab/models/ShowsSchedule',
],
	function(Marionette, Handlebars, template, ShowsSchedule) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'shows-schedule-list',

			constructor: function(options) {
				this.schedule = options.schedule;
				this.vent = options.vent;
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.model = new ShowsSchedule({schedule: this.schedule});
				this.vent.on('shows-schedule-update', this._updateShowsSchedule, this);
			},

			onRender: function() {
			},

			_updateShowsSchedule: function(schedule) {
				this.model.set('schedule', schedule);
				this.render();
			}
		});
	});
