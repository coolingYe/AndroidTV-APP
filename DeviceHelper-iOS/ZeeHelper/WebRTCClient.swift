//
//  WebRTCClient.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/6.
//

import Foundation
import WebRTC
import PocketSocket

class WebRTCClient: NSObject, ObservableObject {
    
    private static let factory: RTCPeerConnectionFactory = {
        RTCInitializeSSL()
        let videoEncoderFactory = RTCDefaultVideoEncoderFactory()
        let videoDecoderFactory = RTCDefaultVideoDecoderFactory()
        return RTCPeerConnectionFactory(encoderFactory: videoEncoderFactory, decoderFactory: videoDecoderFactory)
    }()
    
    private var peerConnection: RTCPeerConnection?
    private let mediaConstrains = [kRTCMediaConstraintsOfferToReceiveAudio: kRTCMediaConstraintsValueTrue,
                                   kRTCMediaConstraintsOfferToReceiveVideo: kRTCMediaConstraintsValueTrue]
    private var localVideoSource: RTCVideoSource?
    private var dataChannel: RTCDataChannel?
    var localVideoTrack: RTCVideoTrack?
    var videoCapturer: RTCCameraVideoCapturer?
    var localDataChannel: RTCDataChannel?
    var signalServer: SignalServer?
    var againCallClient: AgainCallClient?
    var remoteAddress: String
    private var isFrontCamera = true
    
    var receiveListener: WebRTCInterface?
    
    enum ConnectionState {
        case Connected
        case DisConnected
        case Connecting
    }
    
    init(remoteAddress: String) {
        self.remoteAddress = remoteAddress
        super.init()
        
        self.signalServer = SignalServer()
        self.signalServer?.delegate = self
        
        let config = RTCConfiguration()
        config.sdpSemantics = RTCSdpSemantics.unifiedPlan
        config.continualGatheringPolicy = RTCContinualGatheringPolicy.gatherOnce
        let constraints = RTCMediaConstraints(mandatoryConstraints: nil, optionalConstraints: ["DtlsSrtpKeyAgreement" : "true"])
        
        self.peerConnection = WebRTCClient.factory.peerConnection(with: config, constraints: constraints, delegate: self)
        self.createMediaSenders()
        self.againCallClient = AgainCallClient()
        self.againCallClient?.connect(to: self.remoteAddress)
    }
    
    func offer(completion: @escaping (_ sdp: RTCSessionDescription) -> Void) {
        guard let peerConnection = peerConnection else {
            debugPrint("Check Peer connection")
            return
        }
        
        let constrains = RTCMediaConstraints(mandatoryConstraints: self.mediaConstrains,
                                             optionalConstraints: nil)
        peerConnection.offer(for: constrains) { (sdp, error) in
            guard let sdp = sdp else {
                return
            }
            
            peerConnection.setLocalDescription(sdp, completionHandler: { (error) in
                completion(sdp)
            })
        }
    }
    
    func createOffer(conn: PSWebSocket) {
        guard let peerConnection = peerConnection else {
            debugPrint("Check Peer connection")
            return
        }
        
        let constraints = RTCMediaConstraints(mandatoryConstraints: nil, optionalConstraints: nil)
        peerConnection.offer(for: constraints) { (sdp, error) in
            guard let sdp = sdp else {
                print("Failed to create offer: \(String(describing: error))")
                return
            }
            peerConnection.setLocalDescription(sdp) { (error) in
                if let error = error {
                    print("Failed to set local description: \(error)")
                    return
                }
                let message = [
                    "type": "offer",
                    "sdp": sdp.sdp
                ]
                if let jsonData = try? JSONSerialization.data(withJSONObject: message, options: []),
                   let jsonString = String(data: jsonData, encoding: .utf8) {
                    conn.send(jsonString)
                }
            }
        }
    }
    
    func answer(completion: @escaping (_ sdp: RTCSessionDescription) -> Void)  {
        guard let peerConnection = peerConnection else {
            debugPrint("Check Peer connection")
            return
        }
        
        let constrains = RTCMediaConstraints(mandatoryConstraints: self.mediaConstrains,
                                             optionalConstraints: nil)
        peerConnection.answer(for: constrains) { (sdp, error) in
            guard let sdp = sdp else {
                return
            }
            
            peerConnection.setLocalDescription(sdp, completionHandler: { (error) in
                completion(sdp)
            })
        }
    }
    
    private func createMediaSenders() {
        guard let peerConnection = peerConnection else {
            debugPrint("Check Peer connection")
            return
        }
        
        let mediaTrackStreamIDs = "ARDAMS"
        
        let videoTrack = createVideoTrack()
        localVideoTrack = videoTrack
        peerConnection.add(videoTrack, streamIds: [mediaTrackStreamIDs])
        
        //        let audioTrack = self.createAudioTrack()
        //        peerConnection.add(audioTrack, streamIds: [mediaTrackStreamIDs])
        
        if let dataChannel = createDataChannel() {
            dataChannel.delegate = self
            localDataChannel = dataChannel
        }
    }
    
    private func createAudioTrack() -> RTCAudioTrack {
        let audioConstrains = RTCMediaConstraints(mandatoryConstraints: [:], optionalConstraints: nil)
        let audioSource = WebRTCClient.factory.audioSource(with: audioConstrains)
        let audioTrack = WebRTCClient.factory.audioTrack(with: audioSource, trackId: "ARDAMSa0")
        return audioTrack
    }
    
    private func createVideoTrack() -> RTCVideoTrack {
        let videoSource = WebRTCClient.factory.videoSource()
        localVideoSource = videoSource
//        if UserDefaults.standard.string(forKey: "format") == "360P" {
//            localVideoSource?.adaptOutputFormat(toWidth: 360, height: 480, fps: 30)
//        } else {
//            localVideoSource?.adaptOutputFormat(toWidth: 720, height: 1280, fps: 30)
//        }
        videoCapturer = RTCCameraVideoCapturer(delegate: localVideoSource!)
        startCaptureLocalVideo()
        return WebRTCClient.factory.videoTrack(with: localVideoSource!, trackId: "ARDAMSv0")
    }
    
    private func createDataChannel() -> RTCDataChannel? {
        guard let peerConnection = peerConnection else {
            debugPrint("Check Peer connection")
            return nil
        }
        
        let config = RTCDataChannelConfiguration()
        guard let dataChannel = peerConnection.dataChannel(forLabel: "data", configuration: config) else {
            debugPrint("Warning: Couldn't create data channel.")
            return nil
        }
        return dataChannel
    }
    
    func startCaptureLocalVideo() {
        guard let capturer = videoCapturer else {
            return
        }
        guard let data = UserDefaults.standard.string(forKey: "format") else {return}
        
        guard
            let frontCamera = (RTCCameraVideoCapturer.captureDevices().first { $0.position == .front }),
            
//                        let format = (RTCCameraVideoCapturer.supportedFormats(for: frontCamera).sorted { (f1, f2) -> Bool in
//                                let width1 = CMVideoFormatDescriptionGetDimensions(f1.formatDescription).width
//                                let width2 = CMVideoFormatDescriptionGetDimensions(f2.formatDescription).width
//                                return width1 < width2
//                            }).last,
                
//                let format = (RTCCameraVideoCapturer.supportedFormats(for: frontCamera)).last,
                
                let format = if data == "360P" {
                    VideoResolution.H360.bestFormat(for: frontCamera)
                } else {
                    VideoResolution.H720.bestFormat(for: frontCamera)
                },
        
        let fps = (format.videoSupportedFrameRateRanges.sorted { return $0.maxFrameRate < $1.maxFrameRate }.last) else {
            return
        }
        
        capturer.startCapture(with: frontCamera,
                              format: format,
                              fps: Int(fps.maxFrameRate))
    }
    
    func switchCamera() {
        guard let data = UserDefaults.standard.string(forKey: "format") else {return}
        
        if let frontCamera = RTCCameraVideoCapturer.captureDevices().first(where: { $0.position == .front }),
           let backCamera = RTCCameraVideoCapturer.captureDevices().first(where: { $0.position == .back }) {
            isFrontCamera = !isFrontCamera
            let newCamera = (!isFrontCamera) ? backCamera : frontCamera
            guard
                let format = if data == "360P" {
                    VideoResolution.H360.bestFormat(for: frontCamera)
                } else {
                    VideoResolution.H720.bestFormat(for: frontCamera)
                },
            let fps = (format.videoSupportedFrameRateRanges.sorted { return $0.maxFrameRate < $1.maxFrameRate }.last) else {
                return
            }
            self.videoCapturer?.stopCapture {
                self.videoCapturer?.startCapture(with: newCamera, format: format, fps: Int(fps.maxFrameRate))
            }
        }
    }
    
    func changeVideoFormat(to resolution: VideoResolution) {
        if let frontCamera = RTCCameraVideoCapturer.captureDevices().first(where: { $0.position == .front }),
           let backCamera = RTCCameraVideoCapturer.captureDevices().first(where: { $0.position == .back }) {
            let currentCamera = (!isFrontCamera) ? backCamera : frontCamera
            guard let videoCapturer = videoCapturer else { return }
            
            guard let format = resolution.bestFormat(for: currentCamera) else {
                print("No valid format found for resolution: \(resolution)")
                return
            }
            
            let fps = resolution.bestFps(for: format)
            
            videoCapturer.stopCapture {
                videoCapturer.startCapture(with: currentCamera, format: format, fps: fps)
                
                self.localVideoTrack = WebRTCClient.factory.videoTrack(with: self.localVideoSource!, trackId: "ARDAMSv0")
                
                DispatchQueue.main.async {
                    self.objectWillChange.send()
                }
            }
        }
    }
}

enum VideoResolution {
    case H360
    case H720
    
    func bestFormat(for device: AVCaptureDevice) -> AVCaptureDevice.Format? {
        let formats = RTCCameraVideoCapturer.supportedFormats(for: device)
        switch self {
        case .H360:
            return formats.first { $0.formatDescription.dimensions.width == 480 && $0.formatDescription.dimensions.height == 360 }
        case .H720:
            return formats.first { $0.formatDescription.dimensions.width == 1280 && $0.formatDescription.dimensions.height == 720 }
        }
    }
    
    func bestFps(for format: AVCaptureDevice.Format) -> Int {
        return Int(format.videoSupportedFrameRateRanges.first?.maxFrameRate ?? 30)
    }
}

extension WebRTCClient {
    
    func checkConnect() -> Bool {
        if peerConnection != nil {
            if peerConnection?.connectionState == .connected {
                return true
            }
        }
        return false
    }
    
    func cancal() {
        if videoCapturer != nil {
            videoCapturer?.stopCapture()
            videoCapturer = nil
        }
        
        if localVideoSource != nil {
            localVideoSource = nil
        }
        
        if localVideoTrack != nil {
            localVideoTrack = nil
        }
        
        if peerConnection != nil {
            peerConnection?.close()
            peerConnection = nil
        }
        
        if signalServer != nil {
            signalServer?.server.stop()
        }
        
        if againCallClient != nil {
            againCallClient?.disconnect()
        }
    }
}

extension WebRTCClient: RTCPeerConnectionDelegate {
    
    func peerConnection(_ peerConnection: RTCPeerConnection, didChange stateChanged: RTCSignalingState) {
        debugPrint("peerConnection new signaling state: \(stateChanged)")
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection, didAdd stream: RTCMediaStream) {
        debugPrint("peerConnection did add stream")
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection, didRemove stream: RTCMediaStream) {
        debugPrint("peerConnection did remove stream")
    }
    
    func peerConnectionShouldNegotiate(_ peerConnection: RTCPeerConnection) {
        debugPrint("peerConnection should negotiate")
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection, didChange newState: RTCIceConnectionState) {
        debugPrint("peerConnection new connection state: \(newState)")
        receiveListener?.receiveText(newState.rawValue)
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection, didChange newState: RTCIceGatheringState) {
        debugPrint("peerConnection new gathering state: \(newState)")
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection, didGenerate candidate: RTCIceCandidate) {
        debugPrint("peerConnection did generate candidate")
        
        let message = [
            "type": "candidate",
            "label": candidate.sdpMLineIndex,
            "id": candidate.sdpMid!,
            "candidate": candidate.sdp
        ] as [String : Any]
        
        if let jsonData = try? JSONSerialization.data(withJSONObject: message, options: []),
           let jsonString = String(data: jsonData, encoding: .utf8) {
            self.signalServer?.webSocketClient.send(jsonData)
        }
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection, didRemove candidates: [RTCIceCandidate]) {
        debugPrint("peerConnection did remove candidate(s)")
        self.peerConnection?.remove(candidates)
    }
    
    func peerConnection(_ peerConnection: RTCPeerConnection, didOpen dataChannel: RTCDataChannel) {
        debugPrint("peerConnection did open data channel")
        //self.remoteDataChannel = dataChannel
    }
}

extension WebRTCClient: SignalServerInterface {
    
    func sendOffer(_ conn: PSWebSocket) {
        self.offer(completion: { localSdp in
            let message = [
                "type": "offer",
                "sdp": localSdp.sdp
            ]
            if let jsonData = try? JSONSerialization.data(withJSONObject: message, options: []),
               let jsonString = String(data: jsonData, encoding: .utf8) {
                conn.send(jsonString)
            }
        })
    }
    
    func receiveOffer(_ conn: PSWebSocket, _ desc: RTCSessionDescription) {
        guard let peerConnection = peerConnection else {
            debugPrint("Check Peer connection")
            return
        }
        
        peerConnection.setRemoteDescription(desc, completionHandler: { (error) in
            if let error = error {
                debugPrint("Failed to set remote description: \(error)")
            }
            
            if desc.type == .offer, self.peerConnection?.localDescription == nil {
                self.answer(completion: { localSdp in
                    let message = [
                        "type": "answer",
                        "sdp": localSdp.sdp
                    ]
                    if let jsonData = try? JSONSerialization.data(withJSONObject: message, options: []),
                       let jsonString = String(data: jsonData, encoding: .utf8) {
                        conn.send(jsonString)
                    }
                })
            }
        })
    }
    
    func receiveAnswer(_ desc: RTCSessionDescription) {
        guard let peerConnection = peerConnection else {
            debugPrint("Check Peer connection")
            return
        }
        
        peerConnection.setRemoteDescription(desc, completionHandler: { (error) in
            if let error = error {
                debugPrint("Failed to set remote description: \(error)")
            }
        })
    }
    
    func candidate(_ message: [String : Any]) {
        guard let peerConnection = peerConnection else {
            debugPrint("Check Peer connection")
            return
        }
        
        let sdp = message["candidate"] as? String ?? ""
        let label = message["lable"] as? Int32 ?? 0
        let id = message["id"] as? String ?? ""
        let iceCandidate = RTCIceCandidate(sdp: sdp, sdpMLineIndex: label, sdpMid: id)
        
        peerConnection.add(iceCandidate)
    }
    
}

extension WebRTCClient: RTCDataChannelDelegate {
    func dataChannelDidChangeState(_ dataChannel: RTCDataChannel) {
        debugPrint("dataChannel did change state: \(dataChannel.readyState)")
    }
    
    func dataChannel(_ dataChannel: RTCDataChannel, didReceiveMessageWith buffer: RTCDataBuffer) {
    }
}

protocol WebRTCInterface {
    func receiveText(_ state: Int)
}
