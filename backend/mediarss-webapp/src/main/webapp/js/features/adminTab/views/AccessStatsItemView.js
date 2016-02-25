define([
        'marionette',
        'handlebars',
        'text!features/adminTab/templates/access-stats-item.tpl',
        'qtip',
        'utils/HttpUtils'
    ],
    function (Marionette, Handlebars, template, qtip, HttpUtils) {
        "use strict";

        return Marionette.ItemView.extend({
            template: Handlebars.compile(template),
            className: 'access-stats-item',

            ui: {
                userName: '.username-column',
                impersonateButton: '.impersonate-button',
                impersonateButtonDisabled: '.impersonate-button-disabled'
            },

            events: {
                'click .impersonate-button': '_onImpersonateButtonClick'
            },

            constructor: function (options) {
                Marionette.ItemView.prototype.constructor.apply(this, arguments);
            },

            onRender: function () {
                this.ui.userName.qtip({
                    style: 'rssStyle'
                });
                if (this.model.get('loggedIn')) {
                    this.ui.impersonateButton.hide();
                    this.ui.impersonateButtonDisabled.show();
                } else {
                    this.ui.impersonateButton.show();
                    this.ui.impersonateButtonDisabled.hide();
                }
            },

            templateHelpers: function () {
                return {
                    'lastLoginFormatted': this.model.get('lastLogin'),
                    'lastShowsFeedGeneratedFormatted': this.model.get('lastShowsFeedGenerated'),
                    'lastMoviesFeedGeneratedFormatted': this.model.get('lastMoviesFeedGenerated')
                };
            },

            _onImpersonateButtonClick: function () {
                console.log('impersonating: ' + this.model.id);
                HttpUtils.get("rest/user/impersonate/" + this.model.id, function (res) {
                    window.parent.location.reload();
                });
            }
        });
    });

