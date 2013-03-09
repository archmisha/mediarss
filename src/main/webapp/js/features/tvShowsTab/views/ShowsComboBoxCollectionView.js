define([
	'marionette',
	'handlebars',
	'features/tvShowsTab/views/ShowsComboBoxItemView',
	'chosen'
],
	function(Marionette, Handlebars, ShowsComboBoxItemView, Chosen) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: ShowsComboBoxItemView,
			className: 'show-combobox',
			tagName: 'select',

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };
				this.vent = options.vent;

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			},

			clearSelection: function() {
				this.$el.val('').trigger("liszt:updated");
			},

			getSelectedShow: function() {
				var selectedShowId = this.$el.val();//find('option:selected').attr('value');
				return this.collection.get(selectedShowId);
			},

			onRender: function() {
				this.$el.attr('data-placeholder', 'Select a show');

				// inject a first empty options element - <option></option> - ONLY if not yet there!
				if (this.$el.children(":first").length == 0 || this.$el.children(":first").html().length > 0) {
					this.$el.prepend('<option selected></option>');
				}

				var that = this;
				this.vent.on('shows-combo-show-add', function() {
					that.$el.trigger("liszt:updated");
				});

				this.vent.on('shows-combo-show-remove', function() {
					that.$el.trigger("liszt:updated");
				});
			},

			onShow: function() {
				this.waitForElement();
				this.clearSelection();
			},

			waitForElement: function() {
				var that = this;
				var f = function() {
					if ($('.show-combobox').length == 0) {
//						console.log('element not present yet');
						setTimeout(f, 50);
					} else {
//						console.log('element IS present');
						that.createChosen();
					}
				};

//				console.log('Starting to wait for element');
				if ($('.show-combobox').length == 0) {
//					console.log('element not present yet');
					setTimeout(f, 50);
				}
			},

			createChosen: function() {
				$('.show-combobox').chosen({
					no_results_text: "No shows matched"
				});
			}
		});
	});

