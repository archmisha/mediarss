/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/showsTab/templates/shows-search.tpl',
	'features/collections/UserTorrentCollection',
	'components/search-result/views/SearchResultsCollectionView',
	'HttpUtils',
	'components/search-result/views/SearchResultsView'
],
	function(Marionette, Handlebars, template, UserTorrentCollection, SearchResultsCollectionView, HttpUtils, SearchResultsView) {
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
				adminForceDownloadCheckbox: '.shows-search-admin-force-download-checkbox'
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
				searchResultsRegion: '.shows-search-results'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.vent = new Marionette.EventAggregator();
				this.vent.on('search-result-item-download', this.onEpisodeDownload, this);
				this.vent.on('did-you-mean-click', this.onDidYouMeanClick, this);
				this.vent.on('search-result-download-all', this.onDownloadAllClick, this);

				this.searchResultsView = new SearchResultsView({vent: this.vent});
			},

			onRender: function() {
				this.ui.seasonInput.prop('disabled', true);
				this.ui.episodeInput.prop('disabled', true);
				this.searchResultsRegion.show(this.searchResultsView);
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
					that.searchResultsView.setSearchResults(searchResult);
				});
			},

			onDownloadAllClick: function(userTorrents) {
				var torrentIds = [];
				userTorrents.forEach(function(userTorrent) {
					if (userTorrent.get('downloadStatus') === 'NONE') {
						torrentIds.push(userTorrent.get('torrentId'));
					}
				});

				var that = this;
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
			}
		});
	});
