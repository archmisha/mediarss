/*global define*/
define([
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
	'utils/StringUtils'
],
	function(Marionette, Handlebars, template, HeaderView, HomeHeaderTabs, MastheadView, HttpUtils, RoutingPaths, HomeTabView, ShowsTabView, MoviesTabView, AdminTabView, SettingsTabView, StringUtils) {
		"use strict";

		var BASE_TITLE = 'Personalized Media RSS';

		var tabs = {};
		tabs[RoutingPaths.HOME] = {
			title: BASE_TITLE + ' - Home',
			view: new HomeTabView()
		};
		tabs[RoutingPaths.TVSHOWS] = {
			title: BASE_TITLE + ' - TV Shows',
			view: new ShowsTabView()
		};
		tabs[RoutingPaths.MOVIES] = {
			title: BASE_TITLE + ' - Movies',
			view: new MoviesTabView()
		};
		tabs[RoutingPaths.SETTINGS] = {
			title: BASE_TITLE + ' - Settings',
			view: new SettingsTabView()
		};
		tabs[RoutingPaths.ADMIN] = {
			title: BASE_TITLE + ' - Admin',
			view: new AdminTabView()
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
					that.tabData = res;
					that.mastheadView.setTabData(that.tabData);
					that.homeHeaderTabs.setAdmin(that.tabData.isAdmin);

					if (!that.tabData.isLoggedIn) {
						var url = window.parent.location.href;
						url = url.substring(0, url.lastIndexOf('/'));
						window.parent.location = url;
						return;
					}

					that.mastheadRegion.show(that.mastheadView);
					that.headerRegion.show(that.headerView);

					if (that.changeTabData) {
						that.changeTab(that.changeTabData.tabToSelect, that.changeTabData.tabParams);
						that.changeTabData = null;
					}
				});
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

				this.contentRegion.show(tabs[tabToSelect].view);
				this.homeHeaderTabs.selectTab(tabToSelect);
			}
		});
	});
