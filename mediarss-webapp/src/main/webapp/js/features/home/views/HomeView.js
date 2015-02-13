/*global define*/
define([
	'backbone',
	'marionette',
	'handlebars',
	'text!features/home/templates/home.tpl',
	'components/header/views/HeaderView',
	'features/home/views/HomeHeaderTabs',
	'features/home/views/MastheadView',
	'utils/HttpUtils',
	'routers/RoutingPaths',
	'features/homeTab/views/HomeTabView',
	'features/showsTab/views/ShowsTabView',
	'features/moviesTab/views/MoviesTabView',
	'features/adminTab/views/AdminTabView',
	'features/settingsTab/views/SettingsTabView',
	'utils/StringUtils',
	'utils/Utils',
	'features/moviesTab/views/MoviePreviewView'
],
	function(Backbone, Marionette, Handlebars, template, HeaderView, HomeHeaderTabs, MastheadView, HttpUtils, RoutingPaths, HomeTabView, ShowsTabView, MoviesTabView, AdminTabView, SettingsTabView, StringUtils, Utils, MoviePreviewView) {
		"use strict";

		var BASE_TITLE = 'Personalized Media RSS';

		var tabs = {};
		tabs[RoutingPaths.HOME] = {
			title: BASE_TITLE + ' - Home',
			view: HomeTabView
		};
		tabs[RoutingPaths.TVSHOWS] = {
			title: BASE_TITLE + ' - TV Shows',
			view: ShowsTabView
		};
		tabs[RoutingPaths.MOVIES] = {
			title: BASE_TITLE + ' - Movies',
			view: MoviesTabView
		};
		tabs[RoutingPaths.SETTINGS] = {
			title: BASE_TITLE + ' - Settings',
			view: SettingsTabView
		};
		tabs[RoutingPaths.ADMIN] = {
			title: BASE_TITLE + ' - Admin',
			view: AdminTabView
		};
		tabs[RoutingPaths.MOVIE_PREVIEW] = {
			title: BASE_TITLE + ' - Movie preview',
			view: MoviePreviewView
		};

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'home',

			regions: {
				mastheadRegion: '.home-masthead',
				headerRegion: '.home-header',
				contentRegion: '.home-content'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);

				this.mastheadView = new MastheadView();
				this.homeHeaderTabs = new HomeHeaderTabs();
				this.headerView = new HeaderView({
					descriptionView: this.homeHeaderTabs
				});
			},

			onRender: function() {
				var that = this;
				HttpUtils.get("rest/user/pre-login", function(res) {
					if (!res.isLoggedIn) {
						window.parent.location = Utils.getBaseUrl();
						return;
					}

					that.tabData = res;
					that.mastheadView.setTabData(that.tabData);
					that.homeHeaderTabs.setAdmin(that.tabData.isAdmin);
					that.mastheadRegion.show(that.mastheadView);
					that.headerRegion.show(that.headerView);

					if (that.changeTabData) {
						var tabToSelect = that.changeTabData.tabToSelect;
						var tabParams = that.changeTabData.tabParams;
						that.changeTabData = null;
						that.changeTab(tabToSelect, tabParams);
					}
				}, false); // don't show loading circle
			},

			changeTab: function(tabToSelect, tabParams) {
				// tabData not arrived yet
				if (!this.tabData) {
					this.changeTabData = {
						tabToSelect: tabToSelect,
						tabParams: tabParams
					};
					return;
				}

				if (!this.tabData.isAdmin && tabToSelect === RoutingPaths.ADMIN) {
					tabToSelect = RoutingPaths.HOME;
				}

				var route = StringUtils.formatRoute(tabToSelect, tabParams);
				Backbone.history.navigate(route, {trigger: false});
				document.title = tabs[tabToSelect].title;

				var tabView = new tabs[tabToSelect].view({
					tabData: this.tabData,
					tabParams: tabParams
				});
				this.contentRegion.show(tabView);
				this.homeHeaderTabs.selectTab(tabToSelect);
			}
		});
	});
