define([
	'marionette',
	'handlebars',
	'text!components/search-result/templates/search-results-empty-view.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'search-results-empty-view'
		});
	});