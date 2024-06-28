# p1

* Author: Daniel Rosales-Rodriguez
* Class: CS455 or CS555 [Distributed Systems] Section #001
* Semester: Spring 2024

## Overview

In this project, We will implement a simplified version of protocol IRC (Internet Relay Chat).The server will be able to
handle multiple clients and will be able to broadcast messages to other clients within the same channel based on the 
client's command input.

## Building the code

* javac *.java if you are in p1 directory, javac p1/*.java if you are in the parent directory.
* To Run Server: Java ChatServer [port]
* To Run Client: Java ChatClient [host] [port]

## Testing


After having built the code, I ran the server and client on the same machine. I used the following commands to make
requests to the server and see how it would handle the requests. I ran into quite a bit of bugs initially, but I was 
more keen on having the basic functionality of the server and client working. In this case, I was focused on users being
able to join a channel and leave a channel, as well as call /list to see the channels available and the number of users 
in each channel. After making sure that my program didn't fall apart when I made requests, I then focused on the 
broadcasting which was probably the easiest part of the project surprisingly. After that, I kept testing the project and
seeing if clients can write to each other in the same channel properly, nicknames had to be unique in each channel, 
users aren't allowed to join another channel without leaving first, and so on.


## Reflection


I ran into various problems. First off, I had to understand the complexities of a server-client model and figure out how
to implement it. I needed to go back and watch the lectures on the subject to understand how to go about designing it.
After having done that, I still needed more supplementation, so I went to the CS455-resources repository and looked at 
the examples as well as watched other youtubers implement their own design of a chat server. The main goal was to tackle
the server, as I think it would be easier to implement the client after it was done, I also wanted to make sure that I 
could at least get a server up and running before I implemented anything else. It was easy as I used the code from 
TimeServer in the resources repository and modified it to fit the criteria of the project. After a bit of tweaking, I 
was able to get the server to run. I had yet to implement processing cases for client requests based on the commands 
clients would input, so I sat down and thought what should a Server be able to do? I went over possible feats for the
channels in the server and implemented methods to add clients, remove clients, linked hashmaps to keep track of the 
clients and their names, and so on. It was coming together, but I was still far from done. 

After getting the basic feats for a chat channel implemented in ChatChannel.java, I continued to implement ChatServer. I
added switch cases to process each request from the client and added a method to broadcast messages to all clients in 
the same channel. Since I wasn't too sure on how to test it and make sure everything was working, I decided to use a 
dummy-client. I would write objects from the client side and send it out to the server and see how it would respond via
print statements. I was able to test /nick, /join, and /list. I would get the outcome I desired and kept working on the 
server. After getting those three commands implemented the rest was easy (if we're not mentioning edge cases). It was 
really the client-side I had the most trouble implementing. Using write object functions I figured the client would've
been easier to implement, but I was wrong, I was not accounting for a thread to actually listen for incoming messages 
from the server streams, so I had to implement a thread to listen for chats. Soon after, I was well on my way to getting
the project to work.

This is the part of the reflection where I talk about all the awful bugs I came across. I had a lot of bugs. I'll start
in order of each case to make this easier. First off, /nick was probably one of the easier cases to implement, but I 
needed to do some error checking, a user could join a channel but with the hashmap implementation, without a string the
user would register as null, and once they joined a channel there would be NullPointerExceptions and the client would
crash. So before they joined a channel I checked that the user choose a nickname first, and then they could join without
any issues. A minor bug I didn't check for was users must have unique nicknames in the same channel, but outside one 
they could have the same nickname. So if "Daniel" was in channel Alpha, and "Daniel" just connected to server and joined 
channel Alpha also, you would have two Daniels messaging one another, but if one of them changed their nickname in the 
channel and attempt to change it back to "Daniel" they would be denied. Other than that, /nick wasn't a problem. /join 
was a bit of a hassle. 

It was easy to add clients to a channel, in fact all you need is to implement a for each loop to 
look for the channels that matched the user input and place them accordingly. The main issue was users being able to 
join channels before leaving the one they were in, so I checked for it and ensured that users leave a channel first 
before joining another. Before this implementation, I would invoke /list and see that the user was in two channels at 
once, it was pretty annoying, but I was able to fix that. /leave wasn't so bad, there were a lot less checks for it. I 
just needed to remove the user from the channel if they invoked /leave, and if they were not in a channel, I would just 
print out a message saying they were not in a channel, or if they were in a channel and left with the wrong channel name
the server would notify them that they are not in the channel. The biggest issue was /quit, users would be able to quit
the server, but the thread wouldn't die, I kept debugging and debugging, but I couldn't figure out why the thread would
not cease. The thread would only die if the client completely terminated the program, and then the server would say that
the client has disconnected from ChatServer. This was the part that bothered me the most because I figured quitting a 
program would be the easiest part, but it was the most difficult in my case. Other than that, if a user quits I would
assume they are most likely terminating the program also, but if they wanted to, they could actually hog up the thread
pool and keep anybody else (the other 3 threads) from connecting to the server. 

Overall, this project was probably the toughest and most time-consuming project I've had to do, but I'm glad I was able
to get most of it done, and in fact I feel a lot more confident in my abilities to implement a server-client model. I
think this gives me a high baseline for future server projects as I would like to attempt this same one but in Golang
over the summer. It's pretty neat seeing how requests to the server are processed and how the server can handle multiple
clients at once. This was a great learning experience. 


## Video Demo and Program Walkthrough

https://youtu.be/lOZNr8Erzp8



