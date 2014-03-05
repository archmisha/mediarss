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
				f();
			},

			isFlashEnabled: function() {
				var hasFlash = false;
				try {
					var fo = new ActiveXObject('ShockwaveFlash.ShockwaveFlash');
					if (fo) hasFlash = true;
				}
				catch (e) {
					if (navigator.mimeTypes ["application/x-shockwave-flash"] != undefined) hasFlash = true;
				}
				return hasFlash;
			},

			fixForTooltip: function(text) {
				return text.replace('\'', '&#39;');
			},

			addTooltip: function(arr) {
				arr.forEach(
					function(el) {
						el.qtip({
							style: 'qtip-blue qtip-rounded',
							position: {
								my: 'top left',
								at: 'bottom left'
							}
						});
					});
			},

			getBaseUrl: function() {
				// first cut of up to # then find last index of '/'
				// otherwise localhost:8080/#movies/userMovies screws stuff
				var url = this.getBaseRouteUrl();
				url = url.substring(0, url.lastIndexOf('/'));
				return url;
			},

			getBaseRouteUrl: function() {
				// first cut of up to # then find last index of '/'
				// otherwise localhost:8080/#movies/userMovies screws stuff
				var url = window.parent.location.href;
				var ind = url.lastIndexOf('#');
				if (ind > -1) {
					url = url.substring(0, url.lastIndexOf('#') + 1);
				}
				return url;
			}
		};
	});
