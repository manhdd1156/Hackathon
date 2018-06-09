<?php

/*
 * Following code will get single product details
 * A product is identified by product id (pid)
 */

// array for JSON response
$response = array();

// include db connect class
require_once $_SERVER['DOCUMENT_ROOT'] . '\realtimeTest\db_connect.php';


// check for post data
if (isset($_GET["carID"]) || isset($_GET["bookingID"])) {
    $carID = $_GET['carID'];
    
    
    $bookingID = $_GET['bookingID'];
    
    try {
        if ($carID != null) {
            $stmt = $conn->prepare("SELECT * FROM `carofdriver` WHERE carID=:carID");
            $stmt->bindValue(':carID', $carID);
        } else {
            $stmt = $conn->prepare("SELECT bookinginfor.bookingID,bookinginfor.status,carofdriver.licensePlate,typecar.type,bookinginfor.checkinTime,bookinginfor.checkoutTime,parkingprice.price FROM `bookinginfor`,`carofdriver`,`driver`,`typecar`,`parkingprice` WHERE bookinginfor.carID = carofdriver.carID AND driver.driverID = carofdriver.driverID AND typecar.typeCarID = carofdriver.typeCarID AND parkingprice.parkingID = bookinginfor.parkingID AND parkingprice.typeCarID = typecar.typeCarID  AND bookinginfor.bookingID = :bookingID");
            $stmt->bindValue(':bookingID', $bookingID);
        }
        $stmt->execute();
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            
            $response['result'][] = $row;
            
        }
        echo json_encode($response);
        
    }
    catch (PDOException $e) {
        echo "Error: " . $e->getMessage();
    }
    
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
    
    //echoing JSON response
    echo json_encode($response);
}
?>