//
//  SignalServer.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/16.
//

import Foundation
import PocketSocket
import WebRTC

protocol SignalServerInterface: AnyObject {
    func sendOffer(_ conn: PSWebSocket)
    func receiveOffer(_ conn: PSWebSocket, _ desc: RTCSessionDescription)
    func receiveAnswer(_ desc: RTCSessionDescription)
    func candidate(_ message: [String: Any])
}

class SignalServer : NSObject, PSWebSocketServerDelegate {
    
    var server: PSWebSocketServer!
    
    var webSocketClient: PSWebSocket!
    
    weak var delegate: SignalServerInterface?
    
    override init() {
        super.init()
        if let localAddress = NetworkMonitor.getLocalIPAddress() {
            server = PSWebSocketServer(host: localAddress, port: 36901)
            server.delegate = self
            server.start()
            debugPrint("WebSocket Server started on ws://\(localAddress):36901")
        } else {
            debugPrint("Unable to get local IP address")
        }
    }
    
    func serverDidStart(_ server: PSWebSocketServer!) {
        debugPrint("Server started successfully")
    }
    
    func serverDidStop(_ server: PSWebSocketServer!) {
        debugPrint("Server stoped successfully")
    }
    
    func server(_ server: PSWebSocketServer!, didFailWithError error: (any Error)!) {
        debugPrint("WebSocket failed with error: \(error.localizedDescription)")
    }
    
    func server(_ server: PSWebSocketServer!, webSocketDidOpen webSocket: PSWebSocket!) {
        debugPrint("WebSocket connection opened")
    }
    
    func server(_ server: PSWebSocketServer!, webSocket: PSWebSocket!, didReceiveMessage message: Any!) {
        if let text = message as? String {
            print("Received message: \(text)")
            if let jsonData = text.data(using: .utf8) {
                do {
                    if let jsonMessage = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String: Any] {
                        let type = jsonMessage["type"] as? String
                        switch type {
                        case "doStartCall":
                            debugPrint("doStartCall")
                            if webSocketClient != nil {
                                webSocketClient.close()
                            }
                            webSocketClient = webSocket
                            self.delegate?.sendOffer(webSocketClient)
                        case "offer":
                            debugPrint("offer")
                            let sdp = jsonMessage["sdp"] as? String ?? ""
                            self.delegate?.receiveOffer(webSocketClient, RTCSessionDescription(type: .offer, sdp: sdp))
                        case "answer":
                            debugPrint("answer")
                            let sdp = jsonMessage["sdp"] as? String ?? ""
                            self.delegate?.receiveAnswer(RTCSessionDescription(type: .answer, sdp: sdp))
                        case "candidate":
                            debugPrint("candidate")
                            self.delegate?.candidate(jsonMessage)
                        case .none:
                            debugPrint("The type is invalid: \(String(describing: type))")
                        case .some(_):
                            debugPrint("The type is : \(String(describing: type))")
                        }
                    }
                } catch {
                    
                }
            }
        }
    }
    
    func server(_ server: PSWebSocketServer!, webSocket: PSWebSocket!, didFailWithError error: (any Error)!) {
        debugPrint("WebSocket connection failed with error: \(error.localizedDescription)")
    }
    
    func server(_ server: PSWebSocketServer!, webSocket: PSWebSocket!, didCloseWithCode code: Int, reason: String!, wasClean: Bool) {
        debugPrint("WebSocket connection closed with code: \(code), reason: \(String(describing: reason))")
    }

}
