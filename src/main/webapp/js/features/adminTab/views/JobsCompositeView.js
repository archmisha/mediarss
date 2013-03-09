/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/admin-jobs.tpl',
	'features/adminTab/views/JobItemView'
],
	function(Marionette, Handlebars, template, JobItemView) {
		"use strict";

		return Marionette.CompositeView.extend({
			template: Handlebars.compile(template),
			className: 'jobs',
			itemViewContainer: '.jobs-list',
			itemView: JobItemView
		});
	});
