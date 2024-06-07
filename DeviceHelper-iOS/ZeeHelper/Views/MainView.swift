//
//  ContentView.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/6.
//

import SwiftUI
import PushKit

struct MainView: View {
    @StateObject private var viewModel = VideoCallViewModel()
    @State private var isShowSettings = false
    @State private var deviceData: Device = Device()
    @State private var devices: [Device] = DatabaseManager.shared.queryDevice()
    @State private var targetHost = ""
    @State private var targetPort = ""
    @State private var webRTCClient: WebRTCClient?
    @ObservedObject var webSocket = FirstCallClient()
    var mobileClient = MobileClient()
    @EnvironmentObject var network: NetworkMonitor
    @State private var connectState = "一键直连"
    @State private var isShowAlert = false
    @State private var activeAlert: ActiveAlert = .norml
    
    var body: some View {
        NavigationStack {
            VStack {
                VStack(spacing: -10) {
                    HStack {
                        Spacer()
                        Text("紫星助手").font(.title2).padding(.leading, 50)
                        Spacer()
                        Image("icon_set").frame(width: 20, height: 20).padding(16).onTapGesture {
                            self.isShowSettings = true
                        }
                    }
                    
                    HStack {
                        Image(network.isConnectedToWiFi ? "icon_wifi" : "icon_nowifi")
                        Text(network.isConnectedToWiFi ? "已连接WIFI" : "未连接WIFI")
                            .foregroundStyle(Color(network.isConnectedToWiFi ? "#7972FE" : "#999999"))
                    }
                    
                    Image("image_scan_bg")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .padding(.top, 40).padding(.leading, 18).padding(.trailing, 18)
                        .onTapGesture {
                            if !network.isConnectedToWiFi {
                                showAlert(ActiveAlert.wifi)
                                return
                            }
                            viewModel.isQRScanPagePresented = true
                            viewModel.checkCameraAuthorizationStatus()
                        }
                    
                    if network.isConnectedToWiFi {
                        
                        HStack {
                            Text("已添加设备数：")
                            Text("\(devices.count)").padding(.leading, -10)
                            Spacer()
                        }.padding(.leading, 20).padding(.top, 20)
                        
                        if devices.count > 0 {
                            List {
                                ForEach(devices, id: \.self) { device in
                                    DeviceView(connectState: $connectState, sn: device.sn, ipAddress: device.host, account: device.account)
                                        .listRowSeparator(.hidden)
                                        .onTapGesture {
                                            self.targetHost = device.host!
                                            self.targetPort = device.port!
                                            viewModel.checkCameraAuthorizationStatus()
                                            if viewModel.isCameraAuthorized {
//                                                localNS.requestAuthorization { isAgree in
//                                                    if isAgree {
//                                                        self.connectState = "连接中..."
//                                                        outgoingCall()
//                                                        let url = "ws://\(self.targetHost):\(self.targetPort)"
//                                                        mobileClient.actionCode = 10003
//                                                        mobileClient.connect(to: url)
//                                                    } else {
//                                                        showAlert(ActiveAlert.lcoalNetwork)
//                                                    }
//                                                }
                                                self.connectState = "连接中..."
                                                outgoingCall()
                                                let url = "ws://\(self.targetHost):\(self.targetPort)"
                                                mobileClient.actionCode = 10003
                                                mobileClient.connect(to: url)
                                                
                                            } else if viewModel.isCameraDenied {
                                                let url = "ws://\(self.targetHost):\(self.targetPort)"
                                                mobileClient.actionCode = 10001
                                                mobileClient.connect(to: url)
                                                showAlert(ActiveAlert.camera)
                                            }
                                        }
                                    
                                }.onDelete(perform: { indexSet in
                                    indexSet.forEach { index in
                                        DatabaseManager.shared.deleteDevice(snCode: devices[index].sn!)
                                    }
                                    devices.remove(atOffsets: indexSet)
                                }).padding(.leading, -15).padding(.trailing, -15).padding(.top, -20).padding(.bottom, -20)
                                
                            }.listStyle(InsetListStyle()).padding(.top, 16)
                            
                        } else {
                            Spacer()
                            Image("image_empty").resizable().frame(width: 130, height: 130)
                            Text("当前无关联设备，请扫码添加设备").foregroundStyle(Color("#999999"))
                            Spacer()
                            Spacer()
                        }
                        
                    } else {
                        Spacer()
                        VStack{
                            Image("image_nowifi").resizable().frame(width: 130, height: 130)
                            Text("当前未连接WIFI").foregroundStyle(Color("#999999"))
                            Text("APP请与紫星设备连接同一WIFI").foregroundStyle(Color("#999999")).font(.footnote)
                            Button("设置局域网") {
                                if #available(iOS 15.4, *) {
                                    if let url = URL(string: "App-Prefs:root=WIFI") {
                                        if UIApplication.shared.canOpenURL(url) {
                                            UIApplication.shared.open(url, options: [:], completionHandler: nil)
                                        }
                                    }
                                } else {
                                    if let url = URL(string: UIApplication.openSettingsURLString) {
                                        if UIApplication.shared.canOpenURL(url) {
                                            UIApplication.shared.open(url, options: [:], completionHandler: nil)
                                        }
                                    }
                                }
                                
                            }.frame(width: 165, height: 34)
                                .background(Color("#B880FF")).foregroundColor(.white)
                                .cornerRadius(24)
                        }
                        Spacer()
                        Spacer()
                    }
                }
            }
            
            .navigationDestination(isPresented: $viewModel.isQRScanPagePresented) {
                QRCodeScannerView(deviceData: $deviceData)
            }
            
            .navigationDestination(isPresented: $viewModel.isVideoPagePresented) {
                VideoCallView(webRTCClient: webRTCClient)
            }
            
            .navigationDestination(isPresented: $isShowSettings) {
                SettingView()
            }
        }
        .onChange(of: viewModel.isQRScanPagePresented, {
            if !viewModel.isQRScanPagePresented {
                if deviceData.sn == nil {return}
                debugPrint("device info: \(deviceData.sn!); \(String(describing: deviceData.host))")
                let url = "ws://\(deviceData.host!):\(deviceData.port!)"
                mobileClient.actionCode = 10002
                mobileClient.connect(to: url)
                if !DatabaseManager.shared.isDeviceExists(targetSn: deviceData.sn!) {
                    DatabaseManager.shared.addDevice(deviceInfo: deviceData)
                    devices.append(deviceData)
                }
            }
        })
        .onChange(of: webSocket.connectionStatus) {
            if webSocket.connectionStatus == "Failed" {
                self.connectState = "一键直连"
                showAlert(ActiveAlert.connect_failed)
                webSocket.connectionStatus = "norml"
            }
        }
        .onChange(of: viewModel.isVideoPagePresented) {
            if !viewModel.isVideoPagePresented {
                self.connectState = "一键直连"
            }
        }
        .onChange(of: viewModel.isCameraDenied) {
            if viewModel.isCameraDenied {
                showAlert(ActiveAlert.camera)
            }
        }
        .alert(isPresented: $isShowAlert, content: {
            switch activeAlert {
            case .norml:
                Alert(title: Text("\n请进行WIFI连入后在尝试扫码\n\n"), dismissButton: .default(Text("确认")))
            case .wifi:
                Alert(title: Text("\n请进行WIFI连入后在尝试扫码\n\n"), dismissButton: .default(Text("确认")))
            case .lcoalNetwork:
                Alert(title: Text("请前往设置开启访问本地网络权限"), primaryButton: .cancel(Text("否")), secondaryButton: .default(Text("是"), action: {
                    if let appSettings = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(appSettings)
                    }
                }))
            case .disconnect:
                Alert(title: Text("当前手机摄像头与盒子已断开连接，请重新连接"), dismissButton: .default(Text("确认")))
            case .camera:
                Alert(title: Text("请前往设置开启访问相机权限"), primaryButton: .cancel(Text("否")), secondaryButton: .default(Text("是"), action: {
                    if let appSettings = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(appSettings)
                    }
                }))
            case .connect_failed:
                Alert(title: Text("当前设备连接失败请重新尝试"), message: Text("*确认设备与本机在同一局域网\n*重新扫码添加设备"),  dismissButton: .default(Text("确认")))
            }
        })
    }
    
    func showAlert(_ targetAlert: ActiveAlert) {
        self.isShowAlert = true
        self.activeAlert = targetAlert
    }
    
    func outgoingCall() {
        let url = "ws://\(self.targetHost):\(self.targetPort)"
        webSocket.connect(to: url)
        webSocket.receiveListener = self
    }
}

enum ActiveAlert {
    case norml, wifi, lcoalNetwork, disconnect, camera, connect_failed
}

extension MainView: ClientInterface {
    func receiveText(_ remoteAddress: String) {
        self.connectState = "连接成功"
        let url = "ws://\(self.targetHost):\(self.targetPort)"
        let taskID = UIApplication.shared.beginBackgroundTask(expirationHandler: nil)
        DispatchQueue.global().async {
            // 在此处添加采集摄像头数据的代码
            webRTCClient = WebRTCClient(remoteAddress: url)
            webRTCClient?.receiveListener = self
            UIApplication.shared.endBackgroundTask(taskID)
        }
        self.viewModel.isVideoPagePresented = true
    }
}

extension MainView: WebRTCInterface {
    func receiveText(_ state: Int) {
        debugPrint("connectState--->: \(state)")
        if state == 4 {
            showAlert(ActiveAlert.disconnect)
            self.viewModel.isVideoPagePresented = false
        }
    }
}

#Preview {
    MainView()
}
