![](https://github.com/OasisArtisan/OasisLight-ServerManager/blob/master/Graphics/OasisLight_Banner.png?raw=true)
# OasisLight | Linux Server Manager
A light weight java program for linux users that makes managing and monitoring servers (MC servers in particular) extremely easy.

## Features
- The manager is open source and free to use for as many servers and as many players as you like
- No need to edit scripts or configurations the manager does everything for you using a simple console interface.
- Easy access for multiple admins by setting up SSH.
- Automatic restart option for any crashed servers. (Makes sure your servers stay online)
- Custom full backups of all your servers to a minimal compressed size.
- Scheduale periodic restarts, backups, announcements, or any console command for any server.
- A customizable monitor screen that displays all of your servers and their status in one place.
- A global control panel to restart, backup, and send a command to all servers with one command.
- Extremely light as it can run on as little as 5MB of RAM for medium sized networks.
- The manager can be used alongside a small server plugin to detect unresponsive states and to recieve player counts,ram usage etc.
## Screenshots
Adding servers.
![](https://github.com/OasisArtisan/OasisLight-ServerManager/blob/master/Screenshots/Adding_Servers.png?raw=true)
The Monitor showing servers linked to the SMMonitor spigot plugin.
![](https://github.com/OasisArtisan/OasisLight-ServerManager/blob/master/Screenshots/Monitor.png?raw=true)
## Getting started
### Starting the manager
After downloading the jar simply run it with a basic java command specifying the maximum ram if prefered.
```
java -Xmx5M -jar /.../.../OasisLight-ServerManager_1.1.jar
```
If the manager fails to start, Make sure you the following prerequisites installed:
- JAVA
- JPS ([you can find here](http://openjdk.java.net/install/))
- SCREEN
- ZIP

To check, simply type the command followed by `--help`. If the command is not installed linux will provide you with the command to use to install it since all the prerequisites needed are native to most linux repositories.

### Essential commands
Now that you started the manager you should keep these commands in mind which work in almost all of the manager menus
- **help** whenever you feel yourself lost on where you are or what commands do always type help.
- **back** You can always back out of any menu or any process so make sure to use it whenever stuck.
- **clear** Whenever you feel like the screen is filled with old output use clear to clear your screen and show you the current active menu.
- **quit** you can quit the program from any place in the manager this is the equivelant of `ctrl + c`.
Note: quiting the manager does not stop any servers. it simply stops all the services and schedualed commands the manager provides.
- A helpful thing to keep in mind is that most long commands have a shortcut for using them (Ex. `timedcommands` can be accessed by `tc`)
### Adding your first server
- Make sure that the server is offline.
- Use the command `add` from the main menu.
- type in the name you want to give for the server.
- type in the path to the server's jar file ex. /home/oasis/survival/spigot.jar

And you're all set you can open the server's control menu using `open servername` from the main menu and `start` the server from the control menu.
To open the server's console open a new terminal and type `screen -x servername`.

An important thing to note is that the manager was initially made to be used linked to a monitoring plugin. Therefore i recommend linking all of your servers by [following this guide](https://github.com/OasisArtisan/OasisLight-ServerManager/wiki/Linking-Servers).

**The manager is fairly easy to understand and navigate. However, if you need explicit instructions on how to utilize all the features visit the wiki.**

## Supported platforms
- All linux distributions that have the 4 prerequisites installed.

## Support
- If you are having any issues in setting up the manager feel free to [join the discord server](https://discord.gg/FejR9tv) and ask me directly.
- If you think there is a bug in the code open an issue on the issue page on github
- If you want to suggest improvements or request features to be added, preferrably open an issue with the tag [suggestion] in the title. or message me on discord.

If you found the manager useful and want to support its development consider donating or starring the repository. All is appreciated.

[![](https://proxy.spigotmc.org/8d6bb1a6b7ff7f4b68990696fd69823be7565a44?url=https%3A%2F%2Fwww.paypalobjects.com%2Fwebstatic%2Fen_US%2Fi%2Fbtn%2Fpng%2Fbtn_donate_92x26.png)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=omaralama31@yahoo.com&item_name=Donation&item_number=Oasis+Light+Server+Manager&currency_code=USD)
