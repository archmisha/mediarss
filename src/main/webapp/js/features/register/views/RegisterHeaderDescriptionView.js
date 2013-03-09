/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/register/templates/register-header-description.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'register-header-description'
		});
	});
