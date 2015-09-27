//
//  FirstViewController.swift
//  Wandr
//
//  Created by Benjamin Congdon on 9/26/15.
//  Copyright © 2015 Benjamin Congdon. All rights reserved.
//

import UIKit
import CoreLocation
import MapKit
import AudioToolbox.AudioServices

class FirstViewController: UIViewController, CLLocationManagerDelegate, MKMapViewDelegate {
    var manager: CLLocationManager!
    var homeLocation: CLLocation!
    var monitoringLocation: Bool!
    var lastKnownLocation: CLLocation!
    

    
    @IBOutlet weak var mapView: MKMapView!
    @IBOutlet weak var address: UILabel!
    @IBOutlet weak var switchMonitor: UISwitch!
    
    override func viewWillAppear(animated: Bool) {
        super.viewDidLoad()
        manager = CLLocationManager()
        manager.delegate = self
        manager?.requestWhenInUseAuthorization()
        manager?.startUpdatingLocation()
        
        let defaults = NSUserDefaults.standardUserDefaults()
        let homeLatitude = defaults.doubleForKey("HomeLatitude")
        let homeLongitude = defaults.doubleForKey("HomeLongitude")
        let location = CLLocation(latitude: homeLatitude, longitude: homeLongitude)
        homeLocation = location
        
        regionMonitoring(homeLocation)
    }
    

    override func viewDidLoad() {
        setMapOverlay()

    }
    
    func setMapOverlay(){
        self.mapView.delegate = self
        let defaults = NSUserDefaults.standardUserDefaults()
        let homeLatitude = defaults.doubleForKey("HomeLatitude")
        let homeLongitude = defaults.doubleForKey("HomeLongitude")
        let location = CLLocation(latitude: homeLatitude, longitude: homeLongitude)
        let homeRadius = defaults.doubleForKey("HomeRadius")

        homeLocation = location

        if(homeLocation != nil){
            let circle = MKCircle(centerCoordinate: homeLocation.coordinate, radius: homeRadius as CLLocationDistance)
            let overlays = self.mapView.overlays
            self.mapView.removeOverlays(overlays)
            self.mapView.addOverlay(circle)
        }
        
    }

    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    @IBAction func getLocation(sender: AnyObject){
        //Set Home Location
        manager?.requestWhenInUseAuthorization()
        manager?.startUpdatingLocation()

    }
    @IBAction func regionMonitoring(sender: AnyObject){
        manager?.requestAlwaysAuthorization()
        let defaults = NSUserDefaults.standardUserDefaults()
        let homeRadius = defaults.doubleForKey("HomeRadius")
        
        let currRegion = CLCircularRegion(center: homeLocation.coordinate, radius: homeRadius, identifier: "home")
        if(switchMonitor.on){
            manager?.startMonitoringForRegion(currRegion)
            NSLog("region registered")
            setMapOverlay()

        }
        else{
            let regions = manager?.monitoredRegions
            if(regions?.count > 0){
                manager?.stopMonitoringForRegion((regions?.first)!)
                let overlays = self.mapView.overlays
                self.mapView.removeOverlays(overlays)
            }
            NSLog("region unregisterd")
        }
    }
    
    func locationManager(manager: CLLocationManager, didUpdateLocations locations: [CLLocation]){
        manager.stopUpdatingLocation()
        let location = locations[0]
        lastKnownLocation = location
        let geoCoder = CLGeocoder()
        geoCoder.reverseGeocodeLocation(location, completionHandler: {(data,error) -> Void in
            let placeMarks = data ?? []
            let loc: CLPlacemark = placeMarks[0]
            
            self.mapView.centerCoordinate = location.coordinate
            let addr = loc.locality
            self.address.text = addr
            let reg = MKCoordinateRegionMakeWithDistance(location.coordinate, 1500, 1500)
            self.mapView.setRegion(reg, animated: true)
            self.mapView.showsUserLocation = true
            
        })
    }
    @IBAction func monitoringChanged(sender: AnyObject!){
        regionMonitoring(switchMonitor)

    }
    func locationManager(manager: CLLocationManager!, didEnterRegion region:CLRegion!){
        NSLog("enter region")
        
    }
    func locationManager(manager: CLLocationManager, didExitRegion region: CLRegion!){
        NSLog("exit region")
        AudioServicesPlayAlertSound(kSystemSoundID_Vibrate);

    }
    func locationManager(manager: CLLocationManager, didFailWithError error: NSError!){
        NSLog(error.description);

    }
    
    func mapView(mapView: MKMapView!, rendererForOverlay overlay: MKOverlay!) -> MKOverlayRenderer! {
        if overlay is MKCircle {
            var circle = MKCircleRenderer(overlay: overlay)
            circle.strokeColor = UIColor.redColor()
            circle.fillColor = UIColor(red: 255, green: 0, blue: 0, alpha: 0.1)
            circle.lineWidth = 1
            return circle
        } else {
            return nil
        }
    }
}

