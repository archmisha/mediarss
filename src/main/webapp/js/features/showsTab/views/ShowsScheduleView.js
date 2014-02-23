define([
	'marionette',
	'handlebars',
	'text!features/showsTab/templates/shows-schedule.tpl',
	'features/showsTab/models/ShowsSchedule',
	'utils/HttpUtils'
],
	function(Marionette, Handlebars, template, ShowsSchedule, HttpUtils) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'shows-schedule-list',

			constructor: function(options) {
				this.schedule = options.schedule;
				this.vent = options.vent;
				Marionette.Layout.prototype.constructor.apply(this, arguments);

				this.model = new ShowsSchedule();
				this.vent.on('tracked-shows-change', this._onScheduleUpdate, this);
			},

			_onScheduleUpdate: function() {
				var that = this;
				HttpUtils.get("rest/shows/schedule", function(res) {
					that.setSchedule(res.schedule);
				}, false); // no need loading here
			},

			setSchedule: function(schedule) {
				this.model.clear().set({schedule: schedule});
				this.render();
			},

			onRender: function() {
			}
		});
	});
