/*
 * Cordova ZeroConf Plugin
 *
 * ZeroConf plugin for Cordova/Phonegap 
 * by Cambio Creative
 */

'use strict';
var exec = require('cordova/exec');

var ZeroConf = {
	watch: function (type, callback) {
		return exec(function (result) {
			if (callback) {
				callback(result);
			}

		}, ZeroConf.fail, "ZeroConf", "watch", [type]);
	},
	unwatch: function (type) {
		return exec(null, ZeroConf.fail, "ZeroConf", "unwatch", [type]);
	},
	close: function () {
		return exec(null, ZeroConf.fail, "ZeroConf", "close", []);
	},
	register: function (type, name, port, text) {
		if (!type) {
			console.error("'type' is a required field");
			return;
		}
		return exec(null, ZeroConf.fail, "ZeroConf", "register", [type, name, port, text]);
	},
	unregister: function () {
		return exec(null, ZeroConf.fail, "ZeroConf", "unregister", []);
	},
        list: function(type, timeout, success, failure) {
                return exec(success, failure, "ZeroConf", "list", [type, timeout]);
        },
    	fail: function (o) {
		console.error("Error " + JSON.stringify(o));
	}
};

module.exports = ZeroConf;
