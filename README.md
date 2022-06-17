# SwcNoops
Star Wars Commander private server.

A reverse engineer of the server to run the game Star Wars Commander privately.
This first version has just enough APIs to provide a set of bases that can be attacked.

This is a standalone webserver to bring this great game back to players who miss it as much as I do.
To use the server

1) Change config class to point to the root directory of the asset bundles and layouts
2) Run a patched version of the game client to point to the server OR run the game through a proxy and redirect the calls

# Redirect game client
I used Fiddler's autoresponder to intercept and redirect the following API calls from the client.
I would also recommend setting AutoResponder to not passthrough any unmatched rules, as this will prevent other calls it makes to facebook, logging and tracing.

redirect - regex:https://zynga-swc-prod-1-seed.akamaized.net/(.*)

to - http://<serverhost:port>/swcFiles/$1

redirect - regex:https://swc-app-prod.apps.starwarscommander.com/(.*)

to - http://<serverhost:port>/$1

where "serverhost" and "port" is the ip and port of your game server.
The client if running on Android or an Android emulator, you need to configure the wifi connection to use the proxy (google this if you do not know how).

# Patching game client
Coming... (once I have worked out how to do it)

# Obtaining game asset bundle
Coming...

