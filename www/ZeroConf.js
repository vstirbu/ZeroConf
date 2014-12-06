/**
 * ZeroConf plugin for Cordova/Phonegap
 *
 * Copyright (c) 2013 Vlad Stirbu <vlad.stirbu@ieee.org> Converted to Cordova 3.0 format
 * MIT license
 *
 * @author Matt Kane
 * Copyright (c) Triggertrap Ltd. 2012. All Rights Reserved.
 * Available under the terms of the MIT License.
 * 
 */

/*global module, console, require*/
/*jshint -W097 */
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
