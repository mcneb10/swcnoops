# SwcNoops
Star Wars Commander private server.

A reverse engineer of the server to run the game Star Wars Commander privately.
This first version has just enough APIs to provide a set of bases that can be attacked.

This is a standalone webserver to bring this great game back to players who miss it as much as I do.
To use the server

1) Change config class to point to the root directory of the asset bundles and layouts
2) Run a patched version of the game client to point to the server OR run the game through a proxy and redirect the calls

## Redirect game client
I used Fiddler's autoresponder to intercept and redirect the following API calls from the client.
I would also recommend setting AutoResponder to not passthrough any unmatched rules, as this will prevent other calls it makes to facebook, logging and tracing.

>redirect - regex:https://zynga-swc-prod-1-seed.akamaized.net/(.*)
>to - http://<serverhost:port>/swcFiles/$1
>
>redirect - regex:https://swc-app-prod.apps.starwarscommander.com/(.*)
>to - http://<serverhost:port>/$1

where "serverhost" and "port" is the ip and port of your game server.
The client if running on Android or an Android emulator, you need to configure the wifi connection to use the proxy (google this if you do not know how).

## Patching game client
Coming... (once I have worked out how to do it)

## Obtaining game asset bundle and modding
Currently I am undecided if I should provide out the assetbundles, the thinking behind that is I dont want the responsibility of potentially giving out a harmful binary (as some of those files I obtained from some link I found recently, which you can easily find if you search hard enough - hint some modelling site has provided some).

For code, layouts and the latest manifest file, I have no problems giving those out, those I will checkin in when I get the chance.
Now in theory, if you still have the game installed and played it before the shutdown, there is a chance you will have some of the assets in the unity cache. At present I dont know how to reverse those, but it should be feasible to turn those cached files back to a bundle. So if you still have the game installed, quickly take a copy of those files and keep them safe.

If you are a modder and you are interested in providing mods to the game. If you are interested then I can give you those bundles to mod, with the intention that it could be used freely to maybe one day bring this game back to the community. Unfortnately I do not have the skills to mod unity bundles, not one of my skill sets, although to be honest none of this is my area of expertise, I just miss the game, the squads and the community, and this is my way to give something back so this game is not lost forever.
