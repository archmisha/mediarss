/*global define*/
define([
	'jquery',
	'marionette',
	'handlebars',
	'text!features/login/templates/login.tpl',
	'components/header/views/HeaderView',
	'features/login/views/LoginHeaderDescriptionView',
	'MessageBox'
],
	function($, Marionette, Handlebars, template, HeaderView, LoginHeaderDescriptionView, MessageBox) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'login',

			ui: {
				formContainer: '.login-form-container',
				status: '.login-status',
				passwordRecoveryBox: '.login-forgot-password-box',
				passwordRecoveryEmailField: '.login-forgot-password-email-input',
				passwordRecoverySubmitButton: '.login-forgot-password-btn'
			},

			regions: {
				headerRegion: '.login-header'
			},

			events: {
				'submit .login-form': 'onSubmit',
				'keypress input[name=username] input[name=password]': 'onInputKeyPress',
				'click .login-forgot-password': 'onForgotPasswordClick'
			},

			onSubmit: function() {
				if (this._login()) {
					return true;
				}
				return false;
			},

			onRender: function() {
				this.ui.form = $('#login-form');
				this.ui.formContainer.append(this.ui.form);
				this.ui.usernameField = this.ui.form.find('input[name=username]');
				this.ui.passwordField = this.ui.form.find('input[name=password]');
				this.ui.form.show();

				if ($.browser.mozilla) {
					this.ui.form.attr('action', '#');
				}

				this.headerRegion.show(new HeaderView({descriptionViewDef: LoginHeaderDescriptionView}));

				var that = this;
				setTimeout(function() {
					that.ui.usernameField.focus();
				}, 50);
			},

			onClose: function() {
				this.ui.form.hide();
				this.ui.usernameField.val('');
				this.ui.passwordField.val('');
				$('body').append(this.ui.form);
			},

			onInputKeyPress: function() {
				var ENTER_KEY = 13;
				if (event.which === ENTER_KEY) {
					this._login();
				}
			},

			showStatusMessage: function(message) {
				this.ui.status.text(message);
				this.ui.status.fadeIn('slow');
			},

			hideStatusMessage: function() {
				this.ui.status.fadeOut('slow');
				this.ui.status.val('');
			},

			_login: function() {
				this.hideStatusMessage();
				var username = this.ui.usernameField.val();
				var password = this.ui.passwordField.val();

				if (!username || username.trim().length == 0 || !password || password.trim().length == 0) {
					this.showStatusMessage('Invalid email or password');
					return false;
				}

				this.trigger('login', {
					username: username,
					password: password
				});

				if ($.browser.mozilla) {
					return false;
				} else {
					return true;
				}
			},

			onForgotPasswordClick: function() {
				var that = this;
				this.hideStatusMessage();

				MessageBox.show(this.ui.passwordRecoveryBox, {
					hideOnContentClick: true,
					afterShow: function() {
						that.ui.passwordRecoveryEmailField.on('keypress', function(event) {
							that.onForgotPasswordInputKeyPress(event);
						});
						that.ui.passwordRecoverySubmitButton.on('click', function() {
							that.onForgotPasswordButtonClick();
						});
						setTimeout(function() {
							that.ui.passwordRecoveryEmailField.focus();
						}, 50);
					},
					afterClose: function() {
						that.ui.passwordRecoveryEmailField.off('keypress');
						that.ui.passwordRecoverySubmitButton.off('click');
						that.ui.passwordRecoveryEmailField.val('');
					}
				});
			},

			onForgotPasswordInputKeyPress: function(event) {
				var ENTER_KEY = 13;
				if (event.which === ENTER_KEY) {
					this.onForgotPasswordButtonClick();
				}
			},

			onForgotPasswordButtonClick: function() {
				var email = this.ui.passwordRecoveryEmailField.val();

				if (!email || email.trim().length == 0) {
					return;
				}

				$.fancybox.close();
				this.trigger('forgot-password', {email: email});
			}
		});
	});
