define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/jobs-list-loading.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'jobs-list-empty-label-container'
		});
	});