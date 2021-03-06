/*global define*/
define([
	'jquery',
	'marionette',
	'handlebars',
	'text!features/settingsTab/templates/settings-tab.tpl',
	'components/section/views/SectionView',
	'zeroClipboard',
	'chosen',
	'utils/MessageBox',
	'utils/Utils',
	'utils/HttpUtils',
	'components/traktTvImport/views/TraktTvImportView',
	'features/settingsTab/views/SubtitlesCollectionView',
	'features/settingsTab/collections/SubtitlesCollection'
],
	function($, Marionette, Handlebars, template, SectionView, ZeroClipboard, Chosen, MessageBox, Utils, HttpUtils, TraktTvImportView, SubtitlesCollectionView, SubtitlesCollection) {
		"use strict";

		var SUBTITLES_NONE = 'None';
		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'settings-tab',

			ui: {
				subtitlesCombobox: '.subtitles-settings-combobox',
				recentSubtitlesListContainer: '.recent-subtitles-container'
			},

			events: {
				'change .subtitles-settings-combobox': 'onSubtitlesComboboxChange'
			},

			regions: {
				rssFeedsSectionRegion: '.rss-feeds-section',
				subtitlesSectionRegion: '.subtitles-section',
				traktTvRegion: '.trakt-tv-import-container',
				recentSubtitlesRegion: '.recent-subtitles-list'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.tabData = options.tabData;

				this.rssFeedsSection = new SectionView({
					title: 'Your personalized RSS feeds',
					description: 'Those are your personalized rss feeds. You can add them to your torrent client (such as <a href="http://www.utorrent.com/">uTorrent</a>)'
				});
				this.subtitlesSection = new SectionView({
					title: 'Subtitles (in construction)',
					description: 'Subtitles could be added to your rss feeds (both movies and tv shows)'
				});
                this.traktTvImport = new TraktTvImportView({
                    clientId: this.tabData.traktClientId,
                    isConnectedToTrakt: this.tabData.isConnectedToTrakt
                });

				this.recentSubtitlesCollection = new SubtitlesCollection();
				this.recentSubtitlesView = new SubtitlesCollectionView({collection: this.recentSubtitlesCollection});
			},

			onRender: function() {
				var that = this;
				HttpUtils.get('rest/home/initial-data', function (res) {
					$.extend(that.tabData, res);
					var subtitleValues = [SUBTITLES_NONE].concat(that.tabData.subtitles);
					subtitleValues.forEach(function(subtitle) {
						that.ui.subtitlesCombobox.append(
							$('<option></option>').val(subtitle).html(subtitle)
						);
					});

					that.rssFeedsSectionRegion.show(that.rssFeedsSection);
					that.subtitlesSectionRegion.show(that.subtitlesSection);
					that.traktTvRegion.show(that.traktTvImport);

					// register copy to clipboard
					that.setCopyToClipboard();

					Utils.waitForDisplayAndCreate('.subtitles-settings-combobox', function(selector) {
						that.createChosen(selector);
					});

					that.recentSubtitlesRegion.show(that.recentSubtitlesView);
					that.recentSubtitlesCollection.reset(that.tabData.recentSubtitles);
					if (that.tabData.recentSubtitles.length === 0) {
						that.ui.recentSubtitlesListContainer.hide();
					}
				}, false);
			},

			setCopyToClipboard: function() {
				this.$el.find('#tvshows-feed-copy-link').attr('data-clipboard-text', this.tabData.tvShowsRssFeed);
				this.$el.find('#movies-feed-copy-link').attr('data-clipboard-text', this.tabData.moviesRssFeed);

				if (Utils.isFlashEnabled()) {
//					console.log('YES FLASH');
					this.clip = new ZeroClipboard([this.$el.find('#tvshows-feed-copy-link')[0], this.$el.find('#movies-feed-copy-link')[0]]);
					this.clip.on('complete', this._onCopyComplete);
					this.clip.on('wrongflash noflash', function() {
						ZeroClipboard.destroy();
					});
				} else {
//					console.log('no FLASH');
					this.$el.find('#tvshows-feed-copy-link')
						.attr('target', '_blank')
						.attr('href', this.tabData.tvShowsRssFeed);
					this.$el.find('#movies-feed-copy-link')
						.attr('target', '_blank')
						.attr('href', this.tabData.moviesRssFeed);
				}
			},

			onClose: function() {
				if (this.clip) {
					this.clip.off('complete', this._onCopyComplete);
				}
			},

			_onCopyComplete: function(client, args) {
				console.log("Copied text to clipboard: " + args.text + ' ' + this.id);
				var notification;
				if (this.id.indexOf('movies') > -1) {
					notification = 'Movies';
				} else {
					notification = 'TV Shows';
				}
				MessageBox.info(notification + ' rss feed link is copied to clipboard');
			},

			onSubtitlesComboboxChange: function() {
				var selectedSubtitle = this.ui.subtitlesCombobox.val();//select2("val");//this.ui.subtitlesCombobox.find('option:selected').text();
				if (selectedSubtitle == SUBTITLES_NONE) {
					selectedSubtitle = null;
				}

				HttpUtils.post("rest/user/subtitles", {subtitles: selectedSubtitle}, function (res) {
					MessageBox.info('Subtitles preferences were saved');
				});
			},

			createChosen: function(selector) {
				$(selector).chosen();

				if (this.tabData.userSubtitles) {
					$(selector).val(this.tabData.userSubtitles);
					$(selector).trigger("liszt:updated");
				}
			}
		});
	});
