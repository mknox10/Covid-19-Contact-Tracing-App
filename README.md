# Covid-19-Contact-Tracing-App 

## Introduction

This application was created as a group project of CSCI 415 Networking and parallel computation at NDSU.

Our project is an android application that tracks close contact with android users.
Using CDC guidelines this application provides users a mobile solution to check if they have
had close contact with an individual that have a positive test. 

Our app uses the bluetooth beacon library to send a ping to all nearby devices. This ping is
then stored to local storage on the users phone for cross referencing with the database 
that contains device ID's of users who have a positive test result.

We used a firebase database designed to store as little information as possible to avoid the misuse
of personal information,  this means that the database only stores a randomly generated device id that can not be used to identify a user.

---

## Getting started

To build this application first clone the repo to your desktop, import into android studio and
sync the gradle files.
Check to make sure you have the right SDK package downloaded this version is built on 
Android 10.0 API level 29