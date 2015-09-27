//
//  SecondViewController.swift
//  Wandr
//
//  Created by Benjamin Congdon on 9/26/15.
//  Copyright © 2015 Benjamin Congdon. All rights reserved.
//

import UIKit
import CoreLocation
import AudioToolbox

class SecondViewController: UIViewController,CLLocationManagerDelegate {
    var lastKnownLocation: CLLocation!
    var manager: CLLocationManager!
    @IBOutlet weak var textField: UITextField!
    @IBOutlet weak var phoneField: UITextField!
    

    
    @IBAction func setHome(sender: AnyObject){
        manager?.requestWhenInUseAuthorization()
        manager?.startUpdatingLocation()
        
        
        
    }
    
    func setHomeLocation(){
        if(lastKnownLocation != nil){
            NSLog(lastKnownLocation.description)
            
            
            let defaults = NSUserDefaults.standardUserDefaults()
            defaults.setDouble(lastKnownLocation.coordinate.latitude, forKey: "HomeLatitude")
            defaults.setDouble(lastKnownLocation.coordinate.longitude, forKey: "HomeLongitude")
            lastKnownLocation = nil
        }
    }
    
    @IBAction func radiusChanged(sender: AnyObject){
        NSLog(textField.text!)
        let defaults = NSUserDefaults.standardUserDefaults()
        defaults.setDouble(CFStringGetDoubleValue(textField.text!), forKey: "HomeRadius")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        manager = CLLocationManager()
        manager.delegate = self
        manager?.requestWhenInUseAuthorization()
        manager?.startUpdatingLocation()
        
        
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func touchesBegan(touches: Set<UITouch>, withEvent event: UIEvent?) {
        textField.resignFirstResponder()
        phoneField.resignFirstResponder()
    }
    
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]){
        manager.stopUpdatingLocation()
        lastKnownLocation = locations[0]
        setHomeLocation()
    }

}

