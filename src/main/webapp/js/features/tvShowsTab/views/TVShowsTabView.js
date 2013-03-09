/*global define*/
define([
	'marionette',
	'handlebars',
	'features/tvShowsTab/views/TrackedShowsComponentView',
	'text!features/tvShowsTab/templates/tvshows-tab.tpl',
	'features/tvShowsTab/collections/ShowsCollection',
	'components/section/views/SectionView',
	'features/tvShowsTab/views/SearchShowsView',
	'features/tvShowsTab/views/TrackedShowsDescriptionView',
	'MessageBox'
],
	function(Marionette, Handlebars, TrackedShowsComponentView, template, ShowsCollection, SectionView,
		SearchShowsView, TrackedShowsDescriptionView, MessageBox) {
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
				trackedShowsRegion: '.tracked-shows-container'
			},

			constructor: function(options) {
				this.vent = new Marionette.EventAggregator();
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.loggedInUserData = options.loggedInUserData;
				this.initialData = options.initialData;

				this.searchShowsSection = new SectionView({
					title: 'Search TV Shows',
					description: 'Search for older episodes'
				});
				this.searchShowsView = new SearchShowsView({
				});

				this.trackedShowsView = new TrackedShowsComponentView({
					shows: this.initialData.shows,
					trackedShows: this.loggedInUserData.shows,
					vent: this.vent
				});
				this.trackedShowsSection = new SectionView({
					title: 'Tracked TV Shows',
//					description: 'Ended shows are not shown as there is no point tracking them'
					descriptionViewDef: TrackedShowsDescriptionView,
					vent: this.vent
				});
			},

			onRender: function() {
				this.vent.on('add-manual-show', this.onAddManualShow, this);

				this.searchShowsSectionRegion.show(this.searchShowsSection);
				this.searchShowsRegion.show(this.searchShowsView);
				this.trackedShowsSectionRegion.show(this.trackedShowsSection);
				this.trackedShowsRegion.show(this.trackedShowsView);
			},

			onAddManualShow: function(params) {
				this.trackedShowsView.addShow(params.show);
				MessageBox.info('Added show \'' + params.show.name + '\'');
			}
		});
	});
