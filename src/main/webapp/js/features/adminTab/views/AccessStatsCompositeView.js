/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/access-stats-list.tpl',
	'features/adminTab/views/AccessStatsItemView'
],
	function(Marionette, Handlebars, template, AccessStatsItemView) {
		"use strict";

		return Marionette.CompositeView.extend({
			template: Handlebars.compile(template),
			className: 'access-stats-list-container',
			itemView: AccessStatsItemView,
			itemViewContainer: '.access-stats-list',

			ui: {
				totalUsersCounter: '.total-users-counter'
			},

			constructor: function(options) {
				Marionette.CompositeView.prototype.constructor.apply(this, arguments);

				this.collection.on('reset add remove', this.onCollectionSizeChanged, this);
			},

			onCollectionSizeChanged: function() {
				this.ui.totalUsersCounter.html(this.collection.size());
			}
		});
	});
