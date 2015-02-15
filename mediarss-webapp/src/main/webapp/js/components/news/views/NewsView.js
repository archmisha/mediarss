define([
        'marionette',
        'handlebars',
        'text!components/news/templates/news.tpl',
        'utils/HttpUtils'
    ],
    function (Marionette, Handlebars, template, HttpUtils) {
        "use strict";

        return Marionette.Layout.extend({
            template: Handlebars.compile(template),
            className: 'news',

            ui: {
                notificationsField: '.notifications-textarea'
            },

            events: {
                'click .notifications-post-button': 'onSubmitButtonClick'
            },

            constructor: function (options) {
                Marionette.Layout.prototype.constructor.apply(this, arguments);
            },

            onRender: function () {
            },

            onSubmitButtonClick: function () {
                var that = this;
                var text = this.ui.notificationsField.val();

                HttpUtils.post("rest/admin/notification", {
                    text: text
                }, function (res) {
                    that.ui.notificationsField.val('');
                    MessageBox.info('Notifications have been sent to all users');
                });
            }
        });
    });

