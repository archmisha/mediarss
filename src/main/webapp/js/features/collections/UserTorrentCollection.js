define([
	'backbone',
	'features/models/UserTorrent'
],
	function(Backbone, UserTorrent) {
		'use strict';

		return Backbone.Collection.extend({
			model: UserTorrent
		});
	});

