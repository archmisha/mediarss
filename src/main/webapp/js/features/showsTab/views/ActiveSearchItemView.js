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
				status: '.active-search-status',
				viewLink: '.active-search-view'
			},

			events: {
				'click .active-search-remove': 'onActiveSearchRemove',
				'click .active-search-view': 'onActiveSearchView'
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
				this.vent = options.vent;
			},

//			onTrackedShowRemove: function() {
//				this.vent.trigger('tracked-show-remove', this.model);
//			},

			onRender: function() {
				var results = this.model.get('episodes').length;
				if (this.model.get('end')) {
					this.ui.status.html('took ' + Moment.duration(this.model.get('end') - this.model.get('start')).humanize() + ' ' + results + ' results');
				} else {
					this.ui.status.html('running');
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

