/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/tvShowsTab/templates/tracked-shows-desc.tpl',
	'MessageBox',
	'HttpUtils'
],
	function(Marionette, Handlebars, template, MessageBox, HttpUtils) {
		"use strict";

		var urlRegExp = new RegExp('http://www.tv.com/shows/.+');

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'tracked-shows-desc',

			ui: {
				addManuallyBox: '.tracked-shows-add-manually-box',
				urlField: '.tracked-shows-add-manually-input',
				submitButton: '.tracked-shows-add-manually-btn'
			},

			events: {
				'click .tracked-shows-add-manually': 'onAddClick',
				'click .tracked-shows-add-manually-btn': 'onSubmitButtonClick'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.vent = options.vent;
			},

			onRender: function() {
			},

			onAddClick: function() {
				var that = this;
				MessageBox.show(this.ui.addManuallyBox, {
					hideOnContentClick: true,
					afterShow: function() {
						that.ui.urlField.on('keypress', function(event) {
							that.onUrlInputKeyPress(event);
						});
						that.ui.submitButton.on('click', function() {
							that.onSubmitButtonClick();
						});
						setTimeout(function() {
							that.ui.urlField.focus();
						}, 50);
					},
					afterClose: function() {
						that.ui.urlField.off('keypress');
						that.ui.submitButton.off('click');
						that.ui.urlField.val('');
					}
				});
			},

			onUrlInputKeyPress: function(event) {
				var ENTER_KEY = 13;
				if (event.which === ENTER_KEY) {
					this.onSubmitButtonClick();
				}
			},

			onSubmitButtonClick: function() {
				var url = this.ui.urlField.val();

				if (!url || url.trim().length == 0 || !urlRegExp.test(url)) {
					return;
				}

				var that = this;
				HttpUtils.post("rest/shows/addManual", {
					showTvComUrl: url
				}, function(res) {
					that.vent.trigger('add-manual-show', {show: res.show});
					$.fancybox.close();
				});
			}
		});
	});
