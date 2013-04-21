/**
 * Date: 06/01/13
 * Time: 14:23
 */

define([],
	function() {
		"use strict";

		return {
			waitForDisplayAndCreate: function(selector, callback) {
				var f = function() {
					if ($(selector).length == 0) {
//						console.log('element not present yet');
						setTimeout(f, 50);
					} else {
//						console.log('element IS present');
						callback(selector);
					}
				};

//				console.log('Starting to wait for element');
				if ($(selector).length == 0) {
//					console.log('element not present yet');
					setTimeout(f, 50);
				} else {
					// element already present, no need timeouts
					callback(selector);
				}
			}
		};
	});
