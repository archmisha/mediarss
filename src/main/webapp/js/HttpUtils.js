/**
 * Date: 06/01/13
 * Time: 14:23
 */
define(['MessageBox', 'Spinner'],
	function(MessageBox, Spinner) {
		"use strict";

		return {
			post: function(url, params, success, mask) {
				if (mask === undefined || mask === true) {
					Spinner.mask();
				}

				var that = this;
				return $.post(url, params)
					.success(function(res) {
						that._handleSuccess(res, success, mask);
					}).error(function(res) {
						that._handleError(res, mask);
					});
			},

			get: function(url, success, mask) {
				if (mask === undefined || mask === true) {
					Spinner.mask();
				}

				var that = this;
				return $.get(url)
					.success(function(res) {
						that._handleSuccess(res, success, mask);
					}).error(function(res) {
						that._handleError(res, mask);
					});
			},

			_handleError: function(res, mask) {
				if (mask === undefined || mask === true) {
					Spinner.unmask();
				}

				if (res.readyState === 0 && res.status === 0) {
					MessageBox.error('Unable to communicate with the server');
				} else if (res.status === 503) {
					MessageBox.error('Server is not responding for too long');
				} else {
					MessageBox.error(res);
				}
			},

			_handleSuccess: function(res, success, mask) {
				if (mask === undefined || mask === true) {
					Spinner.unmask();
				}

				if (res.success === false) {
					if (res.message == 'User is not logged in') {
						MessageBox.sessionTimeout();
					} else {
						MessageBox.error(res.message);
					}
				} else {
					success(res);
				}
			}
		};
	});
