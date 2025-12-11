🌐 Tutorial: Auto-Connecting Java Apps over Wi-Fi/VPN
Goal: We want a Client device (like a phone or second laptop) to automatically find and connect to a Database hosted on your main Laptop, without typing any IP addresses manually.

Part 1: The Toolkit (The Ingredients)
Before looking at the code, you need to understand the tools you are using and what role they play.

1. 🛠️ XAMPP (The Server Infrastructure)
What it is: A software package that installs a web server (Apache) and a database server (MySQL) on your computer.

Role: It turns your laptop into a Server. Without it, your laptop is just a personal computer. With XAMPP running, your laptop becomes a "place" that other computers can visit to get data.

Key Action: You must click "Start" on MySQL in the XAMPP Control Panel.

2. 🗄️ MySQL (The Filing Cabinet)
What it is: The actual database software inside XAMPP.

Role: It stores your user_accounts, pets, and appointments.

Key Action: Your Java app sends SQL commands (like SELECT * FROM users) to MySQL, and MySQL sends back the data.

3. 🛡️ Radmin VPN (The Virtual Cable)
What it is: Virtual Private Network software.

Role: Normally, devices on different networks (e.g., Laptop on Home Wi-Fi, Friend on their Mobile Data) cannot talk to each other. Radmin VPN creates a "Virtual Room." Anyone who joins this room thinks they are plugged into the same router cable.

Key Action: It gives your laptop a special IP address (e.g., 26.115.66.121) that is visible to anyone else in the Radmin group.

4. ☕ Java (The Application)
What it is: Your programming language.

Role: It runs the logic to "shout" across the network, "listen" for shouts, and "dial" the database.

Part 2: The Logic (How it Works)
We will break this down into a story of two characters: The Host (Laptop) and The Client (Other PC/Phone).

Step 1: The "Identity Crisis" (Am I a Host?)
File: src/PawTrackLogin.java

When the app starts, it doesn't know which device it is running on. It performs a self-test.

The Test: It tries to connect to a database inside itself (localhost).

Scenario A (Host): The connection works! The app says, "I have the database. I am the Server."

Action: It starts listening for others (DatabaseDiscovery.startServerMode()).

Scenario B (Client): The connection fails. The app says, "I have no database. I am a Client."

Action: It starts looking for the server (DatabaseDiscovery.findServer()).

Code Reference: This happens inside attemptAutoDiscovery() in PawTrackLogin.java.

Step 2: The Search (The "Shout")
File: src/DatabaseDiscovery.java

Now the Client needs to find the Host. It uses a technique called UDP Broadcast.

The Client's Move: It cannot call the Host directly because it doesn't know the Host's number (IP address).

The Solution: It uses a "Megaphone." It broadcasts the message "WHO_HAS_DATABASE" to the entire network address 255.255.255.255.

Real World Equivalent: Walking into a dark room and shouting, "Is anyone here?"

Code Reference:

Java

String message = "WHO_HAS_DATABASE";
DatagramPacket request = new DatagramPacket(..., InetAddress.getByName("255.255.255.255"), ...);
socket.send(request);
Step 3: The Discovery (The "Reply")
File: src/DatabaseDiscovery.java

The Host's Move: The Host laptop is quietly listening on Port 9999.

Hearing the Shout: It receives the packet "WHO_HAS_DATABASE".

The Reply: It looks at the return address (The Client's IP) and sends a direct message back: "I_HAVE_DATABASE".

Client Receives: The Client gets the message. It looks at the sender's info and realizes: "Aha! The sender is at IP 26.115.66.121."

Code Reference:

Java

// Server Side
if (message.equals("WHO_HAS_DATABASE")) {
    // Reply "I_HAVE_DATABASE"
    socket.send(response);
}
Step 4: The Connection (The Handshake)
File: src/DBConnector.java

Now the Client knows the IP address. It needs to save it so the rest of the app can use it.

Saving the IP: The Client calls DBConnector.setServerIP("26.115.66.121").

Updating the Link: Inside DBConnector, a variable serverIP is updated.

Connecting: Now, when you try to log in, the app builds a connection URL using that specific IP instead of localhost.

Code Reference:

Java

public static Connection getLocalConnection() {
    // Uses the discovered IP
    String targetHost = (serverIP != null) ? serverIP : LOCAL_HOST;
    String url = "jdbc:mysql://" + targetHost + ":" + ...;
    return DriverManager.getConnection(url, ...);
}
Part 3: How to Test It (Step-by-Step)
Here is how you actually verify this is working using your tools.

Prepare the Host (Laptop):

Open Radmin VPN and create a network.

Open XAMPP and click "Start" on Apache and MySQL.

Run your Java App (PawTrackLogin). It should print: "Server Mode Started on Port 9999".

Prepare the Client (Other PC/VM):

Connect this device to the same Radmin VPN network.

Make sure no XAMPP is running on this device.

Run the Java App.

Watch the Magic:

The Client App will pause for a second (searching).

It should pop up a message: "✅ Connected to Host: 26.x.x.x".

You can now log in using the database credentials stored on the Host laptop.

Troubleshooting Tips for Beginners
Firewall: Windows Firewall loves to block "Broadcasts" (Java shouting). If it fails, try turning off the firewall temporarily on both devices to test.

MySQL Permissions: By default, the root user in MySQL only allows connections from localhost. You might need to go into phpMyAdmin and create a user that allows access from % (Any Host).
