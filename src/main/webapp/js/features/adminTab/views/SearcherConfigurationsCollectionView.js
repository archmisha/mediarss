define([
	'marionette',
	'handlebars',
	'HttpUtils',
	'MessageBox',
	'features/adminTab/views/SearcherConfigurationItemView'
],
	function(Marionette, Handlebars, HttpUtils, MessageBox, SearcherConfigurationItemView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: SearcherConfigurationItemView,
			className: 'admin-searcher-conf-list',

			constructor: function(options) {
				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
			}
		});
	});

