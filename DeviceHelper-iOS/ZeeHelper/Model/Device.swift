//
//  Device.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/13.
//

import Foundation
import WCDBSwift

struct Device: TableCodable, Hashable {
    var sn: String?
    var host: String?
    var port: String?
    var account: String?
    
    static let tableName = "Device"
    
    enum CodingKeys: String, CodingTableKey {
        typealias Root = Device
        static let objectRelationalMapping = TableBinding(CodingKeys.self)
        
        case sn
        case host
        case port
        case account
        
        static var columnConstraints: [CodingKeys:  BindColumnConstraint]? {
            return [
                sn: BindColumnConstraint(sn, isPrimary: true, defaultTo: "defaultDescription")
            ]
        }
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(sn)
    }

    // 实现 == 操作符
    static func == (lhs: Device, rhs: Device) -> Bool {
        return lhs.sn == rhs.sn
    }
    
    func get() -> String {
        return "Device Info: SN Code -> \(sn!) and Host -> \(host!) and Port -> \(port!) and Account -> \(account!)"
    }
}
