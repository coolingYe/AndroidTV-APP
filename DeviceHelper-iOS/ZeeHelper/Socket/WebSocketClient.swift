//
//  WebSocketClient.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/11.
//

import Foundation
import Starscream
import PocketSocket

protocol ClientInterface {
    func receiveText(_ remoteAddress: String)
}

protocol SocketOpenListener {
    func openCallback()
}

protocol SocketCancelledListener {
    func cancelcallback()
}


class FirstCallClient: NSObject, ObservableObject {
    
    @Published var isConnected = false
    @Published var receivedMessage: String?
    @Published var connectionStatus = "Disconnected"
    var receiveListener: ClientInterface?
    
    private var socket: WebSocket?
    private var timeoutTask: DispatchWorkItem?
    
    func connect(to url: String) {
        guard let url = URL(string: url) else {
                    print("Invalid URL")
                    return
                }
        
        var request = URLRequest(url: url)
        request.timeoutInterval = 5
        socket = WebSocket(request: request)
        socket?.delegate = self
        socket?.connect()
        
        timeoutTask = DispatchWorkItem { [weak self] in
                    guard let self = self else { return }
                    if !self.isConnected {
                        self.connectionStatus = "Failed"
                        print("WebSocket Connection timed out")
                        self.socket?.disconnect()
                    }
                }
        
        if let timeoutTask = timeoutTask {
            DispatchQueue.main.asyncAfter(deadline: .now() + 5, execute: timeoutTask)
        }
    }
    
    func disconnect() {
        timeoutTask?.cancel()
        socket?.disconnect()
    }
    
    func sendMessage(_ message: String) {
        socket?.write(string: message)
    }
    
    func sendMessage(_ data: Data) {
        socket?.write(data: data)
    }
}

extension FirstCallClient: WebSocketDelegate {
    func didReceive(event: Starscream.WebSocketEvent, client: Starscream.WebSocketClient) {
        switch event {
        case .connected(let headers):
            isConnected = true
            let jsonStr: [String: Any] = [
                "type": "WebrtcServerInfo",
                "ip": NetworkMonitor.getLocalIPAddress() ?? "192.168.30.172",
                "port": NetworkMonitor.getLocalPort() ?? 0
            ]
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: jsonStr, options: [])
                if let jsonString = String(data: jsonData, encoding: .utf8) {
                    sendMessage(jsonString)
                }
            } catch {
                print("Failed to create JSON: \(error.localizedDescription)")
            }
            print("websocket is connected: \(headers)")
            connectionStatus = "Connected"
        case .disconnected(let reason, let code):
            isConnected = false
            print("websocket is disconnected: \(reason) with code: \(code)")
        case .text(let string):
            receivedMessage = string
            if let jsonData = string.data(using: .utf8) {
                do {
                    if let jsonMessage = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String: Any] {
                        let type = jsonMessage["type"] as? String
                        if type == "WebrtcServerInfo" {
                            disconnect()
                            receiveListener?.receiveText(socket?.request.url?.host ?? "")
                        }
                    }
                } catch {
                    
                }
            }
            print("Received text: \(string)")
        case .binary(let data):
            print("Received data: \(data.count) bytes")
        case .ping(_):
            print("websocket ping")
            break
        case .pong(_):
            print("websocket pong")
            break
        case .viabilityChanged(_):
            break
        case .reconnectSuggested(_):
            break
        case .cancelled:
            print("websocket cancelled")
            isConnected = false
        case .error(let error):
            isConnected = false
            handleError(error)
            connectionStatus = "Failed"
            print("websocket error")
        case .peerClosed:
            isConnected = false
            print("websocket peerClosed")
        }
    }
    
    private func handleError(_ error: Error?) {
        if let e = error as? WSError {
            print("websocket encountered an error: \(e.message)")
        } else if let e = error {
            print("websocket encountered an error: \(e.localizedDescription)")
        } else {
            print("websocket encountered an error")
        }
    }
}

class AgainCallClient: NSObject {
    
    @Published var isConnected = false
    @Published var receivedMessage: String?
    var remoteIp: String = ""
    var receiveListener: ClientInterface?
    
    private var socket: WebSocket?
    
    func connect(to url: String) {
        guard let url = URL(string: url) else {
                    print("Invalid URL")
                    return
                }
        self.remoteIp = url.scheme ?? "null"
        var request = URLRequest(url: url)
        request.timeoutInterval = 5
        socket = WebSocket(request: request)
        socket?.delegate = self
        socket?.connect()
    }
    
    func disconnect() {
        socket?.disconnect()
    }
    
    func sendMessage(_ message: String) {
        socket?.write(string: message)
    }
    
    func sendMessage(_ data: Data) {
        socket?.write(data: data)
    }
}

extension AgainCallClient: WebSocketDelegate {
    func didReceive(event: Starscream.WebSocketEvent, client: Starscream.WebSocketClient) {
        switch event {
        case .connected(let headers):
            isConnected = true
            let jsonStr: [String: Any] = [
                "type": "ZeeDevHelperClientInfo",
                "ip": NetworkMonitor.getLocalIPAddress() ?? "192.168.30.172",
                "port": 36901,
                "deviceName": getDeviceName()
            ]
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: jsonStr, options: [])
                if let jsonString = String(data: jsonData, encoding: .utf8) {
                    sendMessage(jsonString)
                }
            } catch {
                print("Failed to create JSON: \(error.localizedDescription)")
            }
            print("websocket is connected: \(headers)")
        case .disconnected(let reason, let code):
            isConnected = false
            print("websocket is disconnected: \(reason) with code: \(code)")
        case .text(let string):
            receivedMessage = string
//            if let jsonData = string.data(using: .utf8) {
//                do {
//                    if let jsonMessage = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String: Any] {
//                        let type = jsonMessage["type"] as? String
//                        if type == "WebrtcServerInfo" {
//                            if socket?.request.url != nil {
//                                
//                            }
//                            disconnect()
//                            self.receiveListener?.receiveText()
//                        }
//                    }
//                } catch {
//                    
//                }
//            }
            print("Received text: \(string)")
        case .binary(let data):
            print("Received data: \(data.count) bytes")
        case .ping(_):
            break
        case .pong(_):
            break
        case .viabilityChanged(_):
            break
        case .reconnectSuggested(_):
            break
        case .cancelled:
            isConnected = false
        case .error(let error):
            isConnected = false
            handleError(error)
        case .peerClosed:
            isConnected = false
        }
    }
    
    private func handleError(_ error: Error?) {
        if let e = error as? WSError {
            print("websocket encountered an error: \(e.message)")
        } else if let e = error {
            print("websocket encountered an error: \(e.localizedDescription)")
        } else {
            print("websocket encountered an error")
        }
    }
    
    func getDeviceName() -> String {
        // Get the device name set by the user (similar to the Bluetooth name in Android).
        let deviceName = UIDevice.current.name
        
        // If the device name is empty, fallback to the device model name.
        if deviceName.isEmpty {
            return UIDevice.current.model
        } else {
            return deviceName
        }
    }

}

class MobileClient: NSObject {
    @Published var isConnected = false
    @Published var receivedMessage: String?
    
    var remoteIp: String = ""
    var cancelledListener: SocketCancelledListener?
    var actionCode: Int?
    
    private var socket: WebSocket?
    
    func connect(to url: String) {
        guard let url = URL(string: url) else {
                    print("Invalid URL")
                    return
                }
        self.remoteIp = url.scheme ?? "null"
        var request = URLRequest(url: url)
        request.timeoutInterval = 5
        socket = WebSocket(request: request)
        socket?.delegate = self
        socket?.connect()
    }
    
    func setMessage(_ actionCode: Int) {
        self.actionCode = actionCode
    }
    
    func disconnect() {
        socket?.disconnect()
    }
    
    func sendMessage(_ message: String) {
        socket?.write(string: message)
    }
    
    func sendMessage(_ data: Data) {
        socket?.write(data: data)
    }
}

extension MobileClient: WebSocketDelegate {
        
    func didReceive(event: Starscream.WebSocketEvent, client: Starscream.WebSocketClient) {
        switch event {
        case .connected(let headers):
            isConnected = true
            let jsonStr: [String: Any] = [
                "type": "Mobile",
                "action": actionCode ?? -1
            ]
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: jsonStr, options: [])
                if let jsonString = String(data: jsonData, encoding: .utf8) {
                    sendMessage(jsonString)
                }
            } catch {
                print("Failed to create JSON: \(error.localizedDescription)")
            }
            print("websocket is connected: \(headers)")
        case .disconnected(let reason, let code):
            isConnected = false
            print("websocket is disconnected: \(reason) with code: \(code)")
        case .text(let string):
            receivedMessage = string
            if let jsonData = string.data(using: .utf8) {
                do {
                    if let jsonMessage = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String: Any] {
                        let type = jsonMessage["type"] as? String
                        if type == "Mobile" {
                            disconnect()
                        }
                    }
                } catch {
                    
                }
            }
            print("Received text: \(string)")
        case .binary(let data):
            print("Received data: \(data.count) bytes")
        case .ping(_):
            print("websocket ping")
            break
        case .pong(_):
            print("websocket pong")
            break
        case .viabilityChanged(_):
            break
        case .reconnectSuggested(_):
            break
        case .cancelled:
            print("websocket cancelled")
            isConnected = false
            cancelledListener?.cancelcallback()
        case .error(let error):
            isConnected = false
            handleError(error)
            print("websocket error")
        case .peerClosed:
            isConnected = false
            print("websocket peerClosed")
        }
    }
    
    private func handleError(_ error: Error?) {
        if let e = error as? WSError {
            print("websocket encountered an error: \(e.message)")
        } else if let e = error {
            print("websocket encountered an error: \(e.localizedDescription)")
        } else {
            print("websocket encountered an error")
        }
    }
}


