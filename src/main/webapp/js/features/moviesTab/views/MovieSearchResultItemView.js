define([
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movie-search-result-item.tpl',
	'utils/Utils'
],
	function(Marionette, Handlebars, template, Utils) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'movie-search-result-item',

			ui: {
				movieNameWrapper: '.movies-search-result-title-wrapper'
			},

			events: {
				'click .movies-search-result-item-add': 'onMovieSearchResultItemAddClick'
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.ItemView.prototype.constructor.apply(this, arguments);

				var that = this;
				this.model.on('change:added', function() {
					that.render();
				});
			},

			onRender: function() {
				Utils.addTooltip([this.ui.movieNameWrapper]);
			},

			onMovieSearchResultItemAddClick: function() {
				this.vent.trigger('movie-search-result-item-add', this.model);
			},

			templateHelpers: function() {
				return {
					'escapedTitle': Utils.fixForTooltip(this.model.get('name') + ' (' + this.model.get('year') + ')')
				};
			}
		});
	});

