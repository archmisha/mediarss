/*global define*/
define([
	'marionette',
	'backbone',
	'handlebars',
	'text!features/showsTab/templates/shows-search.tpl',
	'features/collections/UserTorrentCollection',
	'components/search-result/views/SearchResultsCollectionView',
	'utils/HttpUtils',
	'components/search-result/views/SearchResultsView',
	'utils/MessageBox',
	'features/showsTab/views/ActiveSearchCollectionView',
	'features/showsTab/collections/ActiveSearchCollection',
	'components/search-result/models/SearchResult'
],
	function(Marionette, Backbone, Handlebars, template, UserTorrentCollection, SearchResultsCollectionView, HttpUtils, SearchResultsView, MessageBox, ActiveSearchCollectionView, ActiveSearchCollection, SearchResult) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'search-shows',

			ui: {
				searchButton: '.shows-search-button',
				titleInput: '.shows-search-title',
				seasonInput: '.shows-search-season',
				episodeInput: '.shows-search-episode',
				adminForceDownload: '.shows-search-admin-force-download',
				adminForceDownloadCheckbox: '.shows-search-admin-force-download-checkbox',
				activeSearchesContainer: '.shows-search-active-searches'
			},

			events: {
				'click .shows-search-button': 'onSearchButtonClick',
				'keypress .shows-search-title': 'onKeyPress',
				'keypress .shows-search-season': 'onKeyPress',
				'keypress .shows-search-episode': 'onKeyPress',
				'keyup .shows-search-title': 'onShowTitleInputChange',
				'keyup .shows-search-season': 'onShowSeasonInputChange'
			},

			regions: {
				searchResultsRegion: '.shows-search-results',
				activeSearchesListRegion: '.shows-search-active-searches-list'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.vent = new Backbone.Wreqr.EventAggregator();
				this.vent.on('search-result-item-download', this.onEpisodeDownload, this);
				this.vent.on('did-you-mean-click', this.onDidYouMeanClick, this);
				this.vent.on('search-result-download-all', this.onDownloadAllClick, this);
				this.vent.on('active-search-remove', this.onActiveSearchRemove, this);
				this.vent.on('active-search-view', this.onActiveSearchView, this);

				this.searchResultsView = new SearchResultsView({vent: this.vent});
				this.activeSearchesCollection = new ActiveSearchCollection();
				this.activeSearchCollectionView = new ActiveSearchCollectionView({
					vent: this.vent,
					collection: this.activeSearchesCollection
				});

				var that = this;
				this.activeSearchesCollection.on('change reset add remove', function() {
					if (that.activeSearchesCollection.length === 0) {
						that.ui.activeSearchesContainer.hide();
					} else {
						that.ui.activeSearchesContainer.show();
					}
				});
			},

			onRender: function() {
				this.ui.seasonInput.prop('disabled', true);
				this.ui.episodeInput.prop('disabled', true);
				this.searchResultsRegion.show(this.searchResultsView);
				this.activeSearchesListRegion.show(this.activeSearchCollectionView);
				this.ui.activeSearchesContainer.hide();
				this._startPollingThread();
			},

			onClose: function() {
				this._stopPollingThread();
			},

			onDidYouMeanClick: function(showId, showName) {
				this.ui.titleInput.val(showName);
				this.onSearchButtonClick(null, showId);
			},

			onEpisodeDownload: function(userTorrent) {
				HttpUtils.post('rest/shows/episode/download', {
					torrentId: userTorrent.get('torrentId')
				}, function(res) {
					userTorrent.set('downloadStatus', 'SCHEDULED');
				});
			},

			setAdmin: function(isAdmin) {
				if (isAdmin) {
					this.ui.adminForceDownload.show();
				}
			},

			onKeyPress: function(event) {
				var ENTER_KEY = 13;
				if (event.which === ENTER_KEY) {
					this.onSearchButtonClick(null, null);
				}
			},

			// didYouMeanShowId might be undefined and its ok
			onSearchButtonClick: function(event, didYouMeanShowId) {
				var title = this.ui.titleInput.val();
				var season = this.ui.seasonInput.val();
				var episode = this.ui.episodeInput.val();
				var forceDownload = this.ui.adminForceDownloadCheckbox.attr('checked') ? true : false;

				if (!title || title.trim().length == 0) {
					return;
				}

				var that = this;
				HttpUtils.post('rest/shows/search', {
					title: title,
					season: season,
					episode: episode,
					showId: didYouMeanShowId,
					forceDownload: forceDownload
				}, function(searchResult) {
					if (searchResult.end == null) {
						that.searchResultsView.$el.slideUp('slow', function() {
							that.searchResultsView.$el.hide();
						});
						MessageBox.infoModal('The search is going to take a while',
							'You can track it in the active searches section.');
						that.activeSearchesCollection.add(searchResult);
						that._startPollingThread();
					} else {
						that.searchResultsView.$el.slideDown('slow');
						that.searchResultsView.setSearchResults(new SearchResult(searchResult));
					}
				});
			},

			onDownloadAllClick: function(userTorrents) {
				var torrentIds = [];
				userTorrents.forEach(function(userTorrent) {
					if (userTorrent.get('downloadStatus') === 'NONE') {
						torrentIds.push(userTorrent.get('torrentId'));
					}
				});

				HttpUtils.post('rest/shows/episode/download-all', {
					torrentIds: torrentIds
				}, function(res) {
					// set all in downloaded state in ui
					userTorrents.forEach(function(userTorrent) {
						if (userTorrent.get('downloadStatus') === 'NONE') {
							userTorrent.set('downloadStatus', 'SCHEDULED');
						}
					});
				});
			},

			onShowTitleInputChange: function() {
				var title = this.ui.titleInput.val();
				if (!title || title.trim().length == 0) {
					this.ui.seasonInput.prop('disabled', true);
					this.ui.episodeInput.prop('disabled', true);
				} else {
					this.ui.seasonInput.prop('disabled', false);
					this.onShowSeasonInputChange();
				}
			},

			onShowSeasonInputChange: function() {
				var season = this.ui.seasonInput.val();
				if (!season || season.trim().length == 0) {
					this.ui.episodeInput.prop('disabled', true);
				} else {
					this.ui.episodeInput.prop('disabled', false);
				}
			},

			_startPollingThread: function() {
				if (this.timer) {
					return;
				}

				var that = this;
				var f = function() {
					if (!that.timer) {
						return;
					}

					$.get("rest/shows/search/status")
						.success(function(res) {
							// if all jobs have stopped
							var allComplete = true;

							// not defined if session timeout
							if (res.activeSearches) {
								that.activeSearchesCollection.reset(res.activeSearches);
								res.activeSearches.forEach(function(el) {
									if (el.end == null) {
										allComplete = false;
									}
								});
							}
							if (allComplete) {
								that._stopPollingThread();
							}
						}).error(function(res) {
							console.log('error. data: ' + res);
							that._stopPollingThread();
						});

					that.timer = setTimeout(f, 1000);
				};
				that.timer = setTimeout(f, 1000);
				f();
			},

			_stopPollingThread: function() {
				clearTimeout(this.timer);
				this.timer = null;
			},

			onActiveSearchRemove: function(activeSearchModel) {
				var that = this;
				HttpUtils.get('rest/shows/search/remove/' + activeSearchModel.id,
					function(res) {
						that.activeSearchesCollection.remove(activeSearchModel);
					});
			},

			onActiveSearchView: function(activeSearchModel) {
				this.searchResultsView.setSearchResults(activeSearchModel);
			}
		});
	});
