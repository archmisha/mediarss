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
				routes[RoutingPaths.ADMIN] = 'showAdmin';
				routes[RoutingPaths.REGISTER] = 'register';
				routes[RoutingPaths.LOGOUT] = 'logout';
				routes['*path'] = 'showRoot';
				return routes;
			}()
		});
	});
