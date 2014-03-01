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
				collapsibleContent: '.section-collapsible',
				counter: '.tracked-shows-counter'
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
				this.getCounter = options.getCounter;
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
				this.updateCounter();
				this.setCollapsible(this.collapsible);
			},

			setCollapsible: function(collapsible) {
				this.collapsible = collapsible;
				if (this.collapsible) {
					this.ui.collapseIconsContainer.show();
					this.ui.collapsibleContent.hide();

					if (this.expanded) {
						this.expand();
					} else {
						this.collapse();
					}
				} else {
					this.ui.collapseIconsContainer.hide();
					this.ui.collapsibleContent.show();
				}
			},

			collapse: function() {
				this.$el.removeClass('expanded');
				this.$el.addClass('collapsed');
				this.expanded = false;
			},

			expand: function() {
				this.$el.addClass('expanded');
				this.$el.removeClass('collapsed');
				this.expanded = true;
			},

			onTriggerCollapse: function() {
				if (!this.collapsible) {
					return;
				}

				var that = this;
				if (this.expanded) {
					this.ui.collapsibleContent.slideUp('slow', function() {
						that.collapse();
					});
				} else {
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
			},

			updateCounter: function() {
				if (this.getCounter) { // optional
					this.ui.counter.html('(' + this.getCounter() + ')');
				}
			}
		});
	});
