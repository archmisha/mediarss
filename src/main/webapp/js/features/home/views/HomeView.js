/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/home/templates/home.tpl',
	'components/header/views/HeaderView',
	'features/home/views/HomeHeaderTabs',
	'features/home/views/MastheadView'
],
	function(Marionette, Handlebars, template, HeaderView, HomeHeaderTabs, MastheadView) {
		"use strict";

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
				this.selectedTab = options.selectedTab;
				this.contentViewDef = options.contentViewDef;
				this.tabData = options.tabData;

				this.mastheadView = new MastheadView({
					tabData: this.tabData
				});

				this.homeHeaderTabs = new HomeHeaderTabs({
					selectedTab: this.selectedTab,
					isAdmin: this.tabData.isAdmin
				});

				this.headerView = new HeaderView({
					descriptionView: this.homeHeaderTabs
				});
				this.contentView = new this.contentViewDef();
			},

			onRender: function() {
				this.mastheadRegion.show(this.mastheadView);
				this.headerRegion.show(this.headerView);
				this.contentRegion.show(this.contentView);
			},

			changeTab: function(tabToSelect, contentViewDef) {
				this.contentView = new contentViewDef();
				this.contentRegion.show(this.contentView);
				this.homeHeaderTabs.selectTab(tabToSelect);
			},

			isAdmin: function() {
				return this.tabData.isAdmin;
			}
		});
	});
