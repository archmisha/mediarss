/*global define*/

define([
	'marionette',
	'routers/RoutingPaths',
	'features/home/views/HomeView',
	'utils/Utils'
],
	function(Marionette, RoutingPaths, HomeView, Utils) {
		"use strict";

		return Marionette.Controller.extend({

			constructor: function(params) {
				Marionette.Controller.prototype.constructor.apply(this, arguments);
			},

			initialize: function(options) {
				this.homeView = new HomeView();
				options.mainRegion.show(this.homeView);
			},

			showHomePage: function() {
				this._show(RoutingPaths.HOME, []);
			},

			showTVShows: function() {
				this._show(RoutingPaths.TVSHOWS, []);
			},

			showMovies: function(category) {
				this._show(RoutingPaths.MOVIES, [category]);
			},

			showSettings: function(category) {
				this._show(RoutingPaths.SETTINGS, [category]);
			},

			showAdmin: function() {
				this._show(RoutingPaths.ADMIN);
			},

			showMoviePreview: function(movieId) {
				this._show(RoutingPaths.MOVIE_PREVIEW, [movieId]);
			},

			logout: function() {
				// it is host:port/#logout here
				window.parent.location = Utils.getBaseUrl() + "/rest/user/logout";
			},

			_show: function(tabToSelect, tabParams) {
				this.homeView.changeTab(tabToSelect, tabParams);
			}
		});
	});
