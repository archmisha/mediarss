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
				this.loggedInUserData = options.loggedInUserData;
				this.initialData = options.initialData;
				this.contentViewDef = options.contentViewDef;

				this.mastheadView = new MastheadView({
					user: this.loggedInUserData.user,
					initialData: this.initialData
				});

				this.headerView = new HeaderView({
					descriptionViewDef: HomeHeaderTabs,
					descriptionViewOptions: {
						selectedTab: this.selectedTab,
						isAdmin: this.loggedInUserData.user.admin
					}
				});
				this.contentView = new this.contentViewDef({
					loggedInUserData: this.loggedInUserData,
					initialData: this.initialData
				});
			},

			onRender: function() {
				this.mastheadRegion.show(this.mastheadView);
				this.headerRegion.show(this.headerView);
				this.contentRegion.show(this.contentView);
			}
		});
	});
