define([
        'marionette',
        'handlebars',
        'text!components/news/templates/news.tpl',
        'utils/HttpUtils',
        'utils/MessageBox'
    ],
    function (Marionette, Handlebars, template, HttpUtils, MessageBox) {
        "use strict";

        return Marionette.Layout.extend({
            template: Handlebars.compile(template),
            className: 'news-view',

            ui: {
                newsContentField: '.news-content'
            },

            events: {
                'click .news-dismiss-button': 'onDismissButtonClick'
            },

            constructor: function (options) {
                this.rendered = false;
                Marionette.Layout.prototype.constructor.apply(this, arguments);
            },

            onRender: function () {
                this.rendered = true;
                if (this.news) {
                    this.ui.newsContentField.html(this.news.map(function (news) {
                        return news.message.replace(/\n/g, '<br/>');
                    }).join('<br/><br/>'));
                }
            },

            onDismissButtonClick: function () {
                var that = this;

                HttpUtils.get("rest/news/dismiss", function () {
                    that.$el.hide();
                    MessageBox.info('News have been dismissed');
                });
            },

            setNews: function (news) {
                if (news.length === 0) {
                    this.$el.hide();
                } else if (this.rendered) {
                    this.ui.newsContentField.val(news.join('<br/>'));
                } else {
                    this.news = news;
                }
            }
        });
    });

