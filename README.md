# BadIntent

## Introduction
BadIntent is the missing link between the Burp Suite and the core Android's IPC/Messaging-system.  BadIntent consists of two parts, an Xposed-based module running on Android and a Burp-plugin. Based on this interplay, it is possible to use the Burp's common workflow and all involved tools and extensions, since the intercept and repeater functionality is provided. BadIntent hooks deeply into the Android system, performs various method redirections in Parcels and adds additional services to provide the described features. Most notably, BadIntent works system-wide **experimental** and is not restricted to individual user apps.

<img src="https://github.com/mateuszk87/BadIntent/blob/master/doc/img/main.png" width="700" />

## Installation
The most handy approach is to install BadIntent Android from the Xposed Module Repository and BadIntent Burp from the Burp’s BApp Store. Both will be made available/submitted during the Arsenal presentation of BadIntent in Black Hat Las Vegas 2017. 

## Environment
BadIntent has been tested on Genymotion with Xposed v87 on Android Marshmallow (6.0) and Burp Suite 1.7.23 (Free and Pro).

There are known limitations in hooking all system apps and all interfaces. During the boot proccess the Android system will remain in a boot loop and you will not be able to uninstall BadIntent from your Android device. Therefore, it is strongly recommended to use the mentioned setup in case all system apps are hooked. 

## Configuration & Usage
Please refer to the wiki pages of this repo.

## License
BadIntent is released under a 3-clause BSD License. See LICENSE for full details.



