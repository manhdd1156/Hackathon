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
if (isset($_GET["parkingID"])) {
    $parkingID = $_GET['parkingID'];

try {
    $stmt = $conn->prepare("SELECT COUNT(`bookingID`) AS NumberOfBooking FROM `bookinginfor` WHERE parkingID = :parkingID AND status = 3"); 
    $stmt->bindValue(':parkingID',$parkingID);
	$stmt->execute();
	while($row=$stmt->fetch(PDO::FETCH_ASSOC)){
		
      $response['numberCarInParking'][] = $row;
 
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