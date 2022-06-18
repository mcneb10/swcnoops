# SwcNoops
Star Wars Commander private server.

A reverse engineer of the server to run the game Star Wars Commander privately.
This first version has just enough APIs to provide a set of bases that can be attacked.

This is a standalone webserver to bring this great game back to players who miss it as much as I do.
To use the server

1) Change config class to point to the root directory of the asset bundles and layouts
2) Copy "swcFiles" folder, which contains the last manifest file, along with your copy of the assetbundles to a location configured in your config class
3) Start the server by executing Main class, currently I run in debug to catch any missing APIs not implemented
4) Run a patched version of the game client to point to the server OR run the game through a proxy and redirect the calls

## Redirect game client
The game client can be easily tricked into having its calls redirected. In the early days, the community worked out how to access the server, which brought out layout manager and some other utilities. The old original disassembled source of the client was also available, which is mostly what allowed me to do this reverse engineering. All of these are still available on github and a good starting point in understanding the APIs.

Like them I used Fiddler to listen to the API calls between the client and server. This was easily achieved if you ran the game on an Android emulator using Android 5, or even easier if you ran the game through Facebook gameroom. Please dont ask me how to do this as you will find better instructions if you google fiddler and intercepting https calls. There is no need for me to repeat those instructions as all I did was follow what I found by googling.

Anyway to redirect the game client, I used Fiddler's autoresponder to intercept and redirect the following API calls from the client.
I would also recommend setting AutoResponder to not passthrough any unmatched rules, as this will prevent other calls it makes to facebook, logging and tracing.

>redirect - regex:https://zynga-swc-prod-1-seed.akamaized.net/(.*)
>to - http://<serverhost:port>/swcFiles/$1
>
>redirect - regex:https://swc-app-prod.apps.starwarscommander.com/(.*)
>to - http://<serverhost:port>/$1

where "serverhost" and "port" is the ip and port of your game server.
Notice that the redirect for zynga-swc-prod-1-seed.akamaized.net, changes the URL to pre-append "swcFiles", that is used by the server to recognise it is a GET for the manifest and assetbundles. This is case sensitive and must match with what you have set in the Config class.
The client if running on Android or an Android emulator, you need to configure the wifi connection to use the proxy (google this if you do not know how).

## Patching game client
Coming... (once I have worked out how to do it)

## Obtaining game asset bundle and modding
Currently I am undecided if I should provide out the assetbundles, the thinking behind that is I dont want the responsibility of potentially giving out a harmful binary (as some of those files I obtained from some link I found recently, which you can easily find if you search hard enough - hint some modelling site has provided some).

For code, layouts and the latest manifest file, I have no problems giving those out.
Now in theory, if you still have the game installed and played it before the shutdown, there is a chance you will have some of the assets in the unity cache. At present I dont know how to reverse those, but it should be feasible to turn those cached files back to a bundle. So if you still have the game installed, quickly take a copy of those files and keep them safe.

If you are a modder and you are interested in providing mods to the game. If you are interested then I can give you those bundles to mod, with the intention that it could be used freely to maybe one day bring this game back to the community. Unfortnately I do not have the skills to mod unity bundles, not one of my skill sets, although to be honest none of this is my area of expertise, I just miss the game, the squads and the community, and this is my way to give something back so this game is not lost forever.
