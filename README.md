# java-swing-secure-chat

### Motivation
That is my first Java project, written for a Java course I took at university.

### About the project
It is a secure chat that stores your messages encrypted on a server. While sending a message, the client encrypts the message being sent using the other user's public key(currently the public key is stored on the server which makes it insecure but it can easily be stored on some public blockchain too). Also it stores the raw message in a local database so that it can read it as it won't be possible to decrypt it later when the backend returns it in some way. It uses a custom simple TCP protocol.

### Known problems that need to be fixed
- public keys should be stored in some public blockchain
- there should be a proper session management because tcp connections can be spoofed
- client polls every second to get all messages from server which is really inefficient
- server uses a SQLite which is also quite inefficient

### How to start
Run the server, then run a few clients. Register with usernames and passwords then log in.

Note: you should use different filenames for the private keys of the different users so that they don't overwrite each other.
