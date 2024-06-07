//
//  ZeeHelperApp.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/6.
//

import SwiftUI
import AVFAudio

@main
struct ZeeHelperApp: App {
    @StateObject private var networkMonitor = NetworkMonitor.shared
    private let localNetworkPermissionRequester = LocalNetworkPermission()
    //    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        WindowGroup {
            MainView()
                .environmentObject(networkMonitor)
                .onAppear {
                    networkMonitor.startMonitoring()
                    initDevice()
                }
                .onDisappear{
                    networkMonitor.stopMonitoring()
                }
        }
    }
}

extension ZeeHelperApp {
    func initDevice() {
        localNetworkPermissionRequester.requestAuthorization(completion: {
            isAuth in
        })
        
        let defaults = UserDefaults.standard
        let format = defaults.string(forKey: "format")
        if format == nil {
            defaults.set("720P", forKey: "format")
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        // 配置音频会话以支持后台模式
        configureAudioSession()
        return true
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        // 在进入后台时执行的代码
        configureAudioSession()
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        // 在进入前台时执行的代码
        configureAudioSession()
    }
    
    private func configureAudioSession() {
        do {
            let audioSession = AVAudioSession.sharedInstance()
            try audioSession.setCategory(.playAndRecord, options: [.allowBluetooth, .defaultToSpeaker, .allowBluetoothA2DP])
            try audioSession.setActive(true)
        } catch {
            print("Failed to configure audio session: \(error)")
        }
    }
}
