/*global define*/
define([
	'marionette',
	'handlebars',
	'features/adminTab/views/JobItemView'
],
	function(Marionette, Handlebars, JobItemView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: JobItemView,
			className: 'jobs-list'
		});
	});
