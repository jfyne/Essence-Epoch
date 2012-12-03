/**
 * Essence 2012
 *
 * MediaView
 *
 */
var Ess = Ess || {}

/**
 * MediaView controller
 *
 */
Ess.MediaView = Backbone.View.extend({

    el: $('#media-view'),

    initialize: function() {

        var self = this;

        // Add events to Ess global namespace
        _.extend(Ess, Backbone.Events);

        // Initialise Router
        this.router = new Ess.Router();
        Backbone.history.start();

        // Subscribe to Route events
        this.handleRoutes();

        // Initialise Web Socket
        this.socket = new Ess.Socket();

        // Build the view
        this.userView = new Ess.UserView();
        this.dataView = new Ess.DataView();
        this.timelineView = new Ess.TimelineView();
        this.navigatorView = new Ess.NavigatorView();

        console.log('MediaView initialised');

    },

    handleRoutes: function() {

        // Default route
        Ess.on('Router-default', function(query) {
            console.log('Route - Default: ' + query);
        });

        // Default route
        Ess.on('Router-filters', function(filter) {
            console.log('Route - Filters: ' + filter);
        });

    },

});

$(document).ready(function() {
    var mediaView = new Ess.MediaView;
});