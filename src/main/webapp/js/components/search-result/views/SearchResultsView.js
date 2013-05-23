/*global define*/
define([
	'marionette',
	'handlebars',
	'text!components/search-result/templates/search-results.tpl',
	'features/collections/UserTorrentCollection',
	'components/search-result/views/SearchResultsCollectionView'
],
	function(Marionette, Handlebars, template, UserTorrentCollection, SearchResultsCollectionView) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'search-results',

			ui: {
				searchResultsRegion: '.search-results-list',
				noResultsStatus: '.search-status-no-results',
				resultsCount: '.search-results-header-count',
				resultsHeader: '.search-results-header',
				titleContainer: '.search-results-header-title-container',
				downloadAllButton: '.search-results-download-all-button',
				multipleResultsTitle: '.search-results-header-title-multiple',
				singleResultTitle: '.search-results-header-title-single',
				showingResultsFor: '.search-results-showing-results-for',
				showingResultsForText: '.search-results-showing-results-for-text',
				didYouMean: '.search-results-did-you-mean',
				didYouMeanList: '.search-results-did-you-mean-list',
			},

			events: {
				'click .search-results-did-you-mean-list': 'onDidYouMeanClick',
				'click .search-results-download-all-button': 'onDownloadAllClick'
			},

			regions: {
				searchResultsRegion: '.search-results-list'
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.Layout.prototype.constructor.apply(this, arguments);
			},

			showDidYouMean: function(searchResult) {
				var arr = [];
				searchResult.didYouMean.forEach(function(show) {
					arr.push('<span class=\'search-results-did-you-mean-item\' showid=\'' + show.id + '\'>' + show.name + '</span>');
				});
				this.ui.didYouMeanList.html(arr.join(', '));
				this.ui.didYouMean.show();
			},

			setSearchResults: function(searchResult) {
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

				if (searchResult.episodes.length > 0) {
					that.ui.resultsHeader.show();
					that.onSearchResultsReceived(searchResult);
				} else {
					if (searchResult.didYouMean !== undefined && searchResult.didYouMean.length > 0) {
						that.ui.resultsHeader.show();
						that.showDidYouMean(searchResult);
						that.setVisibleShowingResultsFor(searchResult.actualSearchTerm,
							searchResult.actualSearchTerm != null && searchResult.actualSearchTerm !== searchResult.originalSearchTerm);
					}
					if (searchResult.actualSearchTerm != null) {
						that.ui.noResultsStatus.fadeIn('slow');
					}
				}
			},

			setVisibleShowingResultsFor: function(text, visible) {
				this.ui.showingResultsForText.html(text);
				if (visible) {
					this.ui.showingResultsFor.show();
					this.ui.resultsHeader.addClass('search-results-header-with-showing-results-for');
				} else {
					this.ui.showingResultsFor.hide();
					this.ui.resultsHeader.removeClass('search-results-header-with-showing-results-for');
				}
			},

			onSearchResultsReceived: function(searchResult) {
				var that = this;

				this.setVisibleShowingResultsFor(searchResult.actualSearchTerm, searchResult.actualSearchTerm !== searchResult.originalSearchTerm);
				if (searchResult.didYouMean !== undefined && searchResult.didYouMean.length > 0) {
					this.showDidYouMean(searchResult);
				}

				this.ui.resultsCount.html(searchResult.episodes.length);
				this.ui.titleContainer.show();
				this.searchResultsCollection = new UserTorrentCollection(searchResult.episodes);
				this.searchResultsCollection.bind('change:downloadStatus', this.setDownloadAllButtonState, this);
				var searchResultsCollectionView = new SearchResultsCollectionView({
					collection: this.searchResultsCollection,
					vent: this.vent
				});
				searchResultsCollectionView.on('render', function() {
					// if there is scroll bar - move download all button more to the left
					//if (that.searchResultsRegion.$el.get(0).scrollHeight > that.searchResultsRegion.$el.height()) {
					if (searchResult.episodes.length > 8) {
						that.ui.downloadAllButton.addClass('search-results-download-all-button-with-scroll');
					} else {
						that.ui.downloadAllButton.removeClass('search-results-download-all-button-with-scroll');
					}
				});
				this.searchResultsRegion.show(searchResultsCollectionView);
				this.ui.searchResultsRegion.slideDown('slow');

				if (searchResult.episodes.length == 1) {
					this.ui.downloadAllButton.hide();
					this.ui.multipleResultsTitle.hide();
					this.ui.singleResultTitle.show();
				} else {
					this.setDownloadAllButtonState();
					this.ui.multipleResultsTitle.show();
					this.ui.singleResultTitle.hide();
				}
			},

			setDownloadAllButtonState: function() {
				var isAllDownloaded = true;
				this.searchResultsCollection.forEach(function(userTorrent) {
					isAllDownloaded = isAllDownloaded && (userTorrent.get('downloadStatus') !== 'NONE');
				});
				if (isAllDownloaded) {
					this.ui.downloadAllButton.hide();
				} else {
					this.ui.downloadAllButton.show();
				}
			},

			onDidYouMeanClick: function(event) {
				var id = $(event.target).attr('showid');
				var name = $(event.target).html();
				this.vent.trigger('did-you-mean-click', id, name);
			},

			onDownloadAllClick: function() {
				this.vent.trigger('search-result-download-all', this.searchResultsCollection.models);
			}
		});
	});
