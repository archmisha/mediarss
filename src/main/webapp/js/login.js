$(function() {
	var status = $('.login-status');
	var usernameInput = $('input[name=username]');
	var passwordInput = $('input[name=password]');
	var passwordRecoveryEmailField = $('.login-forgot-password-email-input');
	var passwordRecoverySubmitButton = $('.login-forgot-password-btn');

	function showStatusMessage(msg) {
		status.text(msg);
		status.fadeIn('slow');
	}

	function hideStatusMessage() {
		status.fadeOut('slow');
		status.val('');
	}

	function login() {
		hideStatusMessage();
		var username = usernameInput.val();
		var password = passwordInput.val();

		if (!username || username.trim().length == 0 || !password || password.trim().length == 0) {
			showStatusMessage('Invalid email or password');
			return false;
		}

		var hash = $.param.fragment();
		console.log('hash: ' + hash);
		if (hash === '') {
			hash = 'home';
			console.log('hash: set to home');
		}

		$.post("rest/user/login", {
			username: username,
			password: password,
			tab: hash
		}, function(res) {
			if (res.success === undefined) {
				var str = window.location.href;
				window.location.href = str.substring(0, str.lastIndexOf('/') + 1) + 'main';
			} else {
				showStatusMessage(res.message);
			}
		});

//            if ($.browser.mozilla) {
		return false;
//            } else {
//                return true;
//            }
	}

	function onForgotPasswordButtonClick() {
		var email = passwordRecoveryEmailField.val();

		if (!email || email.trim().length == 0) {
			return;
		}

		$.fancybox.close();

		$.post("rest/user/forgot-password", {
			email: email
		}, function(res) {
			showStatusMessage(res.message);
		}, false);
	}

	function onForgotPasswordInputKeyPress(event) {
		var ENTER_KEY = 13;
		if (event.which === ENTER_KEY) {
			onForgotPasswordButtonClick();
		}
	}

	[usernameInput, passwordInput].forEach(function(inputEl) {
		inputEl.keypress(function(event) {
			var ENTER_KEY = 13;
			if (event.which === ENTER_KEY) {
				login();
			}
		});
	});

	$('.login-form').submit(function() {
		return login();
	});

	$('.login-forgot-password').click(function() {
		hideStatusMessage();
		passwordRecoveryDialog.click();
	});

	var passwordRecoveryDialog = $('.login-forgot-password-box').fancybox({
		hideOnContentClick: true,
		afterShow: function() {
			passwordRecoveryEmailField.on('keypress', function(event) {
				onForgotPasswordInputKeyPress(event);
			});
			passwordRecoverySubmitButton.on('click', function() {
				onForgotPasswordButtonClick();
			});
			setTimeout(function() {
				passwordRecoveryEmailField.focus();
			}, 50);
		},
		afterClose: function() {
			passwordRecoveryEmailField.off('keypress');
			passwordRecoverySubmitButton.off('click');
			passwordRecoveryEmailField.val('');
		}
	});

	usernameInput.focus();
});