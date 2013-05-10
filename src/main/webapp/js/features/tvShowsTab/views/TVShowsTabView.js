/*global define*/
define([
	'marionette',
	'handlebars',
	'features/tvShowsTab/views/TrackedShowsComponentView',
	'text!features/tvShowsTab/templates/tvshows-tab.tpl',
	'features/tvShowsTab/collections/ShowsCollection',
	'components/section/views/SectionView',
	'features/tvShowsTab/views/SearchShowsView',
	'MessageBox',
	'features/tvShowsTab/views/ShowsScheduleView',
	'HttpUtils'
],
	function(Marionette, Handlebars, TrackedShowsComponentView, template, ShowsCollection, SectionView, SearchShowsView, MessageBox, ShowsScheduleView, HttpUtils) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'tvshows-tab',

			ui: {
				showsCombo: '.all-shows-combo'
			},

			regions: {
				searchShowsSectionRegion: '.search-shows-section',
				searchShowsRegion: '.search-shows-container',
				trackedShowsSectionRegion: '.tracked-shows-section',
				trackedShowsRegion: '.tracked-shows-container',
				showsScheduleSectionRegion: '.shows-schedule-section',
				showsScheduleRegion: '.shows-schedule-list-container'
			},

			constructor: function(options) {
				this.vent = new Marionette.EventAggregator();
				Marionette.Layout.prototype.constructor.apply(this, arguments);

				this.searchShowsSection = new SectionView({
					title: 'Search TV Shows',
					description: 'Search for older episodes'
				});
				this.searchShowsView = new SearchShowsView();

				this.trackedShowsView = new TrackedShowsComponentView({vent: this.vent});
				this.trackedShowsSection = new SectionView({
					title: 'Tracked TV Shows',
					description: 'Ended shows are not shown as there is no point tracking them',
					vent: this.vent
				});

				this.showsScheduleSection = new SectionView({
					title: 'Schedule',
					description: 'View past and future episode air dates<br/>of your tracked shows'
				});
				this.showsScheduleView = new ShowsScheduleView({vent: this.vent});
			},

			onRender: function() {
				this.searchShowsSectionRegion.show(this.searchShowsSection);
				this.searchShowsRegion.show(this.searchShowsView);
				this.trackedShowsSectionRegion.show(this.trackedShowsSection);
				this.trackedShowsRegion.show(this.trackedShowsView);
				this.showsScheduleSectionRegion.show(this.showsScheduleSection);
				this.showsScheduleRegion.show(this.showsScheduleView);

				var that = this;
				HttpUtils.get("rest/shows/initial-data", function(res) {
					that.trackedShowsView.setTrackedShows(res.trackedShows);
					that.showsScheduleView.setSchedule(res.schedule);
					that.searchShowsView.setAdmin(res.isAdmin);
				}, false); // no need loading here
			}
		});
	});
