define([
	'marionette',
	'routers/RoutingPaths'
],
	function(Marionette, RoutingPaths) {
		'use strict';

		return Marionette.AppRouter.extend({
			appRoutes: function() {
				var routes = {};
				routes[RoutingPaths.HOME] = 'showHomePage';
				routes[RoutingPaths.TVSHOWS] = 'showTVShows';
				routes[RoutingPaths.MOVIES] = 'showMovies';
				routes[RoutingPaths.SETTINGS] = 'showSettings';
				routes[RoutingPaths.ADMIN] = 'showAdmin';
				routes[RoutingPaths.LOGOUT] = 'logout';
				routes[RoutingPaths.MOVIE_PREVIEW] = 'showMoviePreview';
				routes['*path'] = 'showHomePage';
				return routes;
			}()
		});
	});
