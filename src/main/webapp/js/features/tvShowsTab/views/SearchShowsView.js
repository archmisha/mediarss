/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/tvShowsTab/templates/search-shows.tpl',
	'features/collections/UserTorrentCollection',
	'features/tvShowsTab/views/SearchResultsCollectionView',
	'HttpUtils'
],
	function(Marionette, Handlebars, template, UserTorrentCollection, SearchResultsCollectionView, HttpUtils) {
		"use strict";

		var didYouMeanShowId = undefined;
		var lastSearchResult = undefined;

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'search-shows',

			ui: {
				searchButton: '.shows-search-button',
				titleInput: '.shows-search-title',
				seasonInput: '.shows-search-season',
				episodeInput: '.shows-search-episode',
				searchResultsRegion: '.shows-search-results',
				noResultsStatus: '.shows-search-status-no-results',
				resultsCount: '.shows-search-results-header-count',
				resultsHeader: '.shows-search-results-header',
				titleContainer: '.shows-search-results-header-title-container',
				downloadAllButton: '.shows-search-results-download-all-button',
				multipleResultsTitle: '.shows-search-results-header-title-multiple',
				singleResultTitle: '.shows-search-results-header-title-single',
				showingResultsFor: '.shows-search-results-showing-results-for',
				showingResultsForText: '.shows-search-results-showing-results-for-text',
				didYouMean: '.shows-search-results-did-you-mean',
				didYouMeanList: '.shows-search-results-did-you-mean-list',
				adminForceDownload: '.shows-search-admin-force-download',
				adminForceDownloadCheckbox: '.shows-search-admin-force-download-checkbox'
			},

			events: {
				'click .shows-search-button': 'onSearchButtonClick',
				'keypress .shows-search-title': 'onKeyPress',
				'keypress .shows-search-season': 'onKeyPress',
				'keypress .shows-search-episode': 'onKeyPress',
				'click .shows-search-results-did-you-mean-list': 'onDidYouMeanClick',
				'click .shows-search-results-download-all-button': 'onDownloadAllClick'
			},

			regions: {
				searchResultsRegion: '.shows-search-results'
			},

			constructor: function(options) {
				this.loggedInUserData = options.loggedInUserData;
				Marionette.Layout.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
				if (this.loggedInUserData.user.admin) {
					this.ui.adminForceDownload.show();
				}
			},

			onKeyPress: function(event) {
				var ENTER_KEY = 13;
				if (event.which === ENTER_KEY) {
					this.onSearchButtonClick();
				}
			},

			showDidYouMean: function(searchResult) {
				var arr = [];
				searchResult.didYouMean.forEach(function(show) {
					arr.push('<span class=\'shows-search-results-did-you-mean-item\' showid=\'' + show.id + '\'>' + show.name + '</span>');
				});
				this.ui.didYouMeanList.html(arr.join(', '));
				this.ui.didYouMean.show();
			},

			onSearchButtonClick: function() {
				var title = this.ui.titleInput.val();
				var season = this.ui.seasonInput.val();
				var episode = this.ui.episodeInput.val();
				var forceDownload = this.ui.adminForceDownloadCheckbox.attr('checked') ? true : false;

				if (!title || title.trim().length == 0) {
					return;
				}

				var that = this;

				this.ui.noResultsStatus.fadeOut('slow');
				this.ui.resultsHeader.hide();
				this.ui.didYouMean.hide();
				this.ui.titleContainer.hide();
				this.ui.showingResultsFor.hide();
				if (this.ui.searchResultsRegion.is(":visible")) {
					this.ui.searchResultsRegion.slideUp('slow', function() {
						that.ui.searchResultsRegion.hide();
					});
				}

				// in case failed want to reset showId param also
				var showId = didYouMeanShowId;
				didYouMeanShowId = undefined;
				lastSearchResult = undefined;
				HttpUtils.post("rest/shows/search", {
					title: title,
					season: season,
					episode: episode,
					showId: showId,
					forceDownload: forceDownload
				}, function(searchResult) {
					if (searchResult.episodes.length > 0) {
						that.ui.resultsHeader.show();
						that.onSearchResultsReceived(searchResult);
					} else {
						if (searchResult.didYouMean !== undefined && searchResult.didYouMean.length > 0) {
							that.ui.resultsHeader.show();
							that.showDidYouMean(searchResult);
							that.setVisibleShowingResultsFor(searchResult.actualSearchTerm, searchResult.actualSearchTerm !== searchResult.originalSearchTerm);
						}
						that.ui.noResultsStatus.fadeIn('slow');
					}
				});
			},

			setVisibleShowingResultsFor: function(text, visible) {
				this.ui.showingResultsForText.html(text);
				if (visible) {
					this.ui.showingResultsFor.show();
					this.ui.resultsHeader.addClass('shows-search-results-header-with-showing-results-for');
				} else {
					this.ui.showingResultsFor.hide();
					this.ui.resultsHeader.removeClass('shows-search-results-header-with-showing-results-for');
				}
			},

			onSearchResultsReceived: function(searchResult) {
				lastSearchResult = searchResult;
				var that = this;

				if (searchResult.didYouMean !== undefined && searchResult.didYouMean.length > 0) {
					this.setVisibleShowingResultsFor(searchResult.actualSearchTerm, true);
					this.showDidYouMean(searchResult);
				} else {
					this.setVisibleShowingResultsFor(searchResult.actualSearchTerm, searchResult.actualSearchTerm !== searchResult.originalSearchTerm);
				}

				this.ui.resultsCount.html(searchResult.episodes.length);
				this.ui.titleContainer.show();
				this.searchResultsCollection = new UserTorrentCollection(searchResult.episodes);
				var searchResultsCollectionView = new SearchResultsCollectionView({collection: this.searchResultsCollection});
				searchResultsCollectionView.on('render', function() {
					// if there is scroll bar - move download all button more to the left
					//if (that.searchResultsRegion.$el.get(0).scrollHeight > that.searchResultsRegion.$el.height()) {
					if (searchResult.episodes.length > 8) {
						that.ui.downloadAllButton.addClass('shows-search-results-download-all-button-with-scroll');
					} else {
						that.ui.downloadAllButton.removeClass('shows-search-results-download-all-button-with-scroll');
					}
				});
				this.searchResultsRegion.show(searchResultsCollectionView);
				this.ui.searchResultsRegion.slideDown('slow');

				if (searchResult.episodes.length == 1) {
					this.ui.downloadAllButton.hide();
					this.ui.multipleResultsTitle.hide();
					this.ui.singleResultTitle.show();
				} else {
					var isAllDownloaded = true;
					searchResult.episodes.forEach(function(userTorrent) {
						isAllDownloaded = isAllDownloaded && userTorrent.downloaded;
					});
					if (isAllDownloaded) {
						this.ui.downloadAllButton.hide();
					} else {
						this.ui.downloadAllButton.show();
					}

					this.ui.multipleResultsTitle.show();
					this.ui.singleResultTitle.hide();
				}
			},

			onDidYouMeanClick: function(event) {
				didYouMeanShowId = $(event.target).attr('showid');
				var showName = $(event.target).html();
				this.ui.titleInput.val(showName);
				this.onSearchButtonClick();
			},

			onDownloadAllClick: function() {
				var torrentIds = [];
				lastSearchResult.episodes.forEach(function(userTorrent) {
					if (!userTorrent.downloaded) {
						torrentIds.push(userTorrent.torrentId);
					}
				});

				var that = this;
				HttpUtils.post("rest/shows/episode/downloadAll", {
					torrentIds: torrentIds
				}, function(res) {
					// set all in downloaded state in ui
					that.searchResultsCollection.forEach(function(userTorrent) {
						if (!userTorrent.downloaded) {
							userTorrent.set('downloaded', true);
						}
					});

					that.ui.downloadAllButton.hide();
				});
			}
		});
	});
