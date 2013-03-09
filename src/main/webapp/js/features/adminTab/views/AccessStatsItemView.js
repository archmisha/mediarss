define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/access-stats-item.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'access-stats-item',

			ui: {
			},

			events: {
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
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

