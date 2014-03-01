define([
	'marionette',
	'handlebars',
	'text!features/showsTab/templates/active-search.tpl',
	'moment'
],
	function(Marionette, Handlebars, template, Moment) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
//			className: 'tracked-show',

			ui: {
				runningStatus: '.active-search-searching',
				status: '.active-search-status',
				viewLink: '.active-search-view',
				noResultsStatus: '.active-search-no-results'
			},

			events: {
				'click .active-search-remove': 'onActiveSearchRemove',
				'click .active-search-view': 'onActiveSearchView'
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
				this.vent = options.vent;
			},

			onRender: function() {
				var results = this.model.get('episodes').length;
				if (this.model.get('end')) {
					this.ui.runningStatus.hide();
					if (results === 0) {
						this.ui.noResultsStatus.show();
						this.ui.status.hide();
					} else {
						this.ui.noResultsStatus.hide();
						this.ui.status.show();
						var resultsPart;
						if (results === 1) {
							resultsPart = '1 result';
						} else {
							resultsPart = results + ' results';
						}
						this.ui.status.html(resultsPart + ', completed in ' + Moment.duration(this.model.get('end') - this.model.get('start')).humanize());
					}
				} else {
					// still running
					this.ui.runningStatus.show();
					this.ui.status.hide();
					this.ui.noResultsStatus.hide();
				}

				if (results === 0) {
					this.ui.viewLink.hide();
				} else {
					this.ui.viewLink.show();
				}
			},

			onActiveSearchRemove: function() {
				this.vent.trigger('active-search-remove', this.model);
			},

			onActiveSearchView: function() {
				this.vent.trigger('active-search-view', this.model);
			}
		});
	});

