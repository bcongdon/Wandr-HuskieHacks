//
//  MyMKMapViewDelegate.swift
//  Wandr
//
//  Created by Benjamin Congdon on 9/26/15.
//  Copyright © 2015 Benjamin Congdon. All rights reserved.
//

import MapKit

class MyMKMapViewDelegate: MKMapView {
    func mapView(mapView: MKMapView!, renderForOverlay overlay: MKOverlay!) ->MKOverlayRenderer! {
        NSLog("Called")
        if (overlay is MKPolyline) {
            let pr = MKPolylineRenderer(overlay: overlay);
            pr.strokeColor = UIColor.blueColor().colorWithAlphaComponent(0.5);
            pr.lineWidth = 5;
            return pr;
        }
        return nil
    }
}