// import the module
var mdns = require('mdns');

// advertise a http server on port 4321
var ad = mdns.createAdvertisement(mdns.tcp('http'), 4321);
ad.start();
