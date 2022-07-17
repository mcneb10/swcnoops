## Patching game client

My notes and attempts on patching the game client to use fiddler proxy to talk to the server.

1) ADB to pull out the APK from device
2) Used APK Easy Tool v2.4.1 to decompile the APK

First have to work out after recompiling the decompiled APK will it still work.

1) Use APK Easy Tool Compile, ZipAlign - Test using MEMU with Android 5.5.
Dragged and dropped the ZipAlign version in C:\Users\boo\Documents\APK Easy Tool\2-Recompiled APKs onto MEMU to install.
This installed and game started.
2) Removing the various versions to determine which one to concentrate on first.
2a) Trying with only lib/x86 in the APK only - installed and worked (hmm odd as I thought the emulator will be using arm, using MEMU device profile ASUS Rog).
2b) Trying with only lib/arm64-v8a - failed gets an error "cannot be installed in the current environment..."
2c) Trying with only lib/armeabi-v7a - works

## Patching for armeabi-v7a - Chosen as this should give support for the most devices (not tried on own device as that still has the game installed, dont want to tocuh this yet)

1) Trying Android 5.5 to see if fiddler can see game requests
2) Configure wifi to use proxy to 192.168.1.142:8888 (you need to use the local IP address running fiddler, that was mine)
3) Can see the tunnelling from the game but not the requests
4) Downloading fiddler certificate onto emulator by using browser and going to 192.168.1.142:8888 which gave a page to download the certificate.
5) Download the certificate, then click on certificate to install onto the emulator
6) Install as "VPN and apps" in the dialog "Name the certificate", give any name
7) Emulator will ask to provide a pin
8) Can now see requests from client, can see graph.facebook.com, sessions.bugsnag.com, zynga-swc-prod-1-seed.akamaized.net, swc-app-prod.apps.starwarscommander.com.
There are other connections but can not tell if those are from the game or from android/device.

## Memu with Android 7.1 64 bit
9) repeated steps above, but can not see requests just the tunneling
10) followed this link https://gist.github.com/luciopaiva/aa9cb30863804fb2ac3ed1ccd11c95c7 to make APK accept user certificates
11) from the decompiled APK, created file res\xml\network_security_config.xml with these values

````
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>  
      <base-config>  
            <trust-anchors>  
                <!-- Trust preinstalled CAs -->  
                <certificates src="system" />  
                <!-- Additionally trust user added CAs -->  
                <certificates src="user" />  
           </trust-anchors>  
      </base-config>  
 </network-security-config>
````

12) edit XML element "application" to include android:networkSecurityConfig="@xml/network_security_config" in file AndroidManifest.xml.
There are two versions of android manifest file, the ones to modify are in the root directory and not the ones in build.
13) Compiled, aligned the APK, then uninstalled the old one and reinstalled with this new one, it works now.

## Memu with Android 9.0 64 bit
14) repeated steps above but not able to set lock pin, it asks for it but it seems to not have taken the setting and crashes.

## NOX with Android 9.0 64 bit
15) Works, but slow

## Android studio using AVD for Android 12.0
16) Too slow did not manage to test as could not even bring Chrome up to install certificate


