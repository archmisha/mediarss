/*global define*/
define([
	'marionette',
	'handlebars',
	'text!components/traktTvImport/templates/trakt-tv-import.tpl',
        'components/section/views/SectionView',
        'utils/HttpUtils'
],
    function (Marionette, Handlebars, template, SectionView, HttpUtils) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
//			className: 'trakt-tab',

			ui: {
			},

			events: {
                'click .trakt-disconnect-button': 'onDisconnectButtonClick'
			},

			regions: {
				traktTvImportSectionRegion: '.trakt-tv-section'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
                this.clientId = options.clientId;
                this.isConnectedToTrakt = options.isConnectedToTrakt;
				this.traktTvImportSection = new SectionView({
					title: 'Trakt.tv Sync (in construction)',
					description: 'Connect with your account on <a href=\'http://trakt.tv/\' target=\'blank\'>trakt.tv</a>. ' +
                    'Every show you add to trakt.tv will be added automatically to your MediaRSS account as a tracked show.'
				});
                this.redirectUri = encodeURIComponent(window.location.href.substring(0, window.location.href.indexOf('#')) + '/rest/trakt/auth');
			},

			onRender: function() {
				this.traktTvImportSectionRegion.show(this.traktTvImportSection);
            },

            onDisconnectButtonClick: function () {
                var that = this;

                HttpUtils.get("rest/user/trakt/disconnect", function () {
                    that.clientId = null;
                    that.isConnectedToTrakt = null;
                    that.render();
                    //that.$el.hide();
                });
            },

            templateHelpers: function () {
                return {
                    'clientId': this.clientId,
                    'isConnectedToTrakt': this.isConnectedToTrakt,
                    'redirectUri': this.redirectUri //http%3A%2F%2F50.62.57.127%2Fmain
                };
            }
		});
	});
