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
if (isset($_POST["parkingID"])) {
    $parkingID = $_POST['parkingID'];
    $actionspace = $_POST['actionspace'];
	$space = $_POST['space'];
    try {
        $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        if ($actionspace == "Inc") {
            $sql = "UPDATE currentspace SET currentspace=currentspace + 1 WHERE parkingID=:parkingID";
           
        } else if ($actionspace == "Desc") {
            $sql = "UPDATE currentspace SET currentspace=currentspace - 1 WHERE parkingID=:parkingID";
        } else if($actionspace == "Manul") {
			$sql = "UPDATE currentspace SET currentspace= :space WHERE parkingID=:parkingID";
			
		}
		
        $stmt = $conn->prepare($sql);
		
        $stmt->bindValue(':parkingID', $parkingID);
		if($actionspace == "Manul") {
		$stmt->bindValue(':space', $space);
		}
		$stmt->execute();
        echo "success";
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