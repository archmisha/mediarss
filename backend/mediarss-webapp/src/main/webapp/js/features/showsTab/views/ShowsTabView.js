/*global define*/
define([
	'jquery',
	'backbone',
	'marionette',
	'handlebars',
	'features/showsTab/views/TrackedShowsComponentView',
	'text!features/showsTab/templates/tvshows-tab.tpl',
	'features/showsTab/collections/ShowsCollection',
	'components/section/views/SectionView',
	'features/showsTab/views/ShowsSearchView',
	'utils/MessageBox',
	'features/showsTab/views/ShowsScheduleView',
	'utils/HttpUtils'
],
	function($, Backbone, Marionette, Handlebars, TrackedShowsComponentView, template, ShowsCollection, SectionView, ShowsSearchView, MessageBox, ShowsScheduleView, HttpUtils) {
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
				showsScheduleSectionRegion: '.shows-schedule-section'
			},

			constructor: function(options) {
				var that = this;
				this.isDataLoaded = false;
				this.vent = new Backbone.Wreqr.EventAggregator();
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.tabData = options.tabData;

				this.searchShowsSection = new SectionView({
					title: 'Search TV Shows',
					description: 'Search for older episodes'
				});

				this.showsSearchView = new ShowsSearchView({isAdmin: this.tabData.isAdmin});

				this.trackedShowsView = new TrackedShowsComponentView({vent: this.vent});
				this.trackedShowsSection = new SectionView({
					title: 'Tracked TV Shows',
					description: 'Ended shows are not shown as there is no point tracking them',
					vent: this.vent,
					getCounter: function() {
						return that.trackedShowsView.getTrackedShowsCount();
					}
				});

				this.showsScheduleView = new ShowsScheduleView({vent: this.vent});
				this.showsScheduleSection = new SectionView({
					title: 'Schedule',
					description: 'View past and future episode air dates of your tracked shows',
					content: this.showsScheduleView,
					collapsed: true,
					collapsible: true
				});

				this.vent.on('tracked-shows-change', function() {
					that.trackedShowsSection.updateCounter();
				}, this);

				$(window).resize(function(event) {
					that._onScreenResolutionChange(event);
				});
			},

			onRender: function() {
				this.searchShowsSectionRegion.show(this.searchShowsSection);
				this.searchShowsRegion.show(this.showsSearchView);
				this.trackedShowsSectionRegion.show(this.trackedShowsSection);
				this.trackedShowsRegion.show(this.trackedShowsView);
				this.showsScheduleSectionRegion.show(this.showsScheduleSection);

				var that = this;
				if (!this.isDataLoaded) {
					this.isDataLoaded = true;
					HttpUtils.get("rest/shows/tracked-shows", function(res) {
						that.trackedShowsView.setTrackedShows(res.trackedShows);
						that.trackedShowsSection.updateCounter();
					}, false); // no need loading here
					HttpUtils.get("rest/shows/schedule", function(res) {
						that.showsScheduleView.setSchedule(res);
					}, false); // no need loading here
				}

				// for the first time, if desktop - need to expand if collapsed
				this._onScreenResolutionChange(null);
			},

			onClose: function() {
				$(window).off("resize");
			},

			_onScreenResolutionChange: function(event) {
				var scheduleSectionCollapsible = $(window).width() <= 480;
//				console.log('window width: ' + $(window).width());
				this.showsScheduleSection.setCollapsible(scheduleSectionCollapsible);
			}
		});
	});
