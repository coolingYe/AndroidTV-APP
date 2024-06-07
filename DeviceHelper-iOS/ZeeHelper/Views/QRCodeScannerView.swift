//
//  ScanView.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/11.
//

import SwiftUI
import AVFoundation

struct QRCodeScannerView: View {
    @StateObject private var viewModel = VideoCallViewModel()
    @State private var isShowingScanner = false
    @State private var showAlert = false
    @State private var scannedCode: String?
    @State private var isAnimating = false
    @Binding var deviceData: Device
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        
        GeometryReader { geometry in
            ScannerView(scannedCode: $scannedCode, isShowingScanner: $isShowingScanner)
                .aspectRatio(contentMode: .fill)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            
            Image("icon_scan_line_bg").frame(height: 4)
                .offset(y: self.isAnimating ? geometry.size.height * 0.8 : 100)
                .onAppear() {
                    startAnimating()
                }
            
            ZStack {
                Image("icon_scan_bottom_bg").resizable()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .padding(.leading, -2).rotationEffect(.degrees(180))
                HStack {
                    Image("icon_back").resizable().frame(width: 25, height: 25).onTapGesture {
                        self.presentationMode.wrappedValue.dismiss()
                    }.padding(.leading, 16)
                    Spacer()
                    Text("扫一扫").font(.title2).foregroundColor(.white).padding(.trailing, 40)
                    Spacer()
                }.padding(.top, 30)
            }.frame(maxWidth: .infinity, maxHeight: 90)
            
            VStack {
                Spacer()
                ZStack {
                    Image("icon_scan_bottom_bg").resizable()
                        .frame(maxWidth: .infinity, maxHeight: 172)
                        .padding(.leading, -2)
                    VStack {
                        
                        Text("扫码关联盒子：").font(.title3)
                            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading).foregroundColor(.white)
                        Text("1、在“把视互动平台”点击菜单栏").font(.subheadline)
                            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading).foregroundColor(.white)
                        Text("2、选择“设备连接”").font(.subheadline)
                            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading).foregroundColor(.white)
                        Text("3、扫描电视屏幕中的二维码并确认连接").font(.subheadline)
                            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading).foregroundColor(.white)
                    }.padding(.leading, 10).padding(.bottom, 40)
                }
            }
        }
        .edgesIgnoringSafeArea(.all)
        .navigationBarHidden(true)
        .onChange(of: scannedCode, {
            showAlert = true
            let deviceList = scannedCode?.components(separatedBy: ";")
            deviceData = Device(sn: deviceList?[1],
                                host: deviceList?[2],
                                port: deviceList?[3],
                                account: deviceList?[5])
            viewModel.isQRScanPagePresented = false
            self.presentationMode.wrappedValue.dismiss()
        })
    }
    
    private func startAnimating() {
        withAnimation(Animation.linear(duration: 2.0).repeatForever(autoreverses: false)) {
            self.isAnimating = true
        }
    }
    
}

struct ScannerView: UIViewControllerRepresentable {
    @Binding var scannedCode: String?
    @Binding var isShowingScanner: Bool
    
    func makeCoordinator() -> Coordinator {
        return Coordinator(parent: self)
    }
    
    func makeUIViewController(context: Context) -> ScannerViewController {
        let scannerViewController = ScannerViewController()
        scannerViewController.delegate = context.coordinator
        return scannerViewController
    }
    
    func updateUIViewController(_ uiViewController: ScannerViewController, context: Context) {
        
    }
    
    class Coordinator: NSObject, ScannerViewControllerDelegate {
        let parent: ScannerView
        
        init(parent: ScannerView) {
            self.parent = parent
        }
        
        func didScan(_ code: String) {
            parent.scannedCode = code
            parent.isShowingScanner = false
        }
    }
    
}

protocol ScannerViewControllerDelegate: AnyObject {
    func didScan(_ code: String)
}

class ScannerViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {
    weak var delegate: ScannerViewControllerDelegate?
    var captureSession: AVCaptureSession!
    var previewLayer: AVCaptureVideoPreviewLayer!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .black
        captureSession = AVCaptureSession()
        
        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else { return }
        let videoInput: AVCaptureDeviceInput
        
        do {
            videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)
        } catch {
            return
        }
        
        if (captureSession.canAddInput(videoInput)) {
            captureSession.addInput(videoInput)
        } else {
            failed()
            return
        }
        
        let metadataOutput = AVCaptureMetadataOutput()
        
        if (captureSession.canAddOutput(metadataOutput)) {
            captureSession.addOutput(metadataOutput)
            
            metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            metadataOutput.metadataObjectTypes = [.qr]
        } else {
            failed()
            return
        }
        
        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)
        
        captureSession.startRunning()
    }
    
    func failed() {
        // Handle failure gracefully
        delegate?.didScan("")
        dismiss(animated: true)
    }
    
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        captureSession.stopRunning()
        
        if let metadataObject = metadataObjects.first {
            guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject,
                  let stringValue = readableObject.stringValue else { return }
            AudioServicesPlaySystemSound(SystemSoundID(kSystemSoundID_Vibrate))
            found(code: stringValue)
        }
        
        dismiss(animated: true)
    }
    
    func found(code: String) {
        delegate?.didScan(code)
    }
    
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .portrait
    }
}
