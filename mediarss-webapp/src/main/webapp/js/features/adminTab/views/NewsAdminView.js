define([
        'marionette',
        'handlebars',
        'text!features/adminTab/templates/news.tpl',
        'utils/HttpUtils',
        'utils/MessageBox'
    ],
    function (Marionette, Handlebars, template, HttpUtils, MessageBox) {
        "use strict";

        return Marionette.Layout.extend({
            template: Handlebars.compile(template),
            className: 'newsAdmin',

            ui: {
                newsField: '.news-textarea'
            },

            events: {
                'click .news-post-button': 'onSubmitButtonClick'
            },

            constructor: function (options) {
                Marionette.Layout.prototype.constructor.apply(this, arguments);
            },

            onRender: function () {
            },

            onSubmitButtonClick: function () {
                var that = this;
                var text = this.ui.newsField.val();

                HttpUtils.post("rest/admin/news", {
                    text: text
                }, function (res) {
                    that.ui.newsField.val('');
                    MessageBox.info('News has been created');
                });
            }
        });
    });

