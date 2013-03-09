/*global define*/
define([
	'jquery',
	'marionette',
	'handlebars',
	'features/tvShowsTab/views/TrackedShowsCollectionView',
	'text!features/tvShowsTab/templates/tracked-shows-component.tpl',
	'features/tvShowsTab/collections/ShowsCollection',
	'features/tvShowsTab/views/ShowsComboBoxCollectionView',
	'HttpUtils',
	'chosen',
	'Spinner'
],
	function($, Marionette, Handlebars, TrackedShowsCollectionView, template, ShowsCollection, ShowsComboBoxCollectionView, HttpUtils, Chosen, Spinner) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'tracked-shows',

			ui: {
				trackedShowsList: '.tracked-shows-list'
			},

			events: {
				'click .add-tracked-show-button': '_onAddTrackedShow'
			},

			regions: {
				showsComboBoxRegion: '.all-shows-combo',
				trackedShowsListRegion: '.tracked-shows-list'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.vent = options.vent;
				this.allShowsCollection = new ShowsCollection();
				this.trackedShowsCollection = new ShowsCollection(options.trackedShows);
				var that = this;
				options.shows.forEach(function(show) {
					if (that.trackedShowsCollection.get(show.id) == null) {
						that.allShowsCollection.add(show);
					}
				});

				this.showsComboBoxCollectionView = new ShowsComboBoxCollectionView({
					collection: this.allShowsCollection,
					vent: this.vent
				});
				this.trackedShowsView = new TrackedShowsCollectionView({
					collection: this.trackedShowsCollection,
					vent: this.vent
				})
			},

			onRender: function() {
				this.showsComboBoxRegion.show(this.showsComboBoxCollectionView);
				this.trackedShowsListRegion.show(this.trackedShowsView);
				this.vent.on('tracked-show-remove', this._onRemoveTrackedShow, this);
				if (this.trackedShowsCollection.length > 0) {
					this.ui.trackedShowsList.addClass('tracked-shows-list-non-empty');
				}
			},

			addShow: function(show) {
				this.allShowsCollection.add(show);
				this.allShowsCollection.sort();
			},

			_onAddTrackedShow: function() {
				var show = this.showsComboBoxCollectionView.getSelectedShow();
				// nothing is selected
				if (show == undefined) {
					return;
				}

				var that = this;
				HttpUtils.post("rest/shows/addTracked/" + show.id, {}, function(res) {
					that.showsComboBoxCollectionView.clearSelection();
					that.allShowsCollection.remove(show);
					that.trackedShowsCollection.add(show);
					that.trackedShowsCollection.sort();
					that.ui.trackedShowsList.addClass('tracked-shows-list-non-empty');
				});
			},

			_onRemoveTrackedShow: function(show) {
				var that = this;
				Spinner.mask();
				setTimeout(function() {
					HttpUtils.post("rest/shows/removeTracked/" + show.id, {}, function(res) {
						that.trackedShowsCollection.remove(show);
						that.allShowsCollection.add(show);
						that.allShowsCollection.sort();
						if (that.trackedShowsCollection.length == 0) {
							that.ui.trackedShowsList.removeClass('tracked-shows-list-non-empty');
						}
						Spinner.unmask();
					}, false);
				}, 150);
			}
		});
	});
