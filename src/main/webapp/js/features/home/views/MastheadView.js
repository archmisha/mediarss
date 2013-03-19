/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/home/templates/masthead.tpl',
	'MessageBox',
	'HttpUtils'
],
	function(Marionette, Handlebars, template, MessageBox, HttpUtils) {
		"use strict";

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
				this.user = options.user;
				this.initialData = options.initialData;
			},

			templateHelpers: function() {
				return {
					'username': this.user.firstName,
					'updated-on': this.initialData.deploymentDate
				};
			},

			onSupportClick: function() {
				var that = this;
				MessageBox.show($('.masthead-support-box'), {
					hideOnContentClick: false,
					closeBtn: false,
					afterShow: function() {
						$('.masthead-support-submit-button').on('click', function() {
							that.onSubmitButtonClick();
						});

						$('.masthead-support-cancel-button').on('click', function() {
							$.fancybox.close();
						});
					}
				});
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
