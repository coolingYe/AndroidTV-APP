//
//  NetworkUtils.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/16.
//

import Foundation
import SystemConfiguration
import Network

class NetworkMonitor: ObservableObject {
    
    static let shared = NetworkMonitor()
    
    private var monitor: NWPathMonitor
    private var queue = DispatchQueue.global(qos: .background)
    
    @Published var isConnected: Bool = false
    @Published var isConnectedToWiFi: Bool = false
    
    private init() {
        self.monitor = NWPathMonitor()
        self.monitor.pathUpdateHandler = { path in
            DispatchQueue.main.async {
                self.isConnected = path.status == .satisfied
                self.isConnectedToWiFi = path.usesInterfaceType(.wifi)
            }
        }
    }
    
    func startMonitoring() {
        monitor.start(queue: queue)
    }
    
    func stopMonitoring() {
        monitor.cancel()
    }
}

extension NetworkMonitor {
    static func getLocalIPAddress() -> String? {
        var address: String?
        var ifaddr: UnsafeMutablePointer<ifaddrs>?
        
        if getifaddrs(&ifaddr) == 0 {
            var ptr = ifaddr
            while ptr != nil {
                let interface = ptr!.pointee
                let addrFamily = interface.ifa_addr.pointee.sa_family
                
                if addrFamily == UInt8(AF_INET) || addrFamily == UInt8(AF_INET6) {
                    if let name = String(validatingUTF8: interface.ifa_name), name == "en0" {
                        var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                        if getnameinfo(interface.ifa_addr, socklen_t(interface.ifa_addr.pointee.sa_len), &hostname, socklen_t(hostname.count), nil, socklen_t(0), NI_NUMERICHOST) == 0 {
                            address = String(cString: hostname)
                        }
                    }
                }
                
                ptr = interface.ifa_next
            }
            freeifaddrs(ifaddr)
        }
        
        return address
    }
    
    static func getLocalPort() -> UInt16? {
        let listener: NWListener
        do {
            listener = try NWListener(using: .tcp)
        } catch {
            print("Failed to create listener: \(error)")
            return nil
        }
        
        listener.newConnectionHandler = { connection in
            connection.start(queue: .main)
        }
        
        listener.start(queue: .main)
        
        return listener.port?.rawValue
    }
    
}
