require.config({
	paths: {
		underscore: 'lib/underscore-1.6.0-min',
		backbone: 'lib/backbone-1.1.2.min',
		marionette: 'lib/backbone.marionette.min-1.6.4',
		jquery: 'lib/jquery-1.11.0.min',
		text: 'lib/text',
		handlebars: 'lib/handlebars-v1.3.0',
		less: 'lib/less',
		moment: 'lib/moment-2.0.0.min',
		zeroClipboard: 'lib/ZeroClipboard-1.3.1.min',
		'jquery.fancybox': 'lib/jquery.fancybox.pack',
		chosen: 'lib/chosen.jquery.min',
		blockUI: 'lib/jquery.blockUI',
		noty: 'lib/noty/jquery.noty',
		qtip: 'lib/jquery.qtip-2.2.0.min',
		ajaxChosen: 'lib/ajax-chosen.min',
		select2: 'lib/select2.min',
		'jquery.MsgBox': 'lib/jquery.msgBox'
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
		'jquery.fancybox': ['jquery'],
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
		},
		'jquery.MsgBox': ['jquery']
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
	'qtip',
	'zeroClipboard',
],
	function($, Backbone, Handlebars, MainRouter, BaseController, Moment, qtip, ZeroClipboard) {
		"use strict";

		// jquery doesn't support $.browser from 1.9 and some plugins still use it ...
		$.browser = {};
		(function() {
			$.browser.msie = false;
			$.browser.version = 0;
			if (navigator.userAgent.match(/MSIE ([0-9]+)\./)) {
				$.browser.msie = true;
				$.browser.version = RegExp.$1;
			}
		})();

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
		Handlebars.registerHelper('ifeq', function(conditional, options) {
			if (options.hash.value === conditional) {
				return options.fn(this)
			} else {
				return options.inverse(this);
			}
		});
		Handlebars.registerHelper('ifneq', function(conditional, options) {
			if (options.hash.value !== conditional) {
				return options.fn(this)
			} else {
				return options.inverse(this);
			}
		});
		Handlebars.registerHelper('ifundef', function(conditional, options) {
			if (!conditional) {
				return options.fn(this)
			} else {
				return options.inverse(this);
			}
		});

		Handlebars.registerHelper('isToday', function(date, options) {
			if (new Date(date).toDateString() == (new Date()).toDateString()) {
				return options.fn(date);
			} else {
				return options.inverse(date);
			}
		});

		ZeroClipboard.config({swfPath: "ZeroClipboard.swf"});

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

			// preload error dialog images
			$(['images/jqueryMessageBox/error.jpg',
				'images/jqueryMessageBox/msgBoxBackGround.png']).each(function() {
				$('<img />').attr('src', this).appendTo('body').css('display', 'none');
			});
		});

		app.start();
	});