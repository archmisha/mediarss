/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/register/templates/register.tpl',
	'components/header/views/HeaderView',
	'features/register/views/RegisterHeaderDescriptionView'
],
	function(Marionette, Handlebars, template, HeaderView, RegisterHeaderDescriptionView) {
		"use strict";

		var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'login',

			ui: {
				firstNameField: 'input[name=firstName]',
				lastNameField: 'input[name=lastName]',
				usernameField: 'input[name=username]',
				passwordField: 'input[name=password]',
				status: '.register-status',
				registerButton: '.register-btn'
			},

			regions: {
				headerRegion: '.register-header'
			},

			events: {
				'click .register-btn': 'onRegister',
				'keypress input[name=firstName]': 'onInputKeyPress',
				'keypress input[name=lastName]': 'onInputKeyPress',
				'keypress input[name=username]': 'onInputKeyPress',
				'keypress input[name=password]': 'onInputKeyPress'
			},

			onRender: function() {
				this.headerRegion.show(new HeaderView({descriptionView: new RegisterHeaderDescriptionView()}));

				var that = this;
				setTimeout(function() {
					that.ui.firstNameField.focus();
				}, 50);
			},

			onInputKeyPress: function(event) {
				var ENTER_KEY = 13;
				if (event.which === ENTER_KEY) {
					this.onRegister();
				}
			},

			_showError: function(msg) {
				this._resetStatus();
				this.ui.status.text(msg);
				this.ui.status.addClass('register-status-error');
				this.ui.status.fadeIn('slow');
			},

			_showInfo: function(msg) {
				this._resetStatus();
				this.ui.status.text(msg);
				this.ui.status.addClass('register-status-info');
				this.ui.status.fadeIn('slow');
			},

			_showProgress: function() {
				this._resetStatus();
				this.ui.status.text("Please wait. This could take a few moments.");
				this.ui.status.addClass('register-status-progress');
				this.ui.status.fadeIn('slow');
			},

			_resetStatus: function() {
				this.ui.status.val('');
				this.ui.status.removeClass('register-status-progress');
				this.ui.status.removeClass('register-status-error');
				this.ui.status.removeClass('register-status-info');
			},

			_disableRegisterButton: function() {
				this.ui.registerButton.text('Registering ...');
				this.ui.registerButton.attr('disabled', 'disabled');
				this.ui.registerButton.addClass('btn-disabled');
			},

			_enableRegisterButton: function() {
				this.ui.registerButton.removeAttr('disabled');
				this.ui.registerButton.text('Register');
				this.ui.registerButton.removeClass('btn-disabled');
			},

			onRegister: function() {
				var that = this;

				that._disableRegisterButton();
				this.ui.status.fadeOut('slow', function() {
//					that._resetStatus();
					var firstName = that.ui.firstNameField.val();
					var lastName = that.ui.lastNameField.val();
					var username = that.ui.usernameField.val();
					var password = that.ui.passwordField.val();

					if (!firstName || firstName.trim().length == 0 || !lastName || lastName.trim().length == 0 || !username || username.trim().length == 0 || !password || password.trim().length == 0 || !emailReg.test(username)) {
						that._showError('Please provide valid details');
						that._enableRegisterButton();
						return;
					}

					that._showProgress();
					$.post("rest/user/register", {
						firstName: firstName,
						lastName: lastName,
						username: username,
						password: password
					}).success(function(res) {
							that._enableRegisterButton();

							if (res.success) {
								that._showInfo(res.message);
								that.ui.firstNameField.val('');
								that.ui.lastNameField.val('');
								that.ui.usernameField.val('');
								that.ui.passwordField.val('');
							} else {
								that._showError(res.message);
							}
						}).error(function(res) {
							console.log('error. data: ' + res);
						});
				});
			}
		});
	});
