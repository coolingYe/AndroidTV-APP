//
//  VideoCallViewModel.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/6.
//

import Foundation
import SwiftUI
import WebRTC
import AVFoundation
import Network

class VideoCallViewModel : ObservableObject {
    @Published var isVideoPagePresented: Bool = false
    @Published var isQRScanPagePresented: Bool = false
    @Published var isCameraAuthorized = false
    @Published var isCameraDenied = false
    @Published var isRequestingPermission = false
    private var browser: NWBrowser?
    
    func checkCameraAuthorizationStatus() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .notDetermined:
            isRequestingPermission = true
            requestCameraAccess()
        case .restricted, .denied:
            isCameraDenied = true
        case .authorized:
            isCameraAuthorized = true
        @unknown default:
            break
        }
    }
    
    private func requestCameraAccess() {
        AVCaptureDevice.requestAccess(for: .video) { granted in
            DispatchQueue.main.async {
                self.isRequestingPermission = false
                if granted {
                    self.isCameraAuthorized = true
                } else {
                    self.isCameraDenied = true
                }
            }
        }
    }
}
