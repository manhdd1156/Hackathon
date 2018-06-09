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
if (isset($_POST["carID"])) {
    $carID     = $_POST['carID'];
    $parkingID = $_POST['parkingID'];
    
    
    try {
        $sql  = "INSERT INTO bookinginfor (carID, parkingID, status) VALUES (:carID,:parkingID,1)";
        $stmt = $conn->prepare($sql);
        $stmt->bindValue(':carID', $carID);
        $stmt->bindValue(':parkingID', $parkingID);
        $stmt->execute();
        $last_id = $conn->lastInsertId();
		$message = "OK";
        //"bookingID" => $lastInsertId
        $pusher->trigger("Fparking", "ORDER_FOR_BOOKING", array("carID"=>$carID,"bookingID" => $last_id,
            "message"=> $message,"action"=> "1"
        ));
        echo "New record created successfully. Last inserted ID is: " . $last_id;
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