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

## decompiling with IDA pro

1) Download il2cppDumper from https://github.com/Perfare/Il2CppDumper/releases/tag/v6.7.25 
2) Start tool Il2CppDumper.exe, select the \lib\armeabi-v7a\libil2cpp.so file and assets\bin\Data\Managed\Metadata\global-metadata.dat
3) This will dump out 4 files dump.cs, stringliteral.json, script.json and il2cpp.h
4) followed this link on how to use IDA pro https://platinmods.com/threads/how-to-use-il2cpp-h-script-json-and-stringliteral-json-il2cppdumper.119775/
5) Using 32 bit IDA.exe select \lib\armeabi-v7a\libil2cpp.so to decompile
6) in IDA file->script file and select ida_with_struct_py3.py then select script.json and il2cpp.h

## hooking attempts
1) using this tutorial https://www.areizen.fr/post/modding-unity-game/ and https://platinmods.com/threads/how-to-modify-unitys-il2cpp-string-methods.123414/
2) In the end it did not work as it hooked too late, found this which works https://github.com/jbro129/Unity-Substrate-Hook-Android
3) download Android NDK build r16b at https://developer.android.com/ndk/downloads/older_releases
4) download https://github.com/jbro129/Unity-Substrate-Hook-Android.git
5) unzip hooking template, and use this compile.bat to compile the hooking code, put the bat file in the same directory as the readme.

````
C:\Users\boo\Downloads\android-ndk-r16b\ndk-build 
NDK_PROJECT_PATH=\
NDK_APPLICATION_MK=\jni\Application.mk
````

6) modify compile.bat (from to configure it to point to ndk-build.bat location on first line (unzip NDK build r16b with the version of the machine you are running on, for me I am on 64 bit windows)
7) we want to hook onto function GetCompileTimeServer as that is how the client gets the hostname of the server
8) hook network tester download as to change the URL for the assetbundle downloads
9) the offset required to create string is at location 0xEC7244 which was found by looking at dump.cs
10) create a file jni/hook.cpp to be like this, using dump.cs to find the offset to the function we want to hook

````
#include <jni.h>
#include <android/log.h>
#include <Substrate/CydiaSubstrate.h>
#include "Unity/Quaternion.hpp" // C++ equivalent of Unity C# Quaternion. <- Credits unknown
#include "Unity/Vector3.hpp" // C++ equivalent of Unity C# Vector3. <- Credits unknown
#include "Unity/Vector2.hpp" // C++ equivalent of Unity C# Vector2. <- Credits unknown
#include "Unity/Unity.h" // C++ equivalent of Unity List/Dictionary/Array <- Credits to Shmoo for this (Small version of https://github.com/shmoo419/UnityStuff/blob/master/Unity.h)
/*
#define LOG_TAG  "JbroMain"

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
*/

long baseAddr(char *soname)  // credits to https://github.com/ikoz/AndroidSubstrate_hookingC_examples/blob/master/nativeHook3/jni/nativeHook3.cy.cpp
{
    void *imagehandle = dlopen(soname, RTLD_LOCAL | RTLD_LAZY);
    if (soname == NULL)
        return NULL;
    if (imagehandle == NULL){
        return NULL;
    }
    uintptr_t * irc = NULL;
    FILE *f = NULL;
    char line[200] = {0};
    char *state = NULL;
    char *tok = NULL;
    char * baseAddr = NULL;
    if ((f = fopen("/proc/self/maps", "r")) == NULL)
        return NULL;
    while (fgets(line, 199, f) != NULL)
    {
        tok = strtok_r(line, "-", &state);
        baseAddr = tok;
        tok = strtok_r(NULL, "\t ", &state);
        tok = strtok_r(NULL, "\t ", &state); // "r-xp" field
        tok = strtok_r(NULL, "\t ", &state); // "0000000" field
        tok = strtok_r(NULL, "\t ", &state); // "01:02" field
        tok = strtok_r(NULL, "\t ", &state); // "133224" field
        tok = strtok_r(NULL, "\t ", &state); // path field

        if (tok != NULL) {
            int i;
            for (i = (int)strlen(tok)-1; i >= 0; --i) {
                if (!(tok[i] == ' ' || tok[i] == '\r' || tok[i] == '\n' || tok[i] == '\t'))
                    break;
                tok[i] = 0;
            }
            {
                size_t toklen = strlen(tok);
                size_t solen = strlen(soname);
                if (toklen > 0) {
                    if (toklen >= solen && strcmp(tok + (toklen - solen), soname) == 0) {
                        fclose(f);
                        return (long)strtoll(baseAddr,NULL,16);
                    }
                }
            }
        }
    }
    fclose(f);
    return NULL;
}

long location; // save lib.so base address so we do not have to recalculate every time causing lag.

long getRealOffset(long offset) // calculate dump.cs address + lib.so base address.
{
    if (location == 0)
    {
        //arm
        location = baseAddr("/data/app/com.lucasarts.starts_goo-1/lib/arm/libil2cpp.so"); // replace the com.package.name with the package name of the app you are modding.
        if (location == 0)
        {
            //x86
            location = baseAddr("/data/app-lib/com.lucasarts.starts_goo-1/libil2cpp.so"); // do the same here.
        }
    }
    return location + offset;
}

typedef struct _monoString {
    void *klass;
    void *monitor;
    int length;
    char chars[1];

    int getLength() {
        return length;
    }

    char *getChars() {
        return chars;
    }
} monoString;

monoString *CreateMonoString(const char *str) {
    monoString *(*String_CreateString)(void *instance, const char *str) = (monoString *(*)(void *, const char *))getRealOffset(0xEC7244);

    return String_CreateString(NULL, str);
}

monoString* (*old_GetServer)(void *instance, void *methodInfo);
monoString* GetServer(void *instance, void *methodInfo) {
	return CreateMonoString("http://192.168.1.142:8080/starts");
}

void* (*old_NetworkConnectionTester_Download)(void *instance, monoString *url, void *methodInfo);
void* NetworkConnectionTester_Download(void *instance, monoString *url, void *methodInfo) {
	return old_NetworkConnectionTester_Download(instance,CreateMonoString("http://192.168.1.142:8080/connection_test.txt"), methodInfo);
}

// the constructor is ran when the lib is loaded
// \smali\com\unity3d\player\UnityPlayerActivity.smali -> onCreate
__attribute__((constructor))
void libjbro_main() {
    __android_log_print(ANDROID_LOG_DEBUG,"Hook", "Going to Cheat");
    MSHookFunction((void *) getRealOffset(0x6404C8), (void *)&GetServer, (void**)&old_GetServer);
    MSHookFunction((void *) getRealOffset(0x35545C), (void *)&NetworkConnectionTester_Download, (void**)&old_NetworkConnectionTester_Download);
    __android_log_print(ANDROID_LOG_DEBUG,"Hook", "hooked");
}

````

10) from command line run Android-Hooking-Template/compile.bat which will compile and build libhook.so
11) modify APK to load our library, will continue using the decompiled version done through Easy APK Tool
12) copy our built Android-Hooking-Template\libs\armeabi-v7a\libhook.so to our decompiled APK Easy Tool\1-Decompiled APKs\SWCfromTablet\lib\armeabi-v7a
13) use Easy APK Tool to compile, align and then install into the emulator