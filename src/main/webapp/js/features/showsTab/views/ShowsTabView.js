/*global define*/
define([
	'jquery',
	'marionette',
	'handlebars',
	'features/showsTab/views/TrackedShowsComponentView',
	'text!features/showsTab/templates/tvshows-tab.tpl',
	'features/showsTab/collections/ShowsCollection',
	'components/section/views/SectionView',
	'features/showsTab/views/ShowsSearchView',
	'MessageBox',
	'features/showsTab/views/ShowsScheduleView',
	'HttpUtils'
],
	function($, Marionette, Handlebars, TrackedShowsComponentView, template, ShowsCollection, SectionView, ShowsSearchView, MessageBox, ShowsScheduleView, HttpUtils) {
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
				this.showsSearchView = new ShowsSearchView();

				this.trackedShowsView = new TrackedShowsComponentView({vent: this.vent});
				this.trackedShowsSection = new SectionView({
					title: 'Tracked TV Shows <span class=\'tracked-shows-counter\'></span>',
					description: 'Ended shows are not shown as there is no point tracking them',
					vent: this.vent
				});

				this.showsScheduleSection = new SectionView({
					title: 'Schedule',
					description: 'View past and future episode air dates<br/>of your tracked shows'
				});
				this.showsScheduleView = new ShowsScheduleView({vent: this.vent});

				this.vent.on('shows-schedule-update', this._onScheduleUpdate, this);
			},

			_onScheduleUpdate: function() {
				$('.tracked-shows-counter').html('(' + this.trackedShowsView.getTrackedShowsCount() + ')');
			},

			onRender: function() {
				this.searchShowsSectionRegion.show(this.searchShowsSection);
				this.searchShowsRegion.show(this.showsSearchView);
				this.trackedShowsSectionRegion.show(this.trackedShowsSection);
				this.trackedShowsRegion.show(this.trackedShowsView);
				this.showsScheduleSectionRegion.show(this.showsScheduleSection);
				this.showsScheduleRegion.show(this.showsScheduleView);

				var that = this;
				HttpUtils.get("rest/shows/tracked-shows", function(res) {
					that.trackedShowsView.setTrackedShows(res.trackedShows);
					that.showsSearchView.setAdmin(res.isAdmin);
					$('.tracked-shows-counter').html('(' + res.trackedShows.length + ')');
				}, false); // no need loading here
				HttpUtils.get("rest/shows/schedule", function(res) {
					that.showsScheduleView.setSchedule(res.schedule);
					that.showsSearchView.setAdmin(res.isAdmin);
				}, false); // no need loading here
			}
		});
	});
