/*
 * Cordova ZeroConf Plugin
 *
 * ZeroConf plugin for Cordova/Phonegap 
 * by Cambio Creative
 */

package com.cambiocreative;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiManager;
import android.util.Log;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class ZeroConf extends CordovaPlugin {
	WifiManager.MulticastLock lock;
	private JmDNS jmdns = null;
	private ServiceListener listener;
	private CallbackContext callback;

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);

		WifiManager wifi = (WifiManager) this.cordova.getActivity()
				.getSystemService(android.content.Context.WIFI_SERVICE);
		lock = wifi.createMulticastLock("ZeroConfPluginLock");
		lock.setReferenceCounted(true);
		lock.acquire();

		Log.v("ZeroConf", "Initialized");
	}

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) {
		this.callback = callbackContext;

		if (action.equals("watch")) {
			final String type = args.optString(0);
			if (type != null) {
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						watch(type); // Thread-safe.
					}
				});
			} else {
				callbackContext.error("Service type not specified.");
				return false;
			}
		} else if (action.equals("unwatch")) {
			final String type = args.optString(0);
			if (type != null) {
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						unwatch(type);
					}
				});
			} else {
				callbackContext.error("Service type not specified.");
				return false;
			}
		} else if (action.equals("register")) {
			JSONObject obj = args.optJSONObject(0);
			if (obj != null) {
				final String type = obj.optString("type");
				final String name = obj.optString("name");
				final int port = obj.optInt("port");
				final String text = obj.optString("text");
				if (type == null) {
					callbackContext.error("Missing required service info.");
					return false;
				}
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						register(type, name, port, text);
					}
				});
			} else {
				callbackContext.error("Missing required service info.");
				return false;

			}

		} else if (action.equals("close")) {
			if (jmdns != null) {
				try {
					jmdns.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (action.equals("unregister")) {
			if (jmdns != null) {
				jmdns.unregisterAllServices();
			}

        } else if (action.equals("list")) {
            final String type = args.optString(0);
            final int timeout = args.optInt(1);
            if (type != null && timeout > 0) {
                cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        list(type, timeout); // Thread-safe.
                    }
                });
            } else {
                callbackContext.error("Missing required parameter: type, timeout.");
                return false;
            
            }
		} else {
			Log.e("ZeroConf", "Invalid action: " + action);
			callbackContext.error("Invalid action.");
			return false;
		}
		PluginResult result = new PluginResult(Status.NO_RESULT);
		result.setKeepCallback(true);
		// return result;
		return true;
	}

	private void watch(String type) {
		if (jmdns == null) {
			setupWatcher();
		}
		Log.d("ZeroConf", "Watch " + type);
		Log.d("ZeroConf",
				"Name: " + jmdns.getName() + " host: " + jmdns.getHostName());
		jmdns.addServiceListener(type, listener);
	}

	private void unwatch(String type) {
		if (jmdns == null) {
			return;
		}
		jmdns.removeServiceListener(type, listener);
	}

	private void register(String type, String name, int port, String text) {
		if (name == null) {
			name = "";
		}

		if (text == null) {
			text = "";
		}

		try {
			ServiceInfo service = ServiceInfo.create(type, name, port, text);
			jmdns.registerService(service);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private void list(String type, int timeout) {
        try {
            JmDNS mdnsQuery = JmDNS.create(ZeroConf.getIPAddress());
            ServiceInfo[] services = mdnsQuery.list(type, timeout);
            sendListCallback("list", services);
            mdnsQuery.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void setupWatcher() {
		Log.d("ZeroConf", "Setup watcher");
		try {
			jmdns = JmDNS.create(ZeroConf.getIPAddress());
			listener = new ServiceListener() {

				public void serviceResolved(ServiceEvent ev) {
					Log.d("ZeroConf", "Resolved");

					sendCallback("added", ev.getInfo());
				}

				public void serviceRemoved(ServiceEvent ev) {
					Log.d("ZeroConf", "Removed");

					sendCallback("removed", ev.getInfo());
				}

				public void serviceAdded(ServiceEvent event) {
					Log.d("ZeroConf", "Added");

					// Force serviceResolved to be called again
					jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
				}
			};

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public void sendCallback(String action, ServiceInfo info) {
		JSONObject status = new JSONObject();
		try {
			status.put("action", action);
			status.put("service", jsonifyService(info));
			Log.d("ZeroConf", "Sending result: " + status.toString());

			PluginResult result = new PluginResult(PluginResult.Status.OK,
					status);
			result.setKeepCallback(true);
			this.callback.sendPluginResult(result);

		} catch (JSONException e) {

			e.printStackTrace();
		}

	}

    public void sendListCallback(String action, ServiceInfo[] services) {
        JSONObject status = new JSONObject();
        try {
            status.put("action", action);
        	JSONArray array = new JSONArray();
        	for (ServiceInfo service : services) {
        	    array.put(jsonifyService(service));
    	    }
	        status.put("service", array);
            Log.d("ZeroConf", "Sending result: " + status.toString());

            PluginResult result = new PluginResult(PluginResult.Status.OK, status);
            result.setKeepCallback(true);
            this.callback.sendPluginResult(result);

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }

	public static JSONObject jsonifyService(ServiceInfo info) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("application", info.getApplication());
			obj.put("domain", info.getDomain());
			obj.put("port", info.getPort());
			obj.put("name", info.getName());
			obj.put("server", info.getServer());
			obj.put("description", info.getNiceTextString());
			obj.put("protocol", info.getProtocol());
			obj.put("qualifiedname", info.getQualifiedName());
			obj.put("type", info.getType());

			JSONArray addresses = new JSONArray();
			String[] add = info.getHostAddresses();
			for (int i = 0; i < add.length; i++) {
				addresses.put(add[i]);
			}
			obj.put("addresses", addresses);
			JSONArray urls = new JSONArray();

			String[] url = info.getURLs();
			for (int i = 0; i < url.length; i++) {
				urls.put(url[i]);
			}
			obj.put("urls", urls);

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return obj;

	}

	/**
	 * Returns the first found IP4 address.
	 * 
	 * @return the first found IP4 address
	 */
	public static InetAddress getIPAddress() {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf
						.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						if (InetAddressUtils.isIPv4Address(sAddr)) {
							return addr;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
