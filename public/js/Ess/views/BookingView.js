/**
 * Essence 2012
 *
 * Epoch
 *
 */
var Ess = Ess || {}

/**
 * Booking view
 *
 */
Ess.BookingView = Backbone.View.extend({

    client: 'Client Name',
    project: 'Project Name',

    initialize: function() {

        this.client = this.options.client || this.client;
        this.project = this.options.project || this.project;
        this.render();

    },

    events: {
        'click': 'bookingTapped'
    },

    render: function() {

        var variables = {
            client_name: this.client,
            project_name: this.project
        }

        var template = _.template(
            $('#booking-template').html(),
            variables
        );

        this.$el.html(template);

    },

    bookingTapped: function() {

        var self = this;
        alert('Booking: "' +  this.project + '" tapped');

    },

});