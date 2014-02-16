/*global define*/

define([
	'marionette',
	'routers/RoutingPaths',
	'features/home/views/HomeView'
],
	function(Marionette, RoutingPaths, HomeView) {
		"use strict";

		return Marionette.Controller.extend({

			constructor: function(params) {
				Marionette.Controller.prototype.constructor.apply(this, arguments);
//				this.mainRegion = params.mainRegion;
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

			logout: function() {
				// it is host:port/#logout here
				var url = window.parent.location.href;
				url = url.substring(0, url.lastIndexOf('/') + 1);
				window.parent.location = url + "rest/user/logout";
			},

			_show: function(tabToSelect, tabParams) {
				this.homeView.changeTab(tabToSelect, tabParams);
			}
		});
	});
