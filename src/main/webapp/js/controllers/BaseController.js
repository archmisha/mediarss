/*global define*/

define([
	'marionette',
	'StringUtils',
	'routers/RoutingPaths',
	'features/login/views/LoginView',
	'features/register/views/RegisterView',
	'features/home/views/HomeView',
	'features/homeTab/views/HomeTabView',
	'features/tvShowsTab/views/TVShowsTabView',
	'features/moviesTab/views/MoviesTabView',
	'features/adminTab/views/AdminTabView'
],
	function(Marionette, StringUtils, RoutingPaths, LoginView, RegisterView, HomeView, HomeTabView, TVShowsTabView, MoviesTabView, AdminTabView) {
		"use strict";

		var BASE_TITLE = 'Personalized Media RSS';
		var mainRegion = null;

		var tabToSelect = null;
		var tabToView = {};
		tabToView[RoutingPaths.HOME] = HomeTabView;
		tabToView[RoutingPaths.TVSHOWS] = TVShowsTabView;
		tabToView[RoutingPaths.MOVIES] = MoviesTabView;
		tabToView[RoutingPaths.ADMIN] = AdminTabView;

		var tabToTitle = {};
		tabToTitle[RoutingPaths.HOME] = BASE_TITLE + ' - Home';
		tabToTitle[RoutingPaths.TVSHOWS] = BASE_TITLE + ' - TV Shows';
		tabToTitle[RoutingPaths.MOVIES] = BASE_TITLE + ' - Movies';
		tabToTitle[RoutingPaths.ADMIN] = BASE_TITLE + ' - Admin';

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

			showMovies: function() {
				tabToSelect = RoutingPaths.MOVIES;
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
				// it is host:port/#logout here
				var url = window.parent.location.href;
				url = url.substring(0, url.lastIndexOf('/') + 1);
				window.parent.location = url + "rest/user/logout";
			},

			_preLoginWrapper: function() {
				var that = this;
				var tab = tabToSelect.substring(tabToSelect.lastIndexOf("/") + 1);
				$.get("rest/user/pre-login/" + tab).success(function(res) {
					if (res.user && res.user != null) {
						that._showHome(res.initialData, res.user);
					} else {
						that._showLogin(res.initialData, tab);
					}
				}).error(function(res) {
						console.log('error. data: ' + res);
					});
			},

			_showHome: function(initialData, loggedInUserData) {
				if (tabToSelect == null) {
					tabToSelect = RoutingPaths.HOME;
				}
				if (tabToSelect == RoutingPaths.ADMIN && !loggedInUserData.user.admin) {
					tabToSelect = RoutingPaths.HOME;
				}

				Backbone.history.navigate(StringUtils.formatRoute(tabToSelect), {trigger: false});
				document.title = tabToTitle[tabToSelect];
				mainRegion.show(new HomeView({
					selectedTab: tabToSelect,
					contentViewDef: tabToView[tabToSelect],
					loggedInUserData: loggedInUserData,
					initialData: initialData
				}));
				tabToSelect = null;
			},

			_showLogin: function(initialData, tab) {
				var that = this;
				var login = new LoginView();
				login.on('login', function(options) {
					$.post("rest/user/login", {
						username: options.username,
						password: options.password,
						includeInitialData: initialData == undefined,
						tab: tab
					}).success(function(res) {
							if (res.success === undefined) {
								if (initialData == undefined) {
									initialData = res.initialData;
								}
								that._showHome(initialData, res.user /*loggedInUserData*/);
							} else {
								login.showStatusMessage(res.message);
							}
						}).error(function(res) {
							console.log('error. data: ' + res);
						});
				});
				login.on('forgot-password', function(options) {
					$.post("rest/user/forgot-password", {
						email: options.email
					}).success(function(res) {
							login.showStatusMessage(res.message);
						}).error(function(res) {
							console.log('error. data: ' + res);
						});
				});
				mainRegion.show(login);
			},

			register: function() {
				mainRegion.show(new RegisterView());
			}
		});
	});
