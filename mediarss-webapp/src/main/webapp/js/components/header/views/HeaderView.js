/*global define*/
define([
	'marionette',
	'handlebars',
        'text!components/header/templates/header.tpl',
        'components/news/views/NewsView'
],
    function (Marionette, Handlebars, template, NewsView) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'header-container',

			regions: {
                descriptionRegion: '.header-description',
                newsRegion: '.header-news'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.descriptionView = options.descriptionView;
                this.newsView = new NewsView();
			},

			onRender: function() {
				this.descriptionRegion.show(this.descriptionView);
                this.newsRegion.show(this.newsView);
            },

            setNews: function (news) {
                this.newsView.setNews(news);
            }
		});
	});
