/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/home/templates/home-header-tabs.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'home-header-tabs',

			ui: {
				adminTab: '.home-admin-tab'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.selectedTab = options.selectedTab;
				this.isAdmin = options.isAdmin;
			},

			onRender: function() {
				var selector = "a[href$='" + this.selectedTab + "']";
				this.$el.find(selector).addClass('home-selected-tab');

				if (this.isAdmin) {
					this.ui.adminTab.show();
				}
			},

			selectTab: function(tabToSelect) {
				this.$el.find('a').removeClass('home-selected-tab');
				var selector = "a[href$='" + tabToSelect + "']";
				this.$el.find(selector).addClass('home-selected-tab');
			}
		});
	});
