📡 Automatic Network Discovery & Database Connection
PawTrack features a custom-built Automatic Network Discovery System that allows client devices (such as a second laptop or mobile phone) to seamlessly find and connect to the main host's database without manual IP configuration.

This system works over local Wi-Fi, Hotspots, and Virtual Networks like Radmin VPN.

🚀 Key Features
Zero-Config Connection: Clients automatically find the database server.

Role Detection: The app intelligently decides if it is a Host (Server) or a Client based on the environment.

Failover Redundancy:

Local Check: Checks for a local database first.

Fast Track: Checks a list of known/hardcoded IP addresses.

Broadcast Discovery: "Shouts" to the entire network to find the host.

Manual Override: Prompts the user if all else fails.

🛠️ Tech Stack & Tools
Language: Java (Swing/AWT)

Database: MySQL (via XAMPP)

Networking:

UDP Sockets: For broadcasting discovery messages.

JDBC: For establishing the database connection.

Radmin VPN: (Optional) For connecting devices across different networks.

🧠 How It Works (The Logic)
The system follows a 4-step "Handshake" process to establish a connection.

Step 1: Identity Check (Host vs. Client)
On startup, the application checks for a local MySQL instance running on localhost.

If Found: The app becomes the HOST. It opens UDP Port 9999 and listens for incoming requests.

If Not Found: The app becomes a CLIENT. It begins searching for the Host.

Step 2: The Search (UDP Broadcast)
The Client sends a UDP packet containing the message "WHO_HAS_DATABASE" to the broadcast address 255.255.255.255. This effectively "shouts" the message to every device on the local network.

Step 3: The Handshake
The Host receives the "WHO_HAS_DATABASE" packet.

The Host identifies the sender's IP address.

The Host replies directly to that IP with the message "I_HAVE_DATABASE".

Step 4: Connection
The Client receives the reply.

The Client extracts the Host's IP address from the packet.

The DBConnector class updates its connection string to point to this new IP (e.g., jdbc:mysql://192.168.1.5:3306/pawpatrol_db).

The application connects, and the user can log in.

📂 Code Structure
1. src/PawTrackLogin.java (The Manager)
Role: Orchestrates the startup sequence.

Key Method: attemptAutoDiscovery() - Decides whether to start the Server listener or the Client searcher.

Fail-safe: Contains the scanKnownHosts() list for faster connections on known networks.

2. src/DatabaseDiscovery.java (The Communicator)
Role: Handles the raw UDP networking.

Key Methods:

startServerMode(): Listens on Port 9999 and sends replies.

findServer(listener): Broadcasts the search packet and waits for a response.

3. src/DBConnector.java (The Switchboard)
Role: Manages the active JDBC connection.

Key Logic: It holds a static serverIP variable. By default, it points to localhost. When a server is found, this variable is updated to the Host's IP, redirecting all database traffic.

🚦 Usage Guide
Setting up the Host (The Laptop)
Install XAMPP and start Apache and MySQL.

Ensure your pawpatrol_db is imported into phpMyAdmin.

(Optional) Open Radmin VPN and create/join a network.

Run the PawTrack application.

Console Output: ✅ Local Database Found! I am the HOST.

Console Output: 📡 Server Mode Started on Port 9999

Setting up the Client (The Other Device)
Ensure the device is on the same Wi-Fi or Radmin VPN network as the Host.

Run the PawTrack application.

Console Output: ❌ No Local Database. I am a CLIENT.

Console Output: 📡 Client: Broadcasting search...

Wait for the handshake.

Popup: ✅ Connected to Host: 26.x.x.x

🔧 Troubleshooting
Issue	Possible Cause	Solution
"Could not find Host"	Firewall Blocking	Turn off Windows Firewall on both Host and Client temporarily.
"Connection Refused"	MySQL Permissions	Ensure your MySQL user (root) allows access from % (Any Host), not just localhost.
No "I_HAVE_DATABASE" reply	Different Networks	Ensure both devices are on the exact same Wi-Fi or Radmin Network.
App Freezes on Start	Timeout too long	The discovery timeout is set to 5 seconds. Be patient or check the console logs.
