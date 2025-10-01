# Sokoban

This is my implementation for a current version of Android of the classic, popular game Sokoban.

This is a puzzle game where you play as a worker in a storage facility. There are designated places for boxes to be stored. Boxes can only ever be pushed, not pulled. Solved puzzles are compared by amount of pushes and steps. 

## Motivation

There used to be an app that we liked to use, which became obsolete with the latest versions of Android on the devices we played on. 
Now I want to provide a playable, extensible and hopefully enjoyable version of the game. 
The initial concepts and trials of how to store levels were done in python, but it all quickly came together in Android too. 
The core game mechanics are there, you get notified on "Success" and everything ;-)
Still a lot to do though…

## ToDo

- use bundles of state saved when e.g. the app is sent to the background
- the levels listing should be optimised for UX
- the levels listing should include hints toward solved and solved-with-help
- provide a ZIP import method for packaged combos
- make the tutorial actually teach the game
