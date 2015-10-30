# ZeroConf plugin for Cordova/Phonegap #

This plugin allows you to browse and publish ZeroConf/Bonjour/mDNS services from applications developed using PhoneGap/Cordova 3.0 or newer.


## Adding the Plugin to your project ##

In your application project directory:

```bash
cordova plugins add https://github.com/cambiocreative/cordova-plugin-zeroconf
```

## Using the plugin ##

There are six static methods on the ZeroConf object, as follows:

### `watch(type, callback)`
Note that `type` is a fully-qualified service type, including the domain, e.g. `"_http._tcp.local."`

`callback` is a function that is called when services are added and removed. The function is passed
an object with the following structure:

```javascript
{
	"service": {
		"port": 50930,
		"protocol": "tcp",
		"application": "http",
		"urls": ["http://192.168.2.2:50930", "http://fe80::7256:81ff:fe00:99e3:50930"],
		"description": "\\00",
		"name": "Black iPod",
		"domain": "local",
		"server": "",
		"addresses": ["192.168.2.2", "fe80::7256:81ff:fe00:99e3"],
		"type": "_http._tcp.local.",
		"qualifiedname": "Black iPod._http._tcp.local."
	},
	"action": "added"
}

```
For more information on the fields, see [the JmDNS docs](http://jmdns.sourceforge.net/apidocs/javax/jmdns/ServiceInfo.html).
If you edit ZeroConf.java, you can easily add more fields if you need them.

### `unwatch(type)`
Stops watching for services of the specified type.

### `close()`
Closes the service browser and stops watching.

### `register(type, name, port, text)`
Publishes a new service. The fields are as in the structure above. For more information,
see [the JmDNS docs](http://jmdns.sourceforge.net/apidocs/javax/jmdns/ServiceInfo.html).

### `unregister()`
Unregisters all published services.

### `list(type, timeout, success, error)`
List all published services, search for timeout (in ms). The fields are as in the structure above.

## Credits

It depends on [the JmDNS library](http://jmdns.sourceforge.net/). Bundles [the jmdns.jar](https://github.com/twitwi/AndroidDnssdDemo/) library.

## Licence ##

The MIT License
