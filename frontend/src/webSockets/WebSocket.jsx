import React, {useEffect, useState, useRef} from "react";
import {Client} from "@stomp/stompjs";
import SockJS from "sockjs-client/dist/sockjs";

function WebSocketComponent() {
    const [error, setError] = useState(null);
    const [counter, setCounter] = useState(0);
    const [serverMessage, setServerMessage] = useState(null);
    const [jobStatus, setJobStatus] = useState(null);
    const [jobID, setJobID] = useState(null);
    const clientRef = useRef(null);

    useEffect(() => {
        // Create STOMP client
        const client = new Client({
            webSocketFactory: () => new SockJS(import.meta.env.VITE_WEBSOCKET_URL + "/customers"),
            reconnectDelay: 5000,
            debug: (msg) => console.log("STOMP:", msg),
            onConnect: () => {
                console.log("Connected to WebSocket");

                // Subscribe to job ID topic
                client.subscribe("/topic/log", (message) => {
                    console.log("Received jobID:", message.body);
                    const receivedJobID = Number(message.body);
                    setJobID(receivedJobID);
                });

                // Subscribe to job status topic
                client.subscribe("/topic/jobStatus", (message) => {
                    console.log("Received job status:", message.body);
                    setJobStatus(message.body);
                    setCounter(prev => prev + 1);
                });
            },
            onStompError: (frame) => {
                console.error("STOMP error:", frame);
                setError("Broker error: " + frame.headers["message"]);
            },
        });

        client.activate();
        clientRef.current = client;

        // Cleanup on unmount
        return () => {
            client.deactivate();
        };
    }, []);

    // Separate effect to request job status when jobID is available
    useEffect(() => {
        if (jobID && clientRef.current?.connected) {
            console.log("Requesting status for jobID:", jobID);

            // Request job status
            clientRef.current.publish({
                destination: `/customers/job/${jobID}`,
                body: JSON.stringify({}),
            });
        }
    }, [jobID]);

    return (
        <div style={{padding: "20px", fontFamily: "Arial, sans-serif"}}>
            <h2>WebSocket Messages</h2>
            {error && <p style={{color: "red"}}>Error: {error}</p>}
            <h1>Hello this is izhar</h1>
            <p>Job ID: {jobID ?? "Waiting..."}</p>
            <p>Job Status: {jobStatus ?? "Waiting..."}</p>
            <p>Messages received: {counter}</p>
            <p>Last message: {serverMessage}</p>
        </div>
    );
}

export default WebSocketComponent;