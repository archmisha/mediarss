define([
	'marionette',
	'handlebars',
        'text!features/adminTab/templates/searcher-configuration-list.tpl',
	'utils/HttpUtils',
	'utils/MessageBox',
	'features/adminTab/views/SearcherConfigurationItemView'
],
    function (Marionette, Handlebars, template, HttpUtils, MessageBox, SearcherConfigurationItemView) {
		"use strict";

        return Marionette.CompositeView.extend({
            template: Handlebars.compile(template),
			itemView: SearcherConfigurationItemView,
            className: 'admin-searcher-conf-list-container',
            itemViewContainer: '.admin-searcher-conf-list',

            ui: {
                torrentzSearcherToggle: '.toggle-torrentz-searcher-checkbox'
            },

            events: {
                'click .toggle-torrentz-searcher-checkbox': 'onToggleTorrentzSearcherCheckboxClick'
            },

			constructor: function(options) {
                Marionette.CompositeView.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
            },

            onToggleTorrentzSearcherCheckboxClick: function () {
                var value = this.ui.torrentzSearcherToggle.is(':checked') ? true : false;
                var that = this;
                HttpUtils.post('rest/admin/searcher-configuration/torrentz/domain/add',
                    {domain: domain},
                    function (res) {
                        that._addDomainToList(domain);
                        that.ui.domainInput.val('');
                        that.ui.domainList.show();
                    });
            }
		});
	});

