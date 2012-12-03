/**
 * Essence 2012
 *
 * Router
 *
 */
var Ess = Ess || {};

/**
 * Router
 *
 */
Ess.Router = Backbone.Router.extend({

    routes: {
        'filters':                  'filters',      // #filters
        'filters/:filter':          'filters',      // #filters/products
        '*query':                   'default'       // #404
    },

    initialize: function() {

    },

    default: function(query) {
        Ess.trigger('Router-default', query);
    },

    filters: function(filter) {
        Ess.trigger('Router-filters', filter);
    }

});