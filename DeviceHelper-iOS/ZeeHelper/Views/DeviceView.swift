//
//  DeviceView.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/9.
//

import SwiftUI

struct DeviceView: View {
    @State private var isShowingSheet = false
    @State private var title: String? = "紫星PRO终端机"
    @Binding var connectState: String
    
    
    let sn: String?
    let ipAddress: String?
    let account: String?
    
    var body: some View {
        VStack {
            HStack {
                Text("SN码：").font(.subheadline)
                Text(sn ?? "null").padding(.leading, -15).font(.subheadline)
                Spacer()
            }.padding(.leading, 13).padding(.top, 5)
            Divider().padding(.leading, 15).padding(.trailing, 15).padding(.top, -4).background(Color("#DDDDDD"))
            HStack {
                Image("icon_device").resizable().frame(width: 70, height: 70).padding(.leading, 15).padding(.trailing, 5)
                HStack {
                    VStack {
                        HStack {
                            Text(title ?? "紫星PRO终端机").font(.headline).onAppear() {
                                if sn?.prefix(4) == "1112" {
                                    title = "紫星PRO终端机"
                                } else {
                                    title = "紫星终端机"
                                }
                            }
                            Spacer()
                        }
                        HStack {
                            Text("设备IP：").foregroundColor(Color("#999999")).font(.footnote)
                            Text(ipAddress ?? "null").foregroundColor(Color("#999999"))
                                .font(.footnote).padding(.leading, -10)
                            Spacer()
                        }
                        HStack {
                            Text("账号：").foregroundColor(Color("#999999")).font(.footnote)
                            Text(account ?? "null").foregroundColor(Color("#999999"))
                                .font(.footnote).padding(.leading, -10)
                            Spacer()
                        }
                    }
                    Spacer()
                    Button(connectState) {

                    }.frame(width: 100, height: 40)
                     .background(Color("#B880FF")).foregroundColor(.white)
                     .cornerRadius(24).padding(.trailing, 15).padding(.top, 8)
                }.padding(.bottom, 10)
            }
            Spacer()
        }.frame(height: 120).background(Color("#F3F4F6")).cornerRadius(18).padding()
    }
}
