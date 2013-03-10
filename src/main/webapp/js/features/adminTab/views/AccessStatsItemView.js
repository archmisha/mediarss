define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/access-stats-item.tpl',
	'qtip'
],
	function(Marionette, Handlebars, template, qtip) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'access-stats-item',

			ui: {
				userName: '.username-column'
			},

			events: {
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
				this.ui.userName.qtip({
					style: 'rssStyle'
				});
			},

			templateHelpers: function() {
				return {
					'lastLoginFormatted': this.model.get('lastLogin'),
					'lastShowsFeedGeneratedFormatted': this.model.get('lastShowsFeedGenerated'),
					'lastMoviesFeedGeneratedFormatted': this.model.get('lastMoviesFeedGenerated')
				};
			}
		});
	});

