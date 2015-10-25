/*global define*/
define([
        'marionette',
        'handlebars',
        'text!features/adminTab/templates/admin-tab.tpl',
        'features/adminTab/views/JobsCollectionView',
        'features/adminTab/collections/JobsCollection',
        'components/section/views/SectionView',
        'features/adminTab/views/NotificationsView',
        'features/adminTab/collections/UsersCollection',
        'features/adminTab/views/AccessStatsCompositeView',
        'utils/HttpUtils',
        'utils/MessageBox',
        'select2',
        'utils/Utils',
        'features/adminTab/views/SearcherConfigurationsCollectionView',
        'features/adminTab/collections/SearcherConfigurationsCollection',
        'features/adminTab/views/NewsAdminView'
    ],
    function (Marionette, Handlebars, template, JobsCollectionView, JobsCollection, SectionView, NotificationsView, UsersCollection, AccessStatsCompositeView, HttpUtils, MessageBox, select2, Utils, SearcherConfigurationsCollectionView, SearcherConfigurationsCollection, NewsAdminView) {
        "use strict";

        var SHOWS_COMBO_BOX_SELECTOR = '.admin-all-shows-combo';

        return Marionette.Layout.extend({
            template: Handlebars.compile(template),
            className: 'admin-tab',

            regions: {
                jobsSectionRegion: '.admin-jobs-section',
                jobsRegion: '.admin-jobs',
                accessStatsSectionRegion: '.admin-access-stats-section',
                accessStatsRegion: '.admin-access-stats',
                notificationsSectionRegion: '.admin-notifications-section',
                notificationsRegion: '.admin-notifications',
                newsSectionRegion: '.admin-news-section',
                newsRegion: '.admin-news',
                searcherConfigurationsSectionRegion: '.admin-searcher-configurations-section'
            },

            ui: {
                showsComboBox: SHOWS_COMBO_BOX_SELECTOR,
                deleteShowIdInput: '.admin-delete-show-id-input',
                deleteMovieIdInput: '.admin-delete-movie-id-input'
            },

            events: {
                'click .admin-download-show-schedule-button': '_onDownloadShowScheduleButtonClick',
                'click .admin-delete-show-button': '_onDeleteShowButtonClick',
                'click .admin-delete-movie-button': '_onDeleteMovieButtonClick'
            },

            constructor: function (options) {
                Marionette.Layout.prototype.constructor.apply(this, arguments);

                this.jobs = new JobsCollection();
                this.jobsView = new JobsCollectionView({collection: this.jobs});

                this.jobsSection = new SectionView({
                    title: 'Actions',
                    description: ''
                });

                this.notificationsSection = new SectionView({
                    title: 'Notifications',
                    description: 'Send a mass notification to all users'
                });
                this.notificationsView = new NotificationsView();

                this.newsSection = new SectionView({
                    title: 'News',
                    description: 'Create news'
                });
                this.newsAdminView = new NewsAdminView();

                this.users = new UsersCollection();
                this.accessStatsView = new AccessStatsCompositeView({collection: this.users});

                this.accessStatsSection = new SectionView({
                    title: 'Access Statistics',
                    description: ''
                });

                this.searcherConfigurationsCollection = new SearcherConfigurationsCollection();
                this.searcherConfigurationsCollectionView = new SearcherConfigurationsCollectionView({collection: this.searcherConfigurationsCollection});
                this.searcherConfigurationsSection = new SectionView({
                    title: 'Searchers configuration',
                    description: '',
                    content: this.searcherConfigurationsCollectionView
                });
            },

            onRender: function () {
                this.jobsSectionRegion.show(this.jobsSection);
                this.jobsRegion.show(this.jobsView);
                this.notificationsSectionRegion.show(this.notificationsSection);
                this.notificationsRegion.show(this.notificationsView);
                this.newsSectionRegion.show(this.newsSection);
                this.newsRegion.show(this.newsAdminView);
                this.accessStatsSectionRegion.show(this.accessStatsSection);
                this.accessStatsRegion.show(this.accessStatsView);
                this.searcherConfigurationsSectionRegion.show(this.searcherConfigurationsSection);
                this.jobs.fetch();
                this.users.fetch();
                this.searcherConfigurationsCollection.fetch();
            },

            onShow: function () {
                Utils.waitForDisplayAndCreate(SHOWS_COMBO_BOX_SELECTOR, this.createChosen);
            },

            createChosen: function (selector) {
                $(selector).select2({
                    placeholder: "Select a Show",
                    minimumInputLength: 3,
                    ajax: {
                        url: 'rest/shows/autocomplete',
                        dataType: 'jsonp',
                        transport: function (queryParams) {
                            return HttpUtils.get(queryParams.url + '?term=' + encodeURIComponent(queryParams.data.term), queryParams.success, false);
                        },
                        data: function (term, page) {
                            return {
                                term: term
                            };
                        },
                        results: function (data, page) {
                            return {results: data.shows};
                        }
                    },
                    formatResult: function (show) {
                        return show.text;
                    },
                    formatSelection: function (show) {
                        return show.text;
                    }
                });
            },

            _onDownloadShowScheduleButtonClick: function () {
                var showId = this.ui.showsComboBox.select2('data').id;
                // nothing is selected
                if (showId == undefined) {
                    return;
                }

                var that = this;
                HttpUtils.get("rest/shows/downloadSchedule/" + showId, function (res) {
                    that.ui.showsComboBox.select2('data', '');
                    MessageBox.info(res);
                });
            },

            _onDeleteShowButtonClick: function () {
                var showId = this.ui.deleteShowIdInput.val();

                if (!showId || showId.trim().length == 0) {
                    return;
                }

                var that = this;
                HttpUtils.get("rest/shows/delete/" + showId, function (res) {
                    that.ui.deleteShowIdInput.val('');
                    MessageBox.info(res);
                });
            },

            _onDeleteMovieButtonClick: function () {
                var movieId = this.ui.deleteMovieIdInput.val();

                if (!movieId || movieId.trim().length == 0) {
                    return;
                }

                var that = this;
                HttpUtils.get("rest/movies/delete/" + movieId, function (res) {
                    that.ui.deleteMovieIdInput.val('');
                    MessageBox.info(res);
                });
            }
        });
    });
