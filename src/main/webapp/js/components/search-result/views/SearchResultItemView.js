define([
	'marionette',
	'handlebars',
	'text!components/search-result/templates/search-result-item.tpl',
	'utils/Utils',
	'moment'
],
	function(Marionette, Handlebars, template, Utils, Moment) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'search-result-item',

			ui: {
				downloadedStatus: '.search-result-item-downloaded-image',
				downloadButton: '.search-result-item-download-image',
				scheduledStatus: '.search-result-item-scheduled-image',
				body: '.search-result-item-body',
				size: '.search-result-item-size',
				scheduledOn: '.search-result-item-scheduled-on',
				scheduledOnDate: '.search-result-item-scheduled-on-date'
			},

			events: {
				'click .search-result-item-download-image': 'onDownloadButtonClick'
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.ItemView.prototype.constructor.apply(this, arguments);

				var that = this;
				this.model.on('change:downloadStatus', function() {
					that.updateDownloadStatus();
				});
				this.model.on('change:scheduledDate', function(model, val) {
					if (val) {
						that.ui.scheduledOnDate.html(Moment(val).format('DD/MM/YYYY HH:mm'));
						that.ui.scheduledOn.show();
					} else {
						that.ui.scheduledOn.hide();
					}
				});
			},

			onRender: function() {
				this.updateDownloadStatus();

				if (this.model.get('viewed') === false) {
					this.$el.addClass('search-result-item-not-viewed');
				}
			},

			onDownloadButtonClick: function() {
				this.vent.trigger('search-result-item-download', this.model);
			},

			onShow: function() {
				Utils.addTooltip([this.ui.downloadButton, this.ui.scheduledStatus, this.ui.downloadedStatus, this.ui.body]);
			},

			updateDownloadStatus: function() {
				if (this._statusClass) {
					this.$el.removeClass(this._statusClass);
				}
				this._statusClass = 'download-status-' + this.model.get('downloadStatus');
				this.$el.addClass(this._statusClass);
			},

			templateHelpers: function() {
				return {
					'escapedTitle': Utils.fixForTooltip(this.model.get('title'))
				};
			}
		});
	});

