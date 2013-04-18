/*global define*/
define([
	'jquery',
	'marionette',
	'handlebars',
	'features/tvShowsTab/views/TrackedShowsCollectionView',
	'text!features/tvShowsTab/templates/tracked-shows-component.tpl',
	'features/tvShowsTab/collections/ShowsCollection',
	'HttpUtils',
	'select2',
	'Spinner',
	'utils/Utils',
	'features/tvShowsTab/models/Show',
	'MessageBox'
],
	function($, Marionette, Handlebars, TrackedShowsCollectionView, template, ShowsCollection, HttpUtils, select2, Spinner, Utils, Show, MessageBox) {
		"use strict";

		var SHOWS_COMBO_BOX_SELECTOR = '.all-shows-combo';

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'tracked-shows',

			ui: {
				trackedShowsList: '.tracked-shows-list',
				showsComboBox: SHOWS_COMBO_BOX_SELECTOR
			},

			events: {
				'click .add-tracked-show-button': '_onAddTrackedShow'
			},

			regions: {
				trackedShowsListRegion: '.tracked-shows-list'
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.trackedShowsCollection = new ShowsCollection(options.trackedShows);

				this.trackedShowsView = new TrackedShowsCollectionView({
					collection: this.trackedShowsCollection,
					vent: this.vent
				})

				this.vent.on('tracked-show-remove', this._onRemoveTrackedShow, this);
			},

			onRender: function() {
				this.trackedShowsListRegion.show(this.trackedShowsView);
				if (this.trackedShowsCollection.length > 0) {
					this.ui.trackedShowsList.addClass('tracked-shows-list-non-empty');
				}
			},

			_onAddTrackedShow: function() {
				var comboShow = this.ui.showsComboBox.select2('data');
				var showId = comboShow.id;
				// nothing is selected
				if (showId == undefined) {
					return;
				}

				var that = this;
				Spinner.mask();
				setTimeout(function() {
					HttpUtils.post("rest/shows/addTracked/" + showId, {}, function(res) {
						Spinner.unmask();
						that.trackedShowsCollection.add(new Show({id: comboShow.id, name: comboShow.text}));
						that.trackedShowsCollection.sort();
						that.ui.trackedShowsList.addClass('tracked-shows-list-non-empty');
						that.ui.showsComboBox.select2('data', '');
						MessageBox.info('Show \'' + comboShow.text + '\' is being tracked');
						that.vent.trigger('shows-schedule-update', res.schedule);
					}, false);
				}, 150);
			},

			_onRemoveTrackedShow: function(show) {
				var that = this;
				Spinner.mask();
				setTimeout(function() {
					HttpUtils.post("rest/shows/removeTracked/" + show.id, {}, function(res) {
						Spinner.unmask();
						var showModel = that.trackedShowsCollection.get(show.id);
						that.trackedShowsCollection.remove(show);
						if (that.trackedShowsCollection.length == 0) {
							that.ui.trackedShowsList.removeClass('tracked-shows-list-non-empty');
						}
						MessageBox.info('Show \'' + showModel.get('name') + '\' is no more tracked');
						that.vent.trigger('shows-schedule-update', res.schedule);
					}, false);
				}, 150);
			},

			onShow: function() {
				Utils.waitForDisplayAndCreate(SHOWS_COMBO_BOX_SELECTOR, this.createChosen);
			},

			createChosen: function(selector) {
				$(selector).select2({
					placeholder: "Select a Show",
					minimumInputLength: 3,
					ajax: {
						url: 'rest/shows/tracked/autocomplete',
						dataType: 'jsonp',
						data: function(term, page) {
							return {
								term: term
							};
						},
						results: function(data, page) {
							return {results: data.shows};
						}
					},
					formatResult: function(show) {
						return show.text;
					},
					formatSelection: function(show) {
						return show.text;
					}
				});
			}
		});
	});
