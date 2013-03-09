/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/login/templates/login-header-description.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'login-header-description'
		});
	});
