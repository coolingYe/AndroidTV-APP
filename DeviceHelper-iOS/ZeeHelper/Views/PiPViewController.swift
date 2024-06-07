//
//  PiPViewController.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/23.
//

import UIKit
import AVKit
import SnapKit
import WebRTC

class PiPViewController: UIViewController, AVPictureInPictureControllerDelegate {
    
    private var playerLayer: AVPlayerLayer!
    var pipController: AVPictureInPictureController!
    var customView: UIView!
    var videoTrack: RTCVideoTrack!
    private var player: AVQueuePlayer!
    private var playerLooper: AVPlayerLooper!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if AVPictureInPictureController.isPictureInPictureSupported() {
            do {
                try AVAudioSession.sharedInstance().setCategory(.playback, options: .mixWithOthers)
            } catch {
                print(error)
            }
            setupPlayer()
            setupPip()
            setupCustomView()
            NotificationCenter.default.addObserver(self, selector: #selector(handleEnterForeground), name: UIApplication.willEnterForegroundNotification, object: nil)
            NotificationCenter.default.addObserver(self, selector: #selector(handleEnterBackground), name: UIApplication.didEnterBackgroundNotification, object: nil)
        } else {
            print("不支持画中画")
        }
        
        //        UIApplication.shared.beginBackgroundTask {
        //            UIApplication.shared.endBackgroundTask(UIBackgroundTaskIdentifier.invalid)
        //        }
    }
    
    private func setupPlayer() {
        
        
        let mp4Video = Bundle.main.url(forResource: "vertical_full_screen", withExtension: "mp4")
        let asset = AVAsset.init(url: mp4Video!)
        let playerItem = AVPlayerItem.init(asset: asset)
        //let playerItem = AVPlayerItem.init(url: URL(string: "https://media.w3.org/2010/05/sintel/trailer.mp4")!)
        //let playerItem = AVPlayerItem.init(url: URL(string: "about:blank")!)
        
        player = AVQueuePlayer()
        playerLooper = AVPlayerLooper(player: player, templateItem: playerItem)
        
        playerLayer = AVPlayerLayer(player: player)
        playerLayer.frame = view.bounds
        playerLayer.videoGravity = .resizeAspectFill
        
        player.isMuted = true
        player.allowsExternalPlayback = true
        player.play()
        
        
        //        let player = AVPlayer.init(playerItem: playerItem)
        //        playerLayer.player = player
        //        player.isMuted = true
        //        player.allowsExternalPlayback = true
        //        player.play()
        
        view.layer.addSublayer(playerLayer)
    }
    
    func setWebRTCVideo(videoTrack: RTCVideoTrack) {
        let videoView = RTCEAGLVideoView(frame: view.bounds)
        videoView.contentMode = .scaleAspectFit
        self.videoTrack = videoTrack
        self.videoTrack.add(videoView)
        view.addSubview(videoView)
    }
    
    private func setupPip() {
        pipController = AVPictureInPictureController.init(playerLayer: playerLayer)!
        pipController.delegate = self
        pipController.setValue(1, forKey: "controlsStyle")
        if #available(iOS 14.2, *) {
            pipController.canStartPictureInPictureAutomaticallyFromInline = true
        } else {
            // Fallback on earlier versions
        }
    }
    
    // 配置自定义view
    private func setupCustomView() {
        customView = UIView()
        customView.backgroundColor = .white
        let videoView = RTCEAGLVideoView(frame: view.bounds)
        videoView.contentMode = .scaleAspectFit
        guard let videoTrack = videoTrack else {
            return
        }
        videoTrack.add(videoView)
        customView.addSubview(videoView)
    }
    
    @objc func pipButtonClicked() {
        if pipController.isPictureInPictureActive {
            pipController.stopPictureInPicture()
        } else {
            pipController.startPictureInPicture()
        }
    }
    
    @objc private func handleEnterForeground() {
        print("进入前台");
    }
    
    @objc private func handleEnterBackground() {
        print("进入后台");
    }
    
    // 画中画将要弹出
    func pictureInPictureControllerWillStartPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
        print("画中画初始化后")
        // 注意是 first window
        if let window = UIApplication.shared.windows.first {
            // 把自定义view加到画中画上
            setupCustomView()
            window.addSubview(customView)
            // 使用自动布局
            customView.snp.makeConstraints { (make) -> Void in
                make.edges.equalToSuperview()
            }
        }
    }
    
    func pictureInPictureControllerDidStartPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
        print("画中画弹出后")
    }
    
    func pictureInPictureControllerDidStopPictureInPicture(_ pictureInPictureController: AVPictureInPictureController) {
        
    }
}
