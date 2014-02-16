/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/homeTab/templates/home-tab.tpl',
	'zeroClipboard',
	'jqplugin',
	'chosen',
	'utils/MessageBox',
	'utils/Utils',
	'utils/HttpUtils'
],
	function(Marionette, Handlebars, template, ZeroClipboard, jqPlugin, Chosen, MessageBox, Utils, HttpUtils) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'home-tab',

			ui: {
				tvShowsCopyLinkNotification: '.guide-tvshows-feed-copy-link-notification',
				moviesCopyLinkNotification: '.guide-movies-feed-copy-link-notification'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
				this.setCopyToClipboard();
			},

			setTabData: function(tabData) {
				this.tabData = tabData;
			},

			setCopyToClipboard: function() {
				this.$el.find('#guide-tvshows-feed-copy-link').attr('data-clipboard-text', this.tabData.tvShowsRssFeed);
				this.$el.find('#guide-movies-feed-copy-link').attr('data-clipboard-text', this.tabData.moviesRssFeed);

				if ($.browser.flash == true) {
//					console.log('YES FLASH');
					this.clip = new ZeroClipboard([this.$el.find('#guide-tvshows-feed-copy-link')[0], this.$el.find('#guide-movies-feed-copy-link')[0]]);
					this.clip.on('complete', this._onCopyComplete);
					this.clip.on('wrongflash noflash', function() {
						ZeroClipboard.destroy();
					});
				} else {
//					console.log('no FLASH');
					this.$el.find('#guide-tvshows-feed-copy-link')
						.attr('target', '_blank')
						.attr('href', this.tabData.tvShowsRssFeed);
					this.$el.find('#guide-movies-feed-copy-link')
						.attr('target', '_blank')
						.attr('href', this.tabData.moviesRssFeed);
				}
			},

			onClose: function() {
				this.clip.off('complete', this._onCopyComplete);
			},

			_onCopyComplete: function(client, args) {
				console.log("Copied text to clipboard: " + args.text + ' ' + this.id);
				var notification;
				if (this.id.indexOf('movies') > -1) {
					notification = $('.guide-movies-feed-copy-link-notification');
				} else {
					notification = $('.guide-tvshows-feed-copy-link-notification');
				}
				notification.fadeIn('slow', function() {
					setTimeout(function() {
						notification.fadeOut('slow');
					}, 2000);
				});
			}
		});
	});
