define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/searcher-configuration-item.tpl',
	'qtip',
	'utils/HttpUtils'
],
	function(Marionette, Handlebars, template, qtip, HttpUtils, SearcherDomainConfigurationsCollection, SearcherDomainConfigurationsCollectionView) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'admin-searcher-conf-item',

			ui: {
				domainInput: '.admin-searcher-conf-domain-input input',
				domainList: '.admin-searcher-conf-domain-list'
			},

			events: {
				'click .admin-searcher-conf-domain-add-button': '_onAddButtonClick',
				'keypress .admin-searcher-conf-domain-input': '_onDomainInputKeyPress',
				'click .admin-searcher-conf-remove-domain-image': '_onRemoveDomainButtonClick'
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);

			},

			onRender: function() {
				var domainsArr = this.model.get('domains');
				var that = this;
				if (domainsArr.length === 0) {
					this.ui.domainList.hide();
				} else {
					domainsArr.forEach(function(domain) {
						that._addDomainToList(domain);
					});
				}
			},

			_onAddButtonClick: function() {
				var domain = this.ui.domainInput.val();
				var that = this;
				HttpUtils.post('rest/admin/searcher-configuration/' + this.model.get('name') + '/domain/add',
					{domain: domain},
					function(res) {
						that._addDomainToList(domain);
						that.ui.domainInput.val('');
						that.ui.domainList.show();
					});
			},

			_addDomainToList: function(domain) {
				this.ui.domainList.append(
					$('<div class="admin-searcher-conf-domain-item"></div>')
						.append($('<span class="admin-searcher-conf-domain-name"></span>').html(domain))
						.append('<img src="images/remove.png" class="admin-searcher-conf-remove-domain-image" _domain="' + domain + '">')
				);
			},

			_removeDomainFromList: function(domain) {
				for (var i in this.ui.domainList.children()) {
					var el = this.ui.domainList.children()[i];
					var curDomain = $(el).select('.admin-searcher-conf-domain-name').text();
					if (curDomain === domain) {
						el.remove();
						break;
					}
				}

				if (this.ui.domainList.children().length === 0) {
					this.ui.domainList.hide();
				}
			},

			_onDomainInputKeyPress: function(event) {
				var ENTER_KEY = 13;
				if (event.which === ENTER_KEY) {
					this._onAddButtonClick();
				}
			},

			_onRemoveDomainButtonClick: function(event) {
				var domain = event.currentTarget.getAttribute('_domain');
				console.log('clicked on: ' + domain);
				var that = this;
				HttpUtils.get('rest/admin/searcher-configuration/' + this.model.get('name') + '/domain/remove/' + domain, function(res) {
					that._removeDomainFromList(domain);
				});
			}
		});
	});

