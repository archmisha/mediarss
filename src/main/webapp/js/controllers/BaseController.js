/*global define*/

define([
	'marionette',
	'StringUtils',
	'routers/RoutingPaths',
	'features/login/views/LoginView',
	'features/register/views/RegisterView',
	'features/home/views/HomeView',
	'features/homeTab/views/HomeTabView',
	'features/showsTab/views/ShowsTabView',
	'features/moviesTab/views/MoviesTabView',
	'features/adminTab/views/AdminTabView',
	'HttpUtils',
	'MessageBox'
],
	function(Marionette, StringUtils, RoutingPaths, LoginView, RegisterView, HomeView, HomeTabView, ShowsTabView, MoviesTabView, AdminTabView, HttpUtils, MessageBox) {
		"use strict";

		var BASE_TITLE = 'Personalized Media RSS';
		var mainRegion = null;

		var tabParams = [];
		var tabToSelect = null;
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

		var isLoggedIn = false;
		var homeView = null;

		return Marionette.Controller.extend({

			constructor: function(params) {
				Marionette.Controller.prototype.constructor.apply(this, arguments);
				mainRegion = params.mainRegion;
			},

			showHomePage: function() {
				tabToSelect = RoutingPaths.HOME;
				this._preLoginWrapper();
			},

			showTVShows: function() {
				tabToSelect = RoutingPaths.TVSHOWS;
				this._preLoginWrapper();
			},

			showMovies: function(category) {
				tabToSelect = RoutingPaths.MOVIES;
				tabParams = [category];
				this._preLoginWrapper();
			},

			showAdmin: function() {
				tabToSelect = RoutingPaths.ADMIN;
				this._preLoginWrapper();
			},

			showRoot: function() {
				tabToSelect = RoutingPaths.HOME;
				this._preLoginWrapper();
			},

			logout: function() {
				isLoggedIn = false;

				// it is host:port/#logout here
				var url = window.parent.location.href;
				url = url.substring(0, url.lastIndexOf('/') + 1);
				window.parent.location = url + "rest/user/logout";
			},

			_preLoginWrapper: function() {
				if (isLoggedIn) {
					this._showHome(null);
					return;
				}

				var that = this;
				var tab = tabToSelect.substring(tabToSelect.lastIndexOf("/") + 1);
				HttpUtils.get("rest/user/pre-login/" + tab, function(res) {
					if (res.isLoggedIn) {
						that._showHome(res);
					} else {
						that._showLogin(tab);
					}
				}, false);
			},

			_showHome: function(tabData) {
				isLoggedIn = true;

				if (tabToSelect == null) {
					tabToSelect = RoutingPaths.HOME;
				}

				Backbone.history.navigate(StringUtils.formatRoute(tabToSelect, tabParams), {trigger: false});
				document.title = tabToTitle[tabToSelect];
				if (homeView != null) {
					homeView.changeTab(tabToSelect, tabToView[tabToSelect]);
				} else {
					homeView = new HomeView({
						selectedTab: tabToSelect,
						contentViewDef: tabToView[tabToSelect],
						tabData: tabData
					});
					mainRegion.show(homeView);
				}
				tabToSelect = null;
			},

			_showLogin: function(tab) {
				var that = this;
				var login = new LoginView();
				login.on('login', function(options) {
					HttpUtils.post("rest/user/login", {
						username: options.username,
						password: options.password,
						tab: tab
					}, function(res) {
						if (res.success === undefined) {
							that._showHome(res);
						} else {
							login.showStatusMessage(res.message);
						}
					}, false);
				});
				login.on('forgot-password', function(options) {
					HttpUtils.post("rest/user/forgot-password", {
						email: options.email
					}, function(res) {
						login.showStatusMessage(res.message);
					}, false);
				});
				mainRegion.show(login);
			},

			register: function() {
				mainRegion.show(new RegisterView());
			}
		});
	});
