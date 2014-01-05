/*global define*/
define([
	'marionette',
	'handlebars',
	'text!components/traktTvImport/templates/trakt-tv-import.tpl',
	'components/section/views/SectionView'
],
	function(Marionette, Handlebars, template, SectionView) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
//			className: 'trakt-tab',

			ui: {
			},

			events: {
			},

			regions: {
				traktTvImportSectionRegion: '.trakt-tv-section'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);

				this.traktTvImportSection = new SectionView({
					title: 'Trakt.tv Sync',
					description: 'Connect with your account on <a href=\'http://trakt.tv/\' target=\'blank\'>trakt.tv</a>. ' +
						'Every show you add to trakt.tv will be added automatically to your MediaRSS account as a tracked show.</br>' +
						'To enable syncing please enter your trakt.tv username and uncheck the \'Protect my data\' option in your account settings on trakt.tv'
				});
			},

			onRender: function() {
				this.traktTvImportSectionRegion.show(this.traktTvImportSection);
			}
		});
	});
