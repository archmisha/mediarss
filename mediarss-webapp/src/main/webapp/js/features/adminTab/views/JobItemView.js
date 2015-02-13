define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/job-item.tpl',
	'moment',
	'utils/HttpUtils'
],
	function(Marionette, Handlebars, template, Moment, HttpUtils) {
		"use strict";

		var JOB_MAX_HOURS = 2;

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'admin-job',
			tagName: 'ul',

			ui: {
				runningStatus: '.job-running',
				notRunningStatus: '.job-not-running',
				startingStatus: 'job-starting',
				neverRunStatus: '.job-never-run',
				progressDuration: '.job-running-duration',
				duration: '.job-not-running-duration',
				errorStatus: '.job-error-status',
				runningStartedOn: '.job-running-started-on',
				lastRunAt: '.job-last-run-at'
			},

			events: {
				'click .job-start': 'onJobStart'
			},

			_isJobRunning: function() {
				// handling some bugs - usually takes a few mins to run a job, so if running for more than 2 hours - means
				// the job is not really running
				return this.model.get('end') == null &&
					new Date().getTime() - this.model.get('start') < JOB_MAX_HOURS * 60 * 60 * 1000;
			},

			_updateDuration: function() {
				if (this.model.get('end') == null) {
					this.ui.duration.html('over  ' + JOB_MAX_HOURS + ' hours or aborted');
				} else {
					this.ui.duration.html(Moment.duration(this.model.get('end') - this.model.get('start')).humanize());
				}
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
				this.model.set('progressDuration', 0);
				this.model.set('duration', 0);
			},

			onRender: function() {
				if (this.model.get('start') == null) {
					this.ui.neverRunStatus.show();
				} else if (this._isJobRunning()) {
					this._markJobRunning();
				} else {
					this.ui.notRunningStatus.show();
					this.ui.lastRunAt.html(this._formatDate(this.model.get('start')));
					this._updateDuration();
					if (this.model.get('errorMessage') != null) {
						this.ui.errorStatus.show();
					}
				}
			},

			onClose: function() {
				// when leaving the view stop polling the server for job updates
				this._stopPollingThread();
			},

			_markJobRunning: function() {
				this.ui.runningStatus.show();
				this.ui.runningStartedOn.html(this._formatDate(this.model.get('start')));

				// start polling thread to update progress
				this._startPollingThread();
			},

			onJobStart: function() {
				var that = this;
				that.ui.notRunningStatus.hide();
				that.ui.neverRunStatus.hide();
				that.ui.errorStatus.hide();
				that.ui.startingStatus.show();
				$.post("rest/jobs/start", {
					name: this.model.get('name')
				}).success(function(jobStatus) {
					that.model.clear().set(jobStatus);
					that.ui.startingStatus.hide();
					that._markJobRunning();
				}).error(function(res) {
					console.log('error. data: ' + res);
					that.ui.startingStatus.hide();
					if (that.model.get('start') == null) {
						that.ui.neverRunStatus.show();
					} else {
						that.ui.runningStatus.show();
					}
				});
			},

			_startPollingThread: function() {
				var that = this;
				var f = function() {
					if (!that.timer) {
						return;
					}

					that.ui.progressDuration.html(Moment.duration((new Date()).getTime() - that.model.get('start')).humanize());

					$.get("rest/jobs/" + that.model.get('id'))
						.success(function(jobStatus) {
							that.model.clear().set(jobStatus);
							if (!that._isJobRunning()) {
								that._updateDuration();
								that._stopPollingThread();
							}
						}).error(function(res) {
							console.log('error. data: ' + res);
							that._stopPollingThread();
						});

					that.timer = setTimeout(f, 1000);
				};
				that.timer = setTimeout(f, 1000);
			},

			_stopPollingThread: function() {
				clearTimeout(this.timer);
				this.timer = null;
				this.ui.notRunningStatus.show();
				this.ui.lastRunAt.html(this._formatDate(this.model.get('start')));
				this.ui.runningStatus.hide();
				if (this.model.get('errorMessage') != null) {
					this.ui.errorStatus.show();
				}
			},

			_formatDate: function(date) {
				return Moment(new Date(date)).format('DD/MM/YYYY HH:mm');
			}
		});
	});

