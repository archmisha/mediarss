/*global define*/
define([
        'marionette',
        'handlebars',
        'text!components/search-result/templates/search-results.tpl',
        'features/collections/UserTorrentCollection',
        'components/search-result/views/SearchResultsCollectionView'
    ],
    function (Marionette, Handlebars, template, UserTorrentCollection, SearchResultsCollectionView) {
        "use strict";

        return Marionette.Layout.extend({
            template: Handlebars.compile(template),
            className: 'search-results',

            ui: {
                searchResultsRegion: '.search-results-list',
                resultsCount: '.search-results-header-count',
                resultsHeader: '.search-results-header',
                titleContainer: '.search-results-header-title-container',
                downloadAllButton: '.search-results-download-all-button',
                multipleResultsTitle: '.search-results-header-title-multiple',
                singleResultTitle: '.search-results-header-title-single',
                showingResultsFor: '.search-results-showing-results-for',
                showingResultsForText: '.search-results-showing-results-for-text',
                didYouMean: '.search-results-did-you-mean',
                didYouMeanList: '.search-results-did-you-mean-list'
            },

            events: {
                'click .search-results-did-you-mean-list': 'onDidYouMeanClick',
                'click .search-results-download-all-button': 'onDownloadAllClick'
            },

            regions: {
                searchResultsRegion: '.search-results-list'
            },

            constructor: function (options) {
                this.vent = options.vent;
                Marionette.Layout.prototype.constructor.apply(this, arguments);
            },

            showDidYouMean: function (searchResult) {
                var arr = [];
                searchResult.get('didYouMean').forEach(function (item) {
                    arr.push('<span class=\'search-results-did-you-mean-item\' item_id=\'' + item.id + '\'>' + item.name + '</span>');
                });
                this.ui.didYouMeanList.html(arr.join(', '));
                this.ui.didYouMean.show();
            },

            setSearchResults: function (searchResult) {
                var that = this;
                this.ui.resultsHeader.hide();
                this.ui.didYouMean.hide();
                this.ui.titleContainer.hide();
                this.ui.showingResultsFor.hide();
                if (this.ui.searchResultsRegion.is(":visible")) {
                    this.ui.searchResultsRegion.slideUp('slow', function () {
                        that.ui.searchResultsRegion.hide();
                    });
                }

                if (searchResult.get('episodes').length > 0) {
                    this.ui.resultsHeader.show();
                    this.onSearchResultsReceived(searchResult);
                } else {
                    var didYouMean = searchResult.get('didYouMean');
                    var actualSearchTerm = searchResult.get('actualSearchTerm');
                    var originalSearchTerm = searchResult.get('originalSearchTerm');

                    if (didYouMean !== undefined && didYouMean.length > 0) {
                        this.ui.resultsHeader.show();
                        this.showDidYouMean(searchResult);
                        this.setVisibleShowingResultsFor(actualSearchTerm, actualSearchTerm != null && actualSearchTerm !== originalSearchTerm);
                    }

                    if (actualSearchTerm != null) {
                        this._showSearchResultsCollectionView();
                    }
                }
            },

            setVisibleShowingResultsFor: function (text, visible) {
                this.ui.showingResultsForText.html(text);
                if (visible) {
                    this.ui.showingResultsFor.show();
                    this.ui.resultsHeader.addClass('search-results-header-with-showing-results-for');
                } else {
                    this.ui.showingResultsFor.hide();
                    this.ui.resultsHeader.removeClass('search-results-header-with-showing-results-for');
                }
            },

            onSearchResultsReceived: function (searchResult) {
                var episodes = searchResult.get('episodes');
                var actualSearchTerm = searchResult.get('actualSearchTerm');
                var originalSearchTerm = searchResult.get('originalSearchTerm');
                var didYouMean = searchResult.get('didYouMean');

                this.setVisibleShowingResultsFor(actualSearchTerm, actualSearchTerm !== originalSearchTerm);
                if (didYouMean !== undefined && didYouMean.length > 0) {
                    this.showDidYouMean(searchResult);
                }

                this.ui.resultsCount.html(episodes.length);
                this.ui.titleContainer.show();
                this._showSearchResultsCollectionView(episodes);

                this.setDownloadAllButtonState();
                if (episodes.length === 1) {
                    this.ui.multipleResultsTitle.hide();
                    this.ui.singleResultTitle.show();
                } else {
                    this.ui.multipleResultsTitle.show();
                    this.ui.singleResultTitle.hide();
                }
            },

            _showSearchResultsCollectionView: function (episodes) {
                episodes = episodes || [];
                this.searchResultsCollection = new UserTorrentCollection(episodes);
                this.searchResultsCollection.bind('change:downloadStatus', this.setDownloadAllButtonState, this);

                var searchResultsCollectionView = new SearchResultsCollectionView({
                    collection: this.searchResultsCollection,
                    vent: this.vent
                });
                var that = this;
                searchResultsCollectionView.on('render', function () {
                    // if there is scroll bar - move download all button more to the left
                    //if (that.searchResultsRegion.$el.get(0).scrollHeight > that.searchResultsRegion.$el.height()) {
                    if (that.searchResultsCollection.length > 8) {
                        that.ui.downloadAllButton.addClass('search-results-download-all-button-with-scroll');
                    } else {
                        that.ui.downloadAllButton.removeClass('search-results-download-all-button-with-scroll');
                    }
                });
                this.ui.searchResultsRegion.slideDown('slow', function () {
                    that.ui.searchResultsRegion.show();
                });
                this.searchResultsRegion.show(searchResultsCollectionView);
            },

            setDownloadAllButtonState: function () {
                if (this.searchResultsCollection.length == 1) {
                    this.ui.downloadAllButton.hide();
                    return;
                }

                var isAllDownloaded = true;
                this.searchResultsCollection.forEach(function (userTorrent) {
                    isAllDownloaded = isAllDownloaded && (userTorrent.get('downloadStatus') !== 'NONE');
                });
                if (isAllDownloaded) {
                    this.ui.downloadAllButton.hide();
                } else {
                    this.ui.downloadAllButton.show();
                }
            },

            onDidYouMeanClick: function (event) {
                var id = $(event.target).attr('item_id');
                var name = $(event.target).html();
                this.vent.trigger('did-you-mean-click', id, name);
            },

            onDownloadAllClick: function () {
                this.vent.trigger('search-result-download-all', this.searchResultsCollection.models);
            }
        });
    });
