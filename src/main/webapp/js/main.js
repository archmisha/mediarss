require.config({
	paths: {
		underscore: 'lib/underscore',
		backbone: 'lib/backbone',
		marionette: 'lib/backbone.marionette.min',
		jquery: 'lib/jquery-1.8.2.min',
		text: 'lib/text',
		handlebars: 'lib/handlebars-1.0.rc.1.min',
		less: 'lib/less',
		moment: 'lib/moment-2.0.0.min',
		zeroClipboard: 'lib/ZeroClipboard.min',
		fancybox: 'lib/jquery.fancybox.pack',
		jqplugin: 'lib/jquery.jqplugin.1.0.2.min',
		chosen: 'lib/chosen.jquery.min',
		blockUI: 'lib/jquery.blockUI',
		noty: 'lib/noty/jquery.noty',
		qtip: 'lib/jquery.qtip-1.0.0-rc3.min',
		ajaxChosen: 'lib/ajax-chosen.min',
		select2: 'lib/select2.min'
	},
	shim: {
		'lib/backbone-localStorage': ['backbone'],
		underscore: {
			exports: '_'
		},
		backbone: {
			exports: 'Backbone',
			deps: ['jquery', 'underscore']
		},
		marionette: {
			exports: 'Backbone.Marionette',
			deps: ['backbone']
		},
		handlebars: {
			exports: 'Handlebars'
		},
		less: {
			exports: 'Less'
		},
		zeroClipboard: {
			exports: 'ZeroClipboard'
		},
		fancybox: {
			exports: 'Fancybox'
		},
		jqplugin: {
			exports: 'jqPlugin'
		},
		chosen: {
			exports: 'Chosen'
		},
		blockUI: {
			exports: 'BlockUI'
		},
		noty: {
			exports: 'Noty'
		},
		qtip: {
			exports: 'qTip',
			deps: ['jquery']
		},
		ajaxChosen: {
			exports: 'AjaxChosen'
		},
		select2: {
			exports: 'Select2'
		}
	},
	deps: ['jquery', 'underscore']
});

require([
	'jquery',
	'backbone',
	'handlebars',
	'routers/MainRouter',
	'controllers/BaseController',
	'moment',
	'qtip'
],
	function($, Backbone, Handlebars, MainRouter, BaseController, Moment, qtip) {
		"use strict";

		//  format an ISO date using Moment.js
		//  http://momentjs.com/
		//  moment syntax example: moment(Date("2011-07-18T15:50:52")).format("MMMM YYYY")
		//  usage: {{dateFormat creation_date format="MMMM YYYY"}}
		Handlebars.registerHelper('dateFormat', function(context, block) {
			if (context == null) {
				return block.hash.default || 'never';
			}
			var f = block.hash.format || "MMM Do, YYYY";
//			console.log('dateFormat:  context=' + new Date(context) + ' ' + Moment(new Date(context)).format(f));
			return Moment(new Date(context)).format(f);
		});

		Handlebars.registerHelper('isToday', function(date, options) {
			if (new Date(date).toDateString() == (new Date()).toDateString()) {
				return options.fn(date);
			} else {
				return options.inverse(date);
			}
		});

		var app = new Backbone.Marionette.Application();

		app.addRegions({
			mainRegion: "#main"
		});

		app.addInitializer(function() {
			var controller = new BaseController({mainRegion: app.mainRegion});
			new MainRouter({
				controller: controller
			});
			Backbone.history.start();

			$.fn.qtip.styles.rssStyle = {
				name: 'blue',
				color: '#097fe3',
				fontSize: 13,
				border: {
					width: 1,
					radius: 4
				}
			};
		});

		app.start();
	});