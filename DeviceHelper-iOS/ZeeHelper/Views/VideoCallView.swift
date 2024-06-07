//
//  VideoCallView.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/6.
//

import SwiftUI
import WebRTC
import AVKit

struct VideoCallView: View {
    @StateObject private var viewModel = VideoCallViewModel()
    var webRTCClient: WebRTCClient?
    @Environment(\.presentationMode) var presentationMode
    @State private var offset = CGSize.zero
    @State private var isDragging = false
    @State private var isShowingSheet = false
    @State private var isPictureInPicture = false
    @State private var connectState = "一键直连"
    @State private var isShowFormat = false
    let defaults = UserDefaults.standard
    @State private var format = UserDefaults.standard.string(forKey: "format")
    
    var body: some View {
        
        GeometryReader { geometry in
            
            //            FloatingWindow(isPictureInPicture: $isPictureInPicture, videoTrack: webRTCClient?.localVideoTrack)
            
            VideoView(videoTrack: webRTCClient?.localVideoTrack)
                .aspectRatio(contentMode: .fill)
                .frame(width: geometry.size.width, height: geometry.size.height)
                .onDisappear {
                    webRTCClient?.cancal()
                    
                }
            
            ZStack {
                Image("icon_scan_bottom_bg").resizable()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .padding(.leading, -2).rotationEffect(.degrees(180))
                HStack {
                    
                    Image("icon_back")
                        .resizable().frame(width: 25, height: 25)
                        .onTapGesture {
                            viewModel.isVideoPagePresented = false
                            self.presentationMode.wrappedValue.dismiss()
                        }.padding(.leading, 16)
                    
                    //                    Image("icon_floating")
                    //                        .resizable().frame(width: 25, height: 25)
                    //                        .onTapGesture {
                    //                            self.isPictureInPicture.toggle()
                    //
                    //                        }.padding(.leading, 16)
                    
                    Spacer()
                    Spacer()
                    Text("摄像头启用中").font(.title2).foregroundColor(.white).padding(.leading, 20)
                    Spacer()
                    
                    Image("icon_format").resizable()
                        .frame(width: 28, height: 28).padding(.trailing, 16).padding(.top, 2).onTapGesture {
                            isShowFormat.toggle()
                        }
                    
                    Image("icon_switch").resizable()
                        .frame(width: 30, height: 30)
                        .padding(.trailing, 16).onTapGesture {
                            webRTCClient?.switchCamera()
                        }
                }.padding(.top, 30)
                
            }.frame(maxWidth: .infinity, maxHeight: 90)
            
            VStack {
                if isShowFormat {
                    HStack {
                        Spacer()
                        ZStack {
                            Image("icon_box").resizable().frame(width: 68, height: 66).padding(.top, 78).padding(.trailing, 50)
                            VStack(spacing: 4) {
                                Text("360P").foregroundStyle(Color(format == "360P" ? "#7972FE" : "#999999")).onTapGesture {
                                    if format != "360P" {
                                        format = "360P"
                                        defaults.set("360P", forKey: "format")
                                        webRTCClient?.changeVideoFormat(to: .H360)
                                    }
                                }
                                Divider().frame(width: 60).background(Color("#DDDDDD"))
                                Text("720P").foregroundStyle(Color(format == "720P" ? "#7972FE" : "#999999")).onTapGesture {
                                    if format != "720P" {
                                        format = "720P"
                                        defaults.set("720P", forKey: "format")
                                        webRTCClient?.changeVideoFormat(to: .H720)
                                    }
                                }
                            }.padding(.trailing, 50).padding(.top, 82)
                        }
                    }.onAppear {

                    }
                }
                Spacer()
                ZStack {
                    Image("icon_scan_bottom_bg").resizable()
                        .frame(maxWidth: .infinity, maxHeight: 172)
                        .padding(.leading, -2)
                    VStack {
                        Text("摄像头使用注意事项：").font(.title3)
                            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading).foregroundColor(.white)
                        Text("1、当前使用的是手机前置摄像头，请保持手机唤醒状态；").font(.subheadline)
                            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading).foregroundColor(.white)
                        Text("2、请保证您的上半身处于取景框内；").font(.subheadline)
                            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading).foregroundColor(.white)
                        Text("3、最佳的取景距离为1.5-2.5米").font(.subheadline)
                            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading).foregroundColor(.white)
                        Text("4、请确认当前“把视互动”应用内出现您的任务形象").font(.subheadline)
                            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading).foregroundColor(.white)
                        
                    }.padding(.leading, 10).padding(.bottom, 40)
                }
            }
        }
        .edgesIgnoringSafeArea(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
        .navigationBarHidden(true)
    }
}
