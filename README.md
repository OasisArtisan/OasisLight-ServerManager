# OasisLight | Linux Server Manager
A light weight java program for linux users that makes managing and monitoring servers extremely easy.

## Features
- The manager is open source and free to use for as many servers and as many players as you like
- No need to edit scripts or configurations the manager does everything for you using a simple console interface.
- Automatic restart option for any crashed servers. (Makes sure your servers stay online)
- Custom full backups of all your servers to a minimal compressed size.
- Scheduale periodic restarts, backups, announcements, or any console command for any server.
- A monitor screen that displays all of your servers and their status in one place.
- Extremely light as it can run on as little as 5MB of RAM for medium sized networks.

## Getting started
### Starting the manager
After downloading the jar simply run it with a basic java command specifying the maximum ram if prefered.
```
java -Xmx5M -jar /.../.../ServerManager.jar
```
If the manager fails to start, Make sure you the following prerequisites installed:
- JAVA
- JPS
- SCREEN
- ZIP

To check, simply type the command followed by `--help`. If the command is not installed linux will provide you with the command to use to install it since all the prerequisites needed are native to most linux repositories.

### Essential commands
Now that you started the manager you should keep these commands in mind which work in almost all of the manager menus
- **help** never you feel yourself lost on where you are or what commands do always type help.
- **back** You can always back out of any menu or any process so make sure to use it whenever stuck.
- **clear** Whenever you feel like the screen is filled with old output use clear to clear your screen and show you the current active menu.
- **quit** you can quit the program from any place in the manager this is the equivelant of `ctrl + c`.

### Adding your first server
- Make sure that the server is offline.
- Use the command `add` from the main menu.
- type in the name you want to give for the server.
- type in the path to the server's jar file ex. /home/oasis/survival/spigot.jar

And you're all set you can open the server using `open <name>` and start the server from the control menu.

**The manager is fairly easy to understand and navigate however if you need explicit instructions on how to utilize all the features visit the wiki.**
