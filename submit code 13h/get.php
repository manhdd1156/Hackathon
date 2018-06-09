<?php
 
// array for JSON response
$response = array();
 
// include db connect class
require_once $_SERVER['DOCUMENT_ROOT'] . '\realtimeTest\db_connect.php';

 
// check for post data
if (isset($_GET["ownerPhoneNumber"])) {
    $ownerPhoneNumber = $_GET['ownerPhoneNumber'];

try {
    $stmt = $conn->prepare("SELECT parkinginfor.parkingID,parkinginfor.address,parkinginfor.space,currentspace.currentSpace FROM parkinginfor,ownerinfor,currentspace where currentspace.parkingID = parkinginfor.parkingID and ownerinfor.ownerID = parkinginfor.ownerID AND ownerinfor.phoneNumber = :ownerPhoneNumber and parkinginfor.flag_del=0"); 
    $stmt->bindValue(':ownerPhoneNumber',$ownerPhoneNumber);
	$stmt->execute();
	while($row=$stmt->fetch(PDO::FETCH_ASSOC)){
		
      $response['parkingInfor'][] = $row;
 
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