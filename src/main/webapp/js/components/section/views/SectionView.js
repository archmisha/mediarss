/*global define*/
define([
	'marionette',
	'handlebars',
	'text!components/section/templates/section.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'section',

			ui: {
				collapseIconsContainer: '.section-collapse-icons',
				sectionHeader: '.section-header',
				collapsedIcon: '.section-collapsed-icon',
				expandedIcon: '.section-expanded-icon',
				collapsibleContent: '.section-collapsible'
			},

			events: {
				'click .section-header': 'onTriggerCollapse'
			},

			regions: {
				descriptionRegion: '.section-description',
				contentRegion: '.section-content'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.title = options.title;
				this.description = options.description;
				this.contentView = options.content;
				this.collapsible = options.collapsible;
				this.expanded = !options.collapsed;
				if (options.descriptionViewDef) {
					this.descriptionView = new options.descriptionViewDef(options);
				}
			},

			onRender: function() {
				if (this.descriptionView) {
					this.descriptionRegion.show(this.descriptionView);
				}
				if (this.contentView) {
					this.contentRegion.show(this.contentView);
				}
			},

			onShow: function() {
				var that = this;
				setTimeout(function() {
					that.collapsible = that.collapsible && (that.ui.collapseIconsContainer.css('display') !== 'none'); //that.ui.collapseIconsContainer.is(':visible');

					if (that.collapsible) {
						that.ui.sectionHeader.addClass('section-collapsible');

						if (that.expanded) {
							that.expand();
						} else {
							that.collapse();
						}
					} else {
						that.ui.collapseIconsContainer.hide();
						that.ui.collapsibleContent.show();
					}
				}, 50);
			},

			collapse: function() {
				this.ui.expandedIcon.hide();
				this.ui.collapsedIcon.show();
				this.ui.collapsibleContent.hide();
				this.expanded = false;
			},

			expand: function() {
				this.ui.expandedIcon.show();
				this.ui.collapsedIcon.hide();
				this.ui.collapsibleContent.show();
				this.expanded = true;
			},

			onTriggerCollapse: function() {
				if (!this.collapsible) {
					return;
				}

				var that = this;
				if (this.expanded) {
					// here change the icons only after the animation
					this.ui.collapsibleContent.slideUp('slow', function() {
						that.collapse();
					});
				} else {
					// change the icons before the animation
					this.ui.expandedIcon.show();
					this.ui.collapsedIcon.hide();
					this.ui.collapsibleContent.slideDown('slow', function() {
						that.expand();
					});
				}
			},

			templateHelpers: function() {
				return {
					'title': this.title,
					'description': this.description
				};
			}
		});
	});
