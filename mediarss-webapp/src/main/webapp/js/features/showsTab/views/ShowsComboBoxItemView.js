define([
        'marionette',
        'handlebars',
        'text!features/showsTab/templates/shows-combobox-item.tpl'
    ],
    function (Marionette, Handlebars, template) {
        "use strict";

        return Marionette.ItemView.extend({
            template: Handlebars.compile(template),
            className: 'shows-combobo-item',
            tagName: 'option',

            constructor: function (options) {
                Marionette.ItemView.prototype.constructor.apply(this, arguments);
                this.vent = options.vent;
            },

            onRender: function () {
                // get rid of that pesky wrapping-div, assumes 1 child element.
//				this.$el = this.$el.children();
//				this.setElement(this.$el);
                this.$el.attr('value', this.model.get('id'));
                this.vent.trigger('shows-combo-show-add', this.model);
            },

            onClose: function () {
                this.vent.trigger('shows-combo-show-remove', this.model);
            }
        });
    });

