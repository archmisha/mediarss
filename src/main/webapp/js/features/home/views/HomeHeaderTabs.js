/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/home/templates/home-header-tabs.tpl',
	'utils/Utils'
],
	function(Marionette, Handlebars, template, Utils) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'home-header-tabs',

			ui: {
				adminTab: '.home-admin-tab'
			},

			events: {
				'click .home-home-tab': '_homeTabClick',
				'click .home-tvshows-tab': '_showsTabClick',
				'click .home-movies-tab': '_moviesTabClick',
				'click .home-settings-tab': '_settingsTabClick',
				'click .home-admin-tab': '_adminTabClick'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
			},

			setAdmin: function(isAdmin) {
				this.isAdmin = isAdmin;
			},

			onRender: function() {
//				this._getTabEl(this.selectedTab).addClass('home-selected-tab');

				if (this.isAdmin) {
					this.ui.adminTab.show();
				}
			},

			selectTab: function(tabToSelect) {
				this.$el.find('span').removeClass('home-selected-tab');
				this._getTabEl(tabToSelect).addClass('home-selected-tab');
			},

			_getTabEl: function(tab) {
				var ind = tab.indexOf('/');
				if (ind > -1) {
					tab = tab.substring(0, ind);
				}
				var selector = "span.home-" + tab + "-tab";
				return this.$el.find(selector);
			},

			_homeTabClick: function() {
				this._tabNavHelper("home");
			},

			_showsTabClick: function() {
				this._tabNavHelper("tvshows");
			},

			_moviesTabClick: function() {
				this._tabNavHelper("movies/availableMovies");
			},

			_settingsTabClick: function() {
				this._tabNavHelper("settings");
			},

			_adminTabClick: function() {
				this._tabNavHelper("admin");
			},

			_tabNavHelper: function(nav) {
				window.parent.location = Utils.getBaseRouteUrl() + nav;
			}
		});
	});
