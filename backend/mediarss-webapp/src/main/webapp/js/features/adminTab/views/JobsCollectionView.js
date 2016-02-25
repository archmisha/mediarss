/*global define*/
define([
	'marionette',
	'handlebars',
	'features/adminTab/views/JobItemView',
	'features/adminTab/views/JobsCollectionLoadingView'
],
	function(Marionette, Handlebars, JobItemView, JobsCollectionLoadingView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: JobItemView,
			className: 'jobs-list',
			emptyView: JobsCollectionLoadingView
		});
	});
