//
//  VideoView.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/6.
//

import SwiftUI
import WebRTC

struct VideoView: UIViewRepresentable {
    let videoTrack: RTCVideoTrack?
    //RTCNSGLVideoView
    //RTCMTLNSVideoView
    func makeUIView(context: Context) -> RTCMTLVideoView {
        let view = RTCMTLVideoView(frame: .zero)
        view.videoContentMode = .scaleAspectFit
        view.transform = CGAffineTransform(scaleX: -1.0, y: 1.0)
        return view
    }
    
    func updateUIView(_ view: RTCMTLVideoView, context: Context) {
        videoTrack?.add(view)
    }
    
}
