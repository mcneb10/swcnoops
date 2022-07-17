# SwcNoops
Star Wars Commander private server.

A reverse engineer of the server to run the game Star Wars Commander privately.
This first version has just enough APIs to provide a set of bases that can be attacked.

This is a standalone webserver to bring this great game back to players who miss it as much as I do.

To use the server :

1) Change config class to point to the root directory of the asset bundles and layouts.
2) Copy "swcFiles" folder, which contains the last manifest file, along with your copy of the assetbundles to a location configured in your config class.
3) Copy a set of base layouts (same format as layout manager) to the location configured in your config class.
4) Start the server by executing Main class, currently I run in debug to catch any missing APIs not implemented.
5) Run a patched version of the game client to point to the server OR run the game through a proxy and redirect the calls.

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

## What works, what does not, TODO and where is this going
The first version of the server supports a single player with a maxed out base and PVP working.
You can attack the bases provided in your layouts folder. 
Currently donating to your SC does not work, and enemy SC is empty.
Enemy does not have any Droidekas.
Anything outside of that you will crash your client as it does not like any failed APIs that does not provide what it needs.
The good news, if you are using fiddler the errors get sent out to a URL with "bi_event2" at the end, and that sometimes gives you details on critical errors.
That is how I recognise if there are problems and which API is not working.

What works so far
* Saving player state for troops, map and builds
* Player can join a self donating squad to donate to oneself
* Campaign missions
* Deka platform and upgrading
* Enemy base always has droidekas and creature
* Transition to prestige
* Enemy SC to always be full

Things to work on in no particular order (eventually)
* Creating and Saving War layouts
* Multiplayer support
* Replays
* Planet objectives
* Daily base defense
* Handling resources that are collected and raided in attacks

Things that may never work.
* Squad chat
* Episodes and special events

Who knows where this will lead to, not really thought that far.
I program all day as a day job and to be honest I dont enjoy it when I stop work. However I find reverse engineering different and currently learning new things is a lot of fun. Maybe one day I will do a remake of a version of this game, but I cant see attempting that any time soon and certainly not to do that on my own.

## Simple build instructions with IntelliJ with a zip of the code

1. Download ZIP code for main branch in Github.
2. Unzip the file swcnoops-main.zip to the root of C drive
3. Rename the folder that is in C drive from swcnoops-main to swcnoops so you have a folder C:\swcnoops
4. Start IntelliJ 2022.1 (latest community version) and click on Open button
5. Select folder "c:\swcnoops" and click on Ok button
6. A dialog pops up to say Maven build scripts found, click on Load Maven Project.
7. IntelliJ will sync and download all the maven dependencies, wait for this to finish, takes about a minute
8. Click on Build->Rebuild Project to build
9. Find main class by ctrl+n and type in "Main", select it and it will jump to the source
10. Right mouse anywhere in the main classes code, select Debug Main.main() to start the server
