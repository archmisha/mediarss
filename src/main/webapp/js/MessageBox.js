/**
 * Date: 06/01/13
 * Time: 14:23
 */
define(['jquery', 'fancybox', 'noty', 'jqueryMsgBox'],
	function($, Fancybox, Noty, JqueryMsgBox) {
		"use strict";

		return {
			show: function(element, options) {
				options = options || {};
				element.fancybox(options).click();
			},

			error: function(message) {
				$.msgBox({
					title: "Error",
					content: message,
					type: "error",
					buttons: [{ value: "OK" }]
				});

//				noty({
//					layout: 'topCenter',
//					text: message,
//					timeout: 5000,
//					type: 'error'
//				});
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
