<?php
 
$response = array();

// include db connect class
require_once $_SERVER['DOCUMENT_ROOT'] . '\realtimeTest\db_connect.php';


// check for post data
if (isset($_GET["parkingID"])) {
    $parkingID = $_GET['parkingID'];

try {
    $stmt = $conn->prepare("SELECT bookinginfor.bookingID,bookinginfor.status,carofdriver.licensePlate,typecar.type,bookinginfor.checkinTime,bookinginfor.checkoutTime,parkingprice.price FROM `bookinginfor`,`carofdriver`,`driver`,`typecar`,`parkingprice` WHERE bookinginfor.carID = carofdriver.carID AND driver.driverID = carofdriver.driverID AND typecar.typeCarID = carofdriver.typeCarID AND parkingprice.parkingID = :parkingID AND parkingprice.typeCarID = typecar.typeCarID  AND bookinginfor.parkingID = :parkingID"); 
    $stmt->bindValue(':parkingID',$parkingID);
	$stmt->execute();
	while($row=$stmt->fetch(PDO::FETCH_ASSOC)){
		
      $response['cars'][] = $row;
	}
	echo json_encode($response);

}
catch(PDOException $e) {
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