/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/admin-tab.tpl',
	'features/adminTab/views/JobsCompositeView',
	'features/adminTab/collections/JobsCollection',
	'components/section/views/SectionView',
	'features/adminTab/views/NotificationsView'
],
	function(Marionette, Handlebars, template, JobsCompositeView, JobsCollection, SectionView, NotificationsView) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'admin-tab',

			regions: {
				jobsSectionRegion: '.admin-jobs-section',
				jobsRegion: '.admin-jobs',
				notificationsSectionRegion: '.admin-notifications-section',
				notificationsRegion: '.admin-notifications'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.loggedInUserData = options.loggedInUserData;
				this.initialData = options.initialData;

				this.jobs = new JobsCollection();
				this.jobsView = new JobsCompositeView({collection: this.jobs});
				this.jobs.fetch();

				this.jobsSection = new SectionView({
					title: 'Jobs',
					description: ''
				});

				this.notificationsSection = new SectionView({
					title: 'Notifications',
					description: 'Send a mass notification to all users'
				});

				this.notificationsView = new NotificationsView();
			},

			onRender: function() {
				this.jobsSectionRegion.show(this.jobsSection);
				this.jobsRegion.show(this.jobsView);
				this.notificationsSectionRegion.show(this.notificationsSection);
				this.notificationsRegion.show(this.notificationsView);
			}
		});
	});
