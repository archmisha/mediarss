/**
 * Date: 06/01/13
 * Time: 14:23
 */
define(['jquery', 'fancybox', 'noty', 'jqueryMsgBox'],
	function($, Fancybox, Noty, JqueryMsgBox) {
		"use strict";

		return {
			createDialog: function(element, options) {
				options = options || {};
				var dialog = element.fancybox(options);
				return {
					show: function() {
						dialog.click();
					}
				};
			},

			error: function(message) {
				$.msgBox({
					title: "Error",
					content: message,
					type: "error",
					buttons: [
						{ value: "OK" }
					]
				});
			},

			infoModal: function(title, message) {
				$.msgBox({
					title: title,
					content: message,
					type: "info",
					buttons: [
						{ value: "OK" }
					]
				});
			},

			sessionTimeout: function() {
				$.msgBox({
					title: 'Authentication Timeout',
					content: "An authentication timeout has been detected.<br/>The application will log out and redirect to login page.",
					type: "error",
					buttons: [
						{ value: "Proceed" }
					],
					success: function(result) {
						if (result == "Proceed") {
							var url = window.parent.location.href;
							url = url.substring(0, url.indexOf('#'));
							window.parent.location = url;
						}
					}
				});
			},

			info: function(message) {
				noty({
					layout: 'topCenter',
					text: message,
					timeout: 5000,
					type: 'success'
				});
			}
		};
	});
