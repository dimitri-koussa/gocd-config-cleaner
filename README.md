# Gocd Config Cleaner

Replaces any potentially organisation specific strings in the Thoughtworks GoCD
config file with randomly generated words from the dictionary. This makes it
easier for paranoid companies to submit bug reports with a (partially)
functional config file.

The idea is to allow the Go server to build the dependency graph but does
not extend to having it successfully run the builds.

The sanitized config file can be imported into the go server. More specifically
it worked with my config file and using version 15.2 of the go server.

Idea shamelessly stolen from: https://github.com/wcurrie/gocd-config-cleaner
I tried..but scala...
