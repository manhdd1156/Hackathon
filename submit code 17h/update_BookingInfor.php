<?php

/*
 * Following code will get single product details
 * A product is identified by product id (pid)
 */

require_once $_SERVER['DOCUMENT_ROOT'] . '\realtimeTest\Pusher\Pusher.php';
require_once $_SERVER['DOCUMENT_ROOT'] . '\realtimeTest\config.php';
$pusher = new Pusher\Pusher($pusherConfig['key'], $pusherConfig['secret'], $pusherConfig['app_id'], $pusherOptions);


// array for JSON response
$response = array();

// include db connect class
require_once $_SERVER['DOCUMENT_ROOT'] . '\realtimeTest\db_connect.php';

$inputJSON = file_get_contents('php://input');
$_POST     = json_decode($inputJSON, TRUE);


// check for post data
if (isset($_POST["bookingID"]) && isset($_POST["status"]) && isset($_POST["actioncheck"])) {
    $bookingID   = $_POST['bookingID'];
    $status      = $_POST['status'];
    $checkinTime = $_POST['checkinTime'];
	$checkoutTime = $_POST['checkoutTime'];
	$licensePlate = $_POST['licensePlate'];
	$type = $_POST['type'];
	$carID = $_POST['carID'];
	$price = $_POST['price'];
	$hours = $_POST['hours'];
	$totalPrice = $_POST['totalPrice'];
	$actioncheck = $_POST['actioncheck'];
    try {
        $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		if($actioncheck == "cancel") {
		$pusher->trigger($PUSHER_CHANNEL, $PUSHER_EVENT_CANCEL_FOR_BOOKING, array(
            "carID" => $carID,"message" => "CANCEL","bookingID" => $bookingID,"action"=>"0"));
		}else if($actioncheck == "checkin") {
			$sql = "UPDATE bookinginfor SET status=:status,checkinTime=:checkinTime WHERE bookingID=:bookingID";
			$stmt = $conn->prepare($sql);
			$stmt->bindValue(':checkinTime', $checkinTime);
			$stmt->bindValue(':status', $status);
        
        	$stmt->bindValue(':bookingID', $bookingID);
        	$stmt->execute();
			$message = "OK";
			$pusher->trigger($PUSHER_CHANNEL, $PUSHER_EVENT_CHECKIN_FOR_BOOKING, array(
            "carID" => $carID,"message" => $message,"bookingID" => $bookingID,"action"=>"2"
        ));
		}else if($actioncheck == "checkout"){
			$sql = "UPDATE bookinginfor SET status=:status,checkoutTime=:checkoutTime WHERE bookingID=:bookingID";
			$stmt = $conn->prepare($sql);
			$stmt->bindValue(':checkoutTime', $checkoutTime);
		
        
        $stmt->bindValue(':status', $status);
        
        $stmt->bindValue(':bookingID', $bookingID);
        $stmt->execute();
        $pusher->trigger($PUSHER_CHANNEL, "CHECKOUT_FOR_BOOKING", array(
            "licensePlate" => $licensePlate,"type" => $type,"checkinTime" => $checkinTime,"checkoutTime" => $checkoutTime,"price" => $price,"hours" => $hours,"totalPrice" => $totalPrice,"action"=> "3","carID"=>$carID,"bookingID"=>$bookingID,"message"=>"OK"
        ));
        }
        echo "OK";
    }
    catch (PDOException $e) {
        echo "Error php: " . $e->getMessage();
    }
    
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
    
    //echoing JSON response
    
}
?>