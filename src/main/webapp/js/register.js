$(function() {
	var usernameInput = $('input[name=username]');
	var passwordInput = $('input[name=password]');
	var firstNameInput = $('input[name=firstName]');
	var lastNameInput = $('input[name=lastName]');
	var status = $('.register-status');
	var registerButton = $('.register-btn');

	var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;

	function showError(msg) {
		resetStatus();
		status.text(msg);
		status.addClass('register-status-error');
		status.fadeIn('slow');
	}

	function showInfo(msg) {
		resetStatus();
		status.text(msg);
		status.addClass('register-status-info');
		status.fadeIn('slow');
	}

	function showProgress() {
		resetStatus();
		status.text("Please wait. This could take a few moments.");
		status.addClass('register-status-progress');
		status.fadeIn('slow');
	}

	function resetStatus() {
		status.val('');
		status.removeClass('register-status-progress');
		status.removeClass('register-status-error');
		status.removeClass('register-status-info');
	}

	function disableRegisterButton() {
		registerButton.text('Registering ...');
		registerButton.attr('disabled', 'disabled');
		registerButton.addClass('btn-disabled');
	}

	function enableRegisterButton() {
		registerButton.removeAttr('disabled');
		registerButton.text('Register');
		registerButton.removeClass('btn-disabled');
	}

	function onRegister() {
		disableRegisterButton();
		status.fadeOut('slow', function() {
			var firstName = firstNameInput.val();
			var lastName = lastNameInput.val();
			var username = usernameInput.val();
			var password = passwordInput.val();

			if (!firstName || firstName.trim().length == 0 || !lastName || lastName.trim().length == 0 || !username || username.trim().length == 0 || !password || password.trim().length == 0 || !emailReg.test(username)) {
				showError('Please provide valid details');
				enableRegisterButton();
				return;
			}

			showProgress();
			$.post("rest/user/register", {
				firstName: firstName,
				lastName: lastName,
				username: username,
				password: password
			}).success(function(res) {
					enableRegisterButton();

					if (res.success) {
						showInfo(res.message);
						firstNameInput.val('');
						lastNameInput.val('');
						usernameInput.val('');
						passwordInput.val('');
					} else {
						showError(res.message);
					}
				}).error(function(res) {
					console.log('error. data: ' + res);
				});
		});
	}

	[usernameInput, passwordInput, firstNameInput, lastNameInput].forEach(function(inputEl) {
		inputEl.keypress(function(event) {
			var ENTER_KEY = 13;
			if (event.which === ENTER_KEY) {
				onRegister();
			}
		});
	});

	registerButton.click(function() {
		onRegister();
	});

	firstNameInput.focus();
});