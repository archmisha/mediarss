/**
 * Date: 06/01/13
 * Time: 14:23
 */
define([
	'jquery',
	'utils/MessageBox',
	'utils/Spinner'
],
	function($, MessageBox, Spinner) {
		"use strict";

		var TIMEOUT = 10;
		return {
			post: function(url, params, success, mask) {
				if (mask === undefined || mask === true) {
					Spinner.mask();
				}

				var that = this;
				setTimeout(function() {
					return $.post(url, params,function(data, textStatus, jqXHR) {
						that._handleSuccess(data, success, mask);
					}).fail(function(jqXHR, textStatus, errorThrown) {
						that._handleError(textStatus, errorThrown, mask);
					});
				}, TIMEOUT);
			},

			get: function(url, success, mask) {
				if (mask === undefined || mask === true) {
					Spinner.mask();
				}

				var that = this;
				setTimeout(function() {
					return $.get(url,function(data, textStatus, jqXHR) {
						that._handleSuccess(data, success, mask);
					}).fail(function(jqXHR, textStatus, errorThrown) {
						that._handleError(textStatus, errorThrown, mask);
					});
				}, TIMEOUT);
			},

			_handleError: function(textStatus, errorThrown, mask) {
				if (mask === undefined || mask === true) {
					Spinner.unmask();
				}

				if (textStatus === "abort") {
					return;
				} else if (textStatus === "timeout") {
					MessageBox.error('Server is not responding for too long');
				} else { //"parsererror", "error"
					MessageBox.error(errorThrown);
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
