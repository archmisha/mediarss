/**
 * Date: 06/01/13
 * Time: 14:23
 */
define(['fancybox', 'noty'],
	function(Fancybox, Noty) {
		"use strict";

		return {
			show: function(element, options) {
				options = options || {};
				element.fancybox(options).click();
			},

			error: function(message) {
				noty({
					layout: 'topCenter',
					text: message,
					timeout: 5000,
					type: 'error'
				});
			},

			info: function(message) {
				noty({
					layout: 'topCenter',
					text: message,
					timeout: 5000,
					type: 'information'
				});
			}
		};
	});
