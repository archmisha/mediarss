/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/admin-tab.tpl',
	'features/adminTab/views/JobsCollectionView',
	'features/adminTab/collections/JobsCollection',
	'components/section/views/SectionView',
	'features/adminTab/views/NotificationsView',
	'features/adminTab/collections/UsersCollection',
	'features/adminTab/views/AccessStatsCompositeView'
],
	function(Marionette, Handlebars, template, JobsCollectionView, JobsCollection, SectionView, NotificationsView, UsersCollection, AccessStatsCompositeView) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'admin-tab',

			regions: {
				jobsSectionRegion: '.admin-jobs-section',
				jobsRegion: '.admin-jobs',
				accessStatsSectionRegion: '.admin-access-stats-section',
				accessStatsRegion: '.admin-access-stats',
				notificationsSectionRegion: '.admin-notifications-section',
				notificationsRegion: '.admin-notifications'
			},

			constructor: function(options) {
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.loggedInUserData = options.loggedInUserData;
				this.initialData = options.initialData;

				this.jobs = new JobsCollection();
				this.jobsView = new JobsCollectionView({collection: this.jobs});
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

				this.users = new UsersCollection();
				this.accessStatsView = new AccessStatsCompositeView({collection: this.users});
				this.users.fetch();

				this.accessStatsSection = new SectionView({
					title: 'Access Statistics',
					description: ''
				});
			},

			onRender: function() {
				this.jobsSectionRegion.show(this.jobsSection);
				this.jobsRegion.show(this.jobsView);
				this.notificationsSectionRegion.show(this.notificationsSection);
				this.notificationsRegion.show(this.notificationsView);
				this.accessStatsSectionRegion.show(this.accessStatsSection);
				this.accessStatsRegion.show(this.accessStatsView);
			}
		});
	});
