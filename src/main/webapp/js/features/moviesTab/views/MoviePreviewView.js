/*global define*/
define([
	'jquery',
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movie-preview.tpl',
	'utils/HttpUtils',
	'utils/MessageBox',
	'moment',
	'utils/StringUtils',
	'routers/RoutingPaths',
	'utils/Spinner'
],
	function($, Marionette, Handlebars, template, HttpUtils, MessageBox, Moment, StringUtils, RoutingPaths, Spinner) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),

			ui: {
				moviePreviewContainer: '.movie-preview-container'
			},

			events: {
			},

			regions: {
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.movieId = options.tabParams[0];
			},

			onRender: function() {
				Spinner.mask();
				var that = this;
				this.ui.moviePreviewContainer.load(function() {
					Spinner.unmask();
					that.ui.moviePreviewContainer.contents().css({
						margin: '0px',
						padding: '0px',
						border: '0px'
					});
					var delta = 5;
					var height = that.ui.moviePreviewContainer.contents().find("body").height() + delta;
					that.ui.moviePreviewContainer.attr("height", height);

					var f = function() {
						var newHeight = that.ui.moviePreviewContainer.contents().find("html").height() + delta;
						var curHeight = that.ui.moviePreviewContainer.attr("height");
						console.log('curHeight ' + curHeight + ' ' + newHeight);
						if (curHeight < newHeight) {
							console.log('in');
							that.ui.moviePreviewContainer.attr("height", newHeight);
							setTimeout(f, 10);
						}
					};
					setTimeout(f, 10);
				});
			},

			templateHelpers: function() {
				return {
					'movieId': this.movieId
				};
			}
		});
	});
