define([
        'marionette',
        'handlebars',
        'features/showsTab/views/ShowsComboBoxItemView',
        'chosen',
        'utils/Utils'
    ],
    function (Marionette, Handlebars, ShowsComboBoxItemView, Chosen, Utils) {
        "use strict";

        return Marionette.CollectionView.extend({
            itemView: ShowsComboBoxItemView,
            className: 'show-combobox',
            tagName: 'select',

            constructor: function (options) {
                this.itemViewOptions = {vent: options.vent};
                this.vent = options.vent;

                Marionette.CollectionView.prototype.constructor.apply(this, arguments);
            },

            clearSelection: function () {
                this.$el.val('').trigger("liszt:updated");
            },

            getSelectedShow: function () {
                var selectedShowId = this.$el.val();
                return this.collection.get(selectedShowId);
            },

            onRender: function () {
                this.$el.attr('data-placeholder', 'Select a show');

                // inject a first empty options element - <option></option> - ONLY if not yet there!
                if (this.$el.children(":first").length == 0 || this.$el.children(":first").html().length > 0) {
                    this.$el.prepend('<option selected></option>');
                }

                var that = this;
                this.vent.on('shows-combo-show-add', function () {
                    that.$el.trigger("liszt:updated");
                });

                this.vent.on('shows-combo-show-remove', function () {
                    that.$el.trigger("liszt:updated");
                });
            },

            onShow: function () {
                Utils.waitForDisplayAndCreate('.show-combobox', this.createChosen);
                this.clearSelection();
            },

            createChosen: function (selector) {
                $(selector).chosen({
                    no_results_text: "No shows matched"
                });
            }
        });
    });

