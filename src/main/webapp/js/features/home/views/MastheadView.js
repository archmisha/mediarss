/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/home/templates/masthead.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'masthead',

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.user = options.user;
				this.initialData = options.initialData;
			},

			templateHelpers: function() {
				return {
					'username': this.user.firstName,
					'updated-on': this.initialData.deploymentDate
				};
			}
		});
	});
