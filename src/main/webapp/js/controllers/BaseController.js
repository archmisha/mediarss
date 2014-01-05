/*global define*/

define([
	'marionette',
	'StringUtils',
	'routers/RoutingPaths',
	'features/home/views/HomeView',
	'features/homeTab/views/HomeTabView',
	'features/showsTab/views/ShowsTabView',
	'features/moviesTab/views/MoviesTabView',
	'features/adminTab/views/AdminTabView',
	'HttpUtils'
],
	function(Marionette, StringUtils, RoutingPaths, HomeView, HomeTabView, ShowsTabView, MoviesTabView, AdminTabView, HttpUtils) {
		"use strict";

		var BASE_TITLE = 'Personalized Media RSS';
		var mainRegion = null;

		var tabParams = [];
		var tabToView = {};
		tabToView[RoutingPaths.HOME] = HomeTabView;
		tabToView[RoutingPaths.TVSHOWS] = ShowsTabView;
		tabToView[RoutingPaths.MOVIES] = MoviesTabView;
		tabToView[RoutingPaths.ADMIN] = AdminTabView;

		var tabToTitle = {};
		tabToTitle[RoutingPaths.HOME] = BASE_TITLE + ' - Home';
		tabToTitle[RoutingPaths.TVSHOWS] = BASE_TITLE + ' - TV Shows';
		tabToTitle[RoutingPaths.MOVIES] = BASE_TITLE + ' - Movies';
		tabToTitle[RoutingPaths.ADMIN] = BASE_TITLE + ' - Admin';

		var homeView = null;

		return Marionette.Controller.extend({

			constructor: function(params) {
				Marionette.Controller.prototype.constructor.apply(this, arguments);
				mainRegion = params.mainRegion;
			},

			showHomePage: function() {
				this._show(RoutingPaths.HOME);
			},

			showTVShows: function() {
				this._show(RoutingPaths.TVSHOWS);
			},

			showMovies: function(category) {
				tabParams = [category];
				this._show(RoutingPaths.MOVIES);
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

			_show: function(tabToSelect) {
				if (homeView != null) {
					if (!homeView.isAdmin() && tabToSelect === RoutingPaths.ADMIN) {
						tabToSelect = RoutingPaths.HOME;
					}
					var route = StringUtils.formatRoute(tabToSelect, tabParams);
					homeView.changeTab(route, tabToView[tabToSelect]);
					Backbone.history.navigate(route, {trigger: false});
					document.title = tabToTitle[tabToSelect];
				} else {
//					var tab = tabToSelect.substring(tabToSelect.lastIndexOf("/") + 1);
					HttpUtils.get("rest/user/pre-login", function(res) {
						if (!res.isLoggedIn) {
							var url = window.parent.location.href;
							url = url.substring(0, url.lastIndexOf('/'));
							window.parent.location = url;
							return;
						}

						if (!res.isAdmin) {
							tabToSelect = RoutingPaths.HOME;
						}
						var route = StringUtils.formatRoute(tabToSelect, tabParams);
						Backbone.history.navigate(route, {trigger: false});
						document.title = tabToTitle[tabToSelect];

						homeView = new HomeView({
							selectedTab: route,
							contentViewDef: tabToView[tabToSelect],
							tabData: res
						});
						mainRegion.show(homeView);
					}, false);
				}
			}
		});
	});
