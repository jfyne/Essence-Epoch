/**
 * Essence 2012
 *
 * Epoch
 *
 */
var Ess = Ess || {}

/**
 * Epoch controller
 *
 */
Ess.Epoch = Backbone.View.extend({

    el: $('#epoch'),

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

        console.log('Epoch initialised');

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
    var epoch = new Ess.Epoch;
});