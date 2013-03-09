define([
	'marionette',
	'handlebars',
	'MessageBox'
],
	function(Marionette, Handlebars, MessageBox) {
		"use strict";

		return Marionette.ItemView.extend({
			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
			},

			show: function() {
				var that = this;
				MessageBox.show($('.session-timeout-box'), {
					hideOnContentClick: false,
					closeBtn: false,
					afterShow: function() {
						$('.session-timeout-button').on('click', that.onProceedButtonClick);
					}
				});
			},

			onProceedButtonClick: function() {
				var url = window.parent.location.href;
				url = url.substring(0, url.indexOf('#'));
				window.parent.location = url;
			}
		});
	});

