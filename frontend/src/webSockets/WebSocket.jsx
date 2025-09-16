import React, {useEffect, useState} from "react";
import {Client} from "@stomp/stompjs";
import SockJS from "sockjs-client/dist/sockjs";


function WebSocketComponent() {
    const [error, setError] = useState(null);
    const [counter, setCounter] = useState(0);
    const [serverMessage, setServerMessage] = useState(null);

    useEffect(() => {
        // Create STOMP client
  
        const client = new Client({
            webSocketFactory: () => new SockJS(import.meta.env.VITE_WEBSOCKET_URL + "/users"), // matches your Spring endpoint

            reconnectDelay: 5000, // auto-reconnect every 5s
            debug: (msg) => console.log("STOMP:", msg),
            onConnect: () => {
                console.log("Connected to WebSocket");

                // Subscribe to topic (server -> client)
                client.subscribe("/topic/log", (message) => {
                    console.log("Message received:", message.body);
                    setServerMessage(message.body);
                    setCounter((prev) => prev + 1);
                });

                // Publish message (client -> server)
                client.publish({
                    destination: "/users/log",
                    body: JSON.stringify({text: "hello from React"}),
                });
            },
            onStompError: (frame) => {
                setError("Broker error: " + frame.headers["message"]);
            },
        });

        client.activate();

        // Cleanup on unmount
        return () => {
            client.deactivate();
        };
    }, []);

    return (
        <div>
            <h2>WebSocket Messages</h2>
            {error && <p style={{color: "red"}}>Error: {error}</p>}
            <p>Messages received: {counter}</p>
            <p>Last message: {serverMessage}</p>
        </div>
    );
}

export default WebSocketComponent;
