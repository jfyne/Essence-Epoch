/**
 * Essence 2012
 *
 * Epoch
 *
 */
var Ess = Ess || {}

/**
 * Timeline view
 *
 */
Ess.TimelineView = Backbone.View.extend({

    el: $('#timeline'),

    initialize: function() {

        var self = this;

        // Handle Resize
        $(window).resize(function() {
            self.resized();
        }).trigger('resize');

        // Handle Scroll
        $(self.el).scroll(function() {
            self.scrolled();
        });

        // Update timeline content size
        self.updateTimelineSize();

        //self.addItem('Google', 'Google Chrome');

    },

    resized: function() {

        var mastheadHeight = $('#masthead').outerHeight();

        // Update timeline view
        $(this.el).height($(window).height() - mastheadHeight);

        // Update timeline markers
        $(this.el).find('#timeline-markers').height($(window).height() - mastheadHeight);

    },

    scrolled: function() {

        var self = this;

        var newTopOffset = $(self.el).scrollTop();
        var newLeftOffset = $(self.el).scrollLeft();

        $(self.el).find('#timeline-markers ul').css('top', '-' + newTopOffset + 'px');
        $(self.el).find('#timeline-items ul').css('left', '-' + newLeftOffset + 'px');

    },

    /**
     * Update timeline content area, markers and items to reflect the contents
     * Most accurate way to determine width/height is to count child items
     */
    updateTimelineSize: function() {

        var self = this;

        var screenWidth = $(window).outerWidth() - $('#timeline-markers').outerWidth();
        var screenHeight = $(window).outerHeight() - $('#timeline-items').outerHeight();
        var itemWidth = $('#timeline-items li').outerWidth() * $('#timeline-items li').length;
        var markerHeight = $('#timeline-markers li').outerHeight() * $('#timeline-markers li').length;
        var width = (itemWidth > screenWidth) ? itemWidth : screenWidth;
        var height = (markerHeight > screenHeight) ? markerHeight : screenHeight;
        $(self.el).find('#timeline-items ul').width(width);
        $(self.el).find('#timeline-markers ul').height(height);
        $(self.el).find('#timeline-inner').width(width).height(height);

    },

    addItem: function(client, project) {

        var self = this;

        var booking = new Ess.BookingView({
            client: client,
            project: project,
            el: $('.bookings')
        });

    },

});