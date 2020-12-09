# Covid-19-Contact-Tracing-App 

## Introduction

This application was created as a group project of CSCI 415 Networking and parallel computation at NDSU.

Our project is an android application that tracks close contact with android users.  Using CDC guidelines this application provides users a mobile solution to check if they have had close contact with an individual that have a positive test. 

Our app uses the bluetooth beacon library to send a ping to all nearby devices This ping is then stored to local storage on the users phone for cross referencing with the database that contains device ID's of users who have a positive test result.

We used a firebase database designed to store as little information as possible to avoid the misuse of personal information, this means that the database only stores a randomly generated device id that can not be used to identify a user.

---

## Getting started

To build this application first clone the repo to your desktop, import into android studio and sync the gradle files.
Check to make sure you have the right SDK package downloaded this version is built on Android 10.0 API level 29

Once the application is sync'd and built select which device you want this application to run on using the AVD manager.  For this project we highly reccommend connecting your own android device with bluetooth capablities since running on an emulator doesn't have access to bluetooth.

Once the application is running you will first be prompted to allow bluetooth permissions, Be sure to allow access.  If permissions are denied this app will not be able to discover beacons. Please go to Settings -> Applications -> permissions to grant location access to this app.

Next select Start Scanning to start the beacon (this will be running in the background). If you have had a positive result within the last 10 day select postive test result. Select Check If You Have Been Exposed to check if you have came in contact with anybody with the app that has had a postive test. The application will automatically check against the database upon each startup.


## Aditional Information

Downloading Android studio
https://developer.android.com/studio

Setting up your android own device
https://developer.android.com/studio/run/device

