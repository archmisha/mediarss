/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/homeTab/templates/home-tab.tpl',
	'components/section/views/SectionView',
	'zeroClipboard',
	'jqplugin',
	'chosen',
	'MessageBox',
	'utils/Utils',
	'HttpUtils'
],
	function(Marionette, Handlebars, template, SectionView, ZeroClipboard, jqPlugin, Chosen, MessageBox, Utils, HttpUtils) {
		"use strict";

		var SUBTITLES_NONE = 'None';
		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'home-tab',

			ui: {
				subtitlesCombobox: '.subtitles-settings-combobox',
				tvShowsCopyLinkNotification: '.tvshows-feed-copy-link-notification',
				moviesCopyLinkNotification: '.movies-feed-copy-link-notification'
			},

			events: {
				'change .subtitles-settings-combobox': 'onSubtitlesComboboxChange'
			},

			regions: {
				rssFeedsSectionRegion: '.rss-feeds-section',
				subtitlesSectionRegion: '.subtitles-section'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.loggedInUserData = options.loggedInUserData;
				this.initialData = options.initialData;

				this.rssFeedsSection = new SectionView({
					title: 'Your personalized RSS feeds',
					description: 'Those are your personalized rss feeds. You can add them to your torrent client (such as uTorrent)'
				});
				this.subtitlesSection = new SectionView({
					title: 'Subtitles',
					description: 'Subtitles could be added to your rss feeds (both movies and tv shows)'
				});
			},

			onRender: function() {
				var that = this;
				var subtitleValues = [SUBTITLES_NONE];
				subtitleValues = subtitleValues.concat(this.initialData.subtitles);
				subtitleValues.forEach(function(subtitle) {
					that.ui.subtitlesCombobox.append(
						$('<option></option>').val(subtitle).html(subtitle)
					);
				});

				this.rssFeedsSectionRegion.show(this.rssFeedsSection);
				this.subtitlesSectionRegion.show(this.subtitlesSection);

				// register copy to clipboard
				this.setCopyToClipboard();
			},

			setCopyToClipboard: function() {
				if ($.browser.flash == true) {
//					console.log('YES FLASH');
					var clip = new ZeroClipboard([this.$el.find('#tvshows-feed-copy-link')[0], this.$el.find('#movies-feed-copy-link')[0]], {
						moviePath: "ZeroClipboard.swf"
					});
					clip.on('complete', function(client, args) {
						console.log("Copied text to clipboard: " + args.text + ' ' + this.id);
						var notification;
						if (this.id.indexOf('movies') > -1) {
							notification = $('.movies-feed-copy-link-notification');
						} else {
							notification = $('.tvshows-feed-copy-link-notification');
						}
						notification.fadeIn('slow', function() {
							setTimeout(function() {
								notification.fadeOut('slow');
							}, 2000);
						});
					});
				} else {
//					console.log('no FLASH');
					this.$el.find('#tvshows-feed-copy-link')
						.attr('target', '_blank')
						.attr('href', this.loggedInUserData.tvShowsRssFeed);
					this.$el.find('#movies-feed-copy-link')
						.attr('target', '_blank')
						.attr('href', this.loggedInUserData.moviesRssFeed);
				}
			},

			templateHelpers: function() {
				return {
					'tvshowsRssFeed': this.loggedInUserData.tvShowsRssFeed,
					'moviesRssFeed': this.loggedInUserData.moviesRssFeed
				};
			},

			onSubtitlesComboboxChange: function() {
				var selectedSubtitle = this.ui.subtitlesCombobox.val();//select2("val");//this.ui.subtitlesCombobox.find('option:selected').text();
				if (selectedSubtitle == SUBTITLES_NONE) {
					selectedSubtitle = null;
				}

				HttpUtils.post("rest/user/subtitles", {subtitles: selectedSubtitle}, function(res) {
					MessageBox.info('Subtitles preferences were saved');
				});
			},

			onShow: function() {
				Utils.waitForDisplayAndCreate('.subtitles-settings-combobox', this.createChosen);

				if (this.loggedInUserData.user.subtitles) {
					this.ui.subtitlesCombobox.val(this.loggedInUserData.user.subtitles);
				}
			},

			createChosen: function(selector) {
				$(selector).chosen();
			}
		});
	});
