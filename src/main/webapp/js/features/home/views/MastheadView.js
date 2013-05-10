/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/home/templates/masthead.tpl',
	'MessageBox',
	'HttpUtils',
	'utils/Utils'
],
	function(Marionette, Handlebars, template, MessageBox, HttpUtils, Utils) {
		"use strict";

		var SUPPORT_DIALOG_SELECTOR = '.masthead-support-box';

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'masthead',

			ui: {
				content: '.masthead-support-content'
			},

			events: {
				'click .masthead-support': 'onSupportClick'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.tabData = options.tabData;
			},

			onShow: function() {
				var that = this;
				Utils.waitForDisplayAndCreate(SUPPORT_DIALOG_SELECTOR, function() {
					that.createSupportDialog();
				});
			},

			createSupportDialog: function() {
				var that = this;
				this._supportDialog = MessageBox.createDialog($(SUPPORT_DIALOG_SELECTOR), {
					hideOnContentClick: false,
					closeBtn: false,
					afterShow: function() {
						$('.masthead-support-submit-button').on('click', function() {
							that.onSubmitButtonClick();
						});

						$('.masthead-support-cancel-button').on('click', function() {
							$.fancybox.close();
						});
					},
					afterClose: function() {
						$('.masthead-support-submit-button').off('click');
						$('.masthead-support-cancel-button').off('click');
						that.ui.content.val('');
					}
				});
			},

			templateHelpers: function() {
				return {
					'username': this.tabData.firstName,
					'updated-on': this.tabData.deploymentDate
				};
			},

			onSupportClick: function() {
				this._supportDialog.show();
			},

			onSubmitButtonClick: function() {
				var content = this.ui.content.val();
				var type = $('input[name=type]:checked', '.masthead-support-box-content').val();

				if (!content || content.trim().length == 0) {
					return;
				}

				var that = this;
				HttpUtils.post("rest/support", {type: type, content: content}, function(res) {
					that.ui.content.val('');
					MessageBox.info(res);
					$.fancybox.close();
				});
			}
		});
	});
