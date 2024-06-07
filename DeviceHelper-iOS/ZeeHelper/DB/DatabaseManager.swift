//
//  DBManage.swift
//  ZeeHelper
//
//  Created by yeah on 2024/5/13.
//

import Foundation
import WCDBSwift

class DatabaseManager {
    static let shared = DatabaseManager()
    
    private let database: Database
    private let databasePath: String
    
    private init() {
        // 配置数据库路径
        let documentsPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first!
        databasePath = documentsPath + "/zeeDatabase.db"
        
        // 创建数据库对象
        database = Database(at: databasePath)
        
        // 打开数据库连接
        do {
            try database.create(table: Device.tableName, of: Device.self)
            debugPrint("Database initialized successfully.")
        } catch let error {
            debugPrint("Failed to initialize database: \(error)")
        }
    }
    
    func getDatabase() -> Database {
        return database
    }
}

extension DatabaseManager {
    
    func addDevice(deviceInfo: Device) {
        let targetDevice = deviceInfo
        
        let db = DatabaseManager.shared.getDatabase()
        
        do {
            try db.insert(targetDevice, intoTable: Device.tableName)
            debugPrint("Inserted device: \(targetDevice)")
        } catch let error {
            debugPrint("Failed to insert device: \(error)")
        }
    }
    
    func queryDevice() -> [Device] {
        let db = DatabaseManager.shared.getDatabase()
        
        do {
            let devices: [Device] = try db.getObjects(fromTable: Device.tableName)
            return devices
//            for device in devices {
//                debugPrint("Device - SN: \(device.sn), Address: \(device.address), Account: \(device.account)")
//            }
        } catch let error {
            print("Failed to fetch devices: \(error)")
            return []
        }
    }
    
    func updateDevice(snCode: String, targetDevice: Device) {
        let db = DatabaseManager.shared.getDatabase()
        let condition = Column(named: "sn") == snCode
        
        do {
            try db.update(table: Device.tableName, on: [Device.Properties.sn, Device.Properties.host, Device.Properties.port, Device.Properties.account], with: targetDevice, where: condition)
            debugPrint("Device updated successfully.")
        } catch let error {
            debugPrint("Failed to update device: \(error)")
        }
    }
    
    func deleteDevice(snCode: String) {
        let db = DatabaseManager.shared.getDatabase()
        let condition = Column(named: "sn") == snCode
        
        do {
            try db.delete(fromTable: Device.tableName, where: condition)
            debugPrint("Device deleted successfully.")
        } catch let error {
            debugPrint("Failed to delete device: \(error)")
        }
    }
    
    func isDeviceExists(targetSn: String) -> Bool {
        let db = DatabaseManager.shared.getDatabase()
        
        do {
            let condition = Device.Properties.sn == targetSn
            let localSn: Int = try db.getValue(on: Column.all().count(), fromTable: Device.tableName, where: condition).intValue
            return localSn > 0
        } catch let error {
            debugPrint("Failed to check device existence: \(error)")
            return false
        }
    }
}
