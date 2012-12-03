/**
 * Essence 2012
 *
 * WebSocket connection to backend
 */
var Ess = Ess || {}

/**
 * Ess socket
 *
 */
Ess.Socket = function() {
    this.init();

    var self = this;

    Ess.on('Socket-request', function(data) {
        self.sendRequest(data);
    });
};

/**
 * Initialise socket
 *
 */
Ess.Socket.prototype.init = function() {
    var self = this;

    var uri = (window.location.protocol === 'https:') ? 'wss://' : 'ws://';
    uri += window.location.host + '/socket';

    this.socket = new WebSocket(uri);

    this.socket.onopen = function(event) {
        self.onOpen(event);
    };
    this.socket.onclose = function(event) {
        self.onClose(event);
    };
    this.socket.onmessage = function() {
        self.onMessage(event);
    };
    this.socket.onerror = function() {
        self.onError(event);
    };
};

/**
 * Connection opened
 *
 */
Ess.Socket.prototype.onOpen = function(event) {
    console.log('EssSocket: connected');
};

/**
 * Connection closed
 *
 */
Ess.Socket.prototype.onClose = function(event) {
    console.log('EssSocket: close', event.data);
};

/**
 * Message received
 *
 */
Ess.Socket.prototype.onMessage = function(event) {
    console.log(Date(), 'EssSocket: message', JSON.parse(event.data));
};

/**
 * Error received
 *
 */
Ess.Socket.prototype.onError = function(event) {
    console.log('EssSocket: error', event.data);
};

/**
 * Send request
 *
 */
Ess.Socket.prototype.sendRequest = function(data) {
    this.socket.send(data);
};

