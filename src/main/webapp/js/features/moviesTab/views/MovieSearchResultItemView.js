define([
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movie-search-result-item.tpl',
	'qtip'
],
	function(Marionette, Handlebars, template, qtip) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'movie-search-result-item',

			ui: {
				addButton: '.movies-search-result-item-add',
				addedStatus: '.movies-search-result-item-added'
			},

			events: {
				'click .movies-search-result-item-add': 'onMovieSearchResultItemAddClick'
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.ItemView.prototype.constructor.apply(this, arguments);

				var that = this;
				this.model.on('change:added', function() {
					that.setStatus();
				});
			},

			setStatus: function() {
				if (this.model.get('added')) {
					this.ui.addButton.hide();
					this.ui.addedStatus.show();
				} else {
					this.ui.addButton.show();
					this.ui.addedStatus.hide();
				}
			},

			onRender: function() {
				this.setStatus();
			},

			onMovieSearchResultItemAddClick: function() {
				this.vent.trigger('movie-search-result-item-add', this.model);
			}
		});
	});

