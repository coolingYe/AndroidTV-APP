//
//  SettingView.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/10.
//

import SwiftUI

struct SettingView: View {
    var body: some View {
        Text("紫星助手").font(.title2)
        
        Image("icon_app")
            .resizable()
            .aspectRatio(contentMode: .fit).frame(width: 85, height: 85).padding(.top, 60)
        
        Text("紫星助手").font(.title2).padding(.top, 25)
        
        Text("V1.0.0").foregroundStyle(Color("#999999"))
        
        List {
            HStack {
                Text("检测更新")
                Spacer()
                Image("icon_more")
            }.frame(height: 37)
            
            HStack {
                Text("用户隐私协议")
                Spacer()
                Image("icon_more")
            }.frame(height: 37)
            
            HStack {
                Text("用户使用手册")
                Spacer()
                Image("icon_more")
            }.frame(height: 37)
        }.listStyle(InsetListStyle()).padding(.top, 40)
    }
}

#Preview {
    SettingView()
}
