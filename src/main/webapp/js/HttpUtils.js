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
						if (mask === undefined || mask === true) {
							Spinner.unmask();
						}
						MessageBox.error(res);
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
						if (mask === undefined || mask === true) {
							Spinner.unmask();
						}

						MessageBox.error(res);
					});
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
