define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/access-stats-item.tpl',
	'qtip',
	'HttpUtils'
],
	function(Marionette, Handlebars, template, qtip, HttpUtils) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'access-stats-item',

			ui: {
				userName: '.username-column',
				impersonateButton: '.impersonate-button'
			},

			events: {
				'click .impersonate-button': '_onImpersonateButtonClick'
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
				this.ui.userName.qtip({
					style: 'rssStyle'
				});
				if (this.model.get('loggedIn')) {
					this.ui.impersonateButton.hide();
				}
			},

			templateHelpers: function() {
				return {
					'lastLoginFormatted': this.model.get('lastLogin'),
					'lastShowsFeedGeneratedFormatted': this.model.get('lastShowsFeedGenerated'),
					'lastMoviesFeedGeneratedFormatted': this.model.get('lastMoviesFeedGenerated')
				};
			},

			_onImpersonateButtonClick: function() {
				console.log('impersonating: ' + this.model.id);
				HttpUtils.get("rest/admin/impersonate/" + this.model.id, function(res) {
					window.parent.location.reload();
				});
			}
		});
	});

