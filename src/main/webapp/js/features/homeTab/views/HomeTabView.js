/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/homeTab/templates/home-tab.tpl',
	'zeroClipboard',
	'chosen',
	'utils/MessageBox',
	'utils/Utils',
],
	function(Marionette, Handlebars, template, ZeroClipboard, Chosen, MessageBox, Utils) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'home-tab',

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.tabData = options.tabData;
			},

			onRender: function() {
				this.setCopyToClipboard();
			},

			setCopyToClipboard: function() {
				this.$el.find('#guide-tvshows-feed-copy-link').attr('data-clipboard-text', this.tabData.tvShowsRssFeed);
				this.$el.find('#guide-movies-feed-copy-link').attr('data-clipboard-text', this.tabData.moviesRssFeed);

				if (Utils.isFlashEnabled()) {
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
					notification = 'Movies';
				} else {
					notification = 'TV Shows';
				}
				MessageBox.info(notification + ' rss feed link is copied to clipboard');
			}
		});
	});
