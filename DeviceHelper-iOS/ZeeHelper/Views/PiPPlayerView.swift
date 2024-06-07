//
//  PlayerView.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/21.
//

import SwiftUI
import WebRTC
import AVKit

struct PiPPlayerView: UIViewControllerRepresentable {
    var videoTrack: RTCVideoTrack?
    @Binding var isPictureInPicture: Bool
    
    func makeUIViewController(context: Context) -> UIViewController {
        print("画中画初始化前：\(UIApplication.shared.windows)")
        
        let playerViewController = UIViewController()
        let player = AVPlayer(url: URL(string: "https://media.w3.org/2010/05/sintel/trailer.mp4")!)
        let playerLayer = AVPlayerLayer(player: player)
        playerLayer.frame = .init(x: 90, y: 90, width: 200, height: 150)
        player.isMuted = true
        player.allowsExternalPlayback = true
        player.play()
        
        playerViewController.view.layer.addSublayer(playerLayer)
        
        if let videoTrack = videoTrack {
            addVideoTrack(videoTrack, to: playerViewController)
        }
        
        if AVPictureInPictureController.isPictureInPictureSupported() {
            do {
                try AVAudioSession.sharedInstance().setCategory(.playback, options: .mixWithOthers)
            } catch {
                print(error)
            }
            
            if let pictureInPictureController = AVPictureInPictureController(playerLayer: playerLayer) {
                pictureInPictureController.delegate = context.coordinator
                pictureInPictureController.setValue(1, forKey: "controlsStyle")
                if #available(iOS 14.2, *) {
                    pictureInPictureController.canStartPictureInPictureAutomaticallyFromInline = true
                } else {
                    // Fallback on earlier versions
                }
                context.coordinator.pipController = pictureInPictureController
            }
            
        } else {
            debugPrint("Cannot support to PIP")
        }
        
        //        UIApplication.shared.beginBackgroundTask {
        //            UIApplication.shared.endBackgroundTask(UIBackgroundTaskIdentifier.invalid)
        //        }
        
        return playerViewController
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // 清除现有的子视图
        for subview in uiViewController.view.subviews {
            subview.removeFromSuperview()
        }
        
        // 重新添加视频轨道视图
        if let videoTrack = videoTrack {
            addVideoTrack(videoTrack, to: uiViewController)
        }
        
        
        let player = AVPlayer(url: URL(string: "https://media.w3.org/2010/05/sintel/trailer.mp4")!)
        let playerLayer = AVPlayerLayer(player: player)
        playerLayer.frame = .init(x: 90, y: 90, width: 200, height: 150)
        player.isMuted = true
        player.allowsExternalPlayback = true
        player.play()
        
        uiViewController.view.layer.addSublayer(playerLayer)
        
        if AVPictureInPictureController.isPictureInPictureSupported() {
            do {
                try AVAudioSession.sharedInstance().setCategory(.playback, options: .mixWithOthers)
            } catch {
                print(error)
            }
            
            if let pictureInPictureController = AVPictureInPictureController(playerLayer: playerLayer) {
                pictureInPictureController.delegate = context.coordinator
                pictureInPictureController.setValue(1, forKey: "controlsStyle")
                if #available(iOS 14.2, *) {
                    pictureInPictureController.canStartPictureInPictureAutomaticallyFromInline = true
                } else {
                    // Fallback on earlier versions
                }
                context.coordinator.pipController = pictureInPictureController
            }
            
        } else {
            debugPrint("Cannot support to PIP")
        }
        
        if isPictureInPicture {
            context.coordinator.pipController?.startPictureInPicture()
        } else {
            context.coordinator.pipController?.stopPictureInPicture()
        }
    }
    
    private func addVideoTrack(_ videoTrack: RTCVideoTrack, to playerViewController: UIViewController) {
        guard let overlayView = playerViewController.view else {return}
        let videoView = RTCEAGLVideoView(frame: overlayView.bounds)
        videoView.contentMode = .scaleAspectFit
        videoTrack.add(videoView)
        overlayView.addSubview(videoView)
    }
    
    private func addVideoTrack(_ videoTrack: RTCVideoTrack, to window: UIWindow) {
        let videoView = RTCEAGLVideoView(frame: window.bounds)
        videoView.contentMode = .scaleAspectFit
        videoTrack.add(videoView)
        window.addSubview(videoView)
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, AVPictureInPictureControllerDelegate {
        var parent: PiPPlayerView
        var pipController: AVPictureInPictureController?
        
        init(_ parent: PiPPlayerView) {
            self.parent = parent
        }
        
        func pictureInPictureControllerWillStartPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
            debugPrint("PIP ready")
            if let window = UIApplication.shared.windows.first {
                if let videoTrack = parent.videoTrack {
                    parent.addVideoTrack(videoTrack, to: window)
                }
            }
        }
        
        func pictureInPictureControllerDidStartPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
            debugPrint("PIP start")
        }
        
        func pictureInPictureControllerDidStopPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
            debugPrint("PIP end")
        }
        
        func pipButtonClicked() {
            if ((pipController?.isPictureInPictureActive) != nil) {
                pipController?.stopPictureInPicture()
            } else {
                pipController?.startPictureInPicture()
            }
        }
    }
}

struct FloatingWindow: UIViewControllerRepresentable {
    @Binding var isPictureInPicture: Bool
    let videoTrack: RTCVideoTrack?
    
    func makeUIViewController(context: Context) -> PiPViewController {
        let pipView = PiPViewController()
        //        pipView.setWebRTCVideo(videoTrack: videoTrack!)
        return pipView
    }
    
    func updateUIViewController(_ uiViewController: PiPViewController, context: Context) {
        // Here you can add code to update the view controller if needed
        
        //        uiViewController.setWebRTCVideo(videoTrack: videoTrack!)
        uiViewController.videoTrack = videoTrack
        
        if isPictureInPicture {
            uiViewController.pipButtonClicked()
        } else {
            uiViewController.pipButtonClicked()
        }
    }
}
