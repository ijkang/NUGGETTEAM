<?php
header('Content-Type: text/html; charset=utf-8');

$path = null;
$response = "not yet";
$output = "0000";
$return = 0;
 if(isset($_GET['get'])) {
  echo date("Y-m-d H:i:s");
 
}
else if(isset($_GET['post'])) {
//   reading post value
  $path = $_POST['path'];
  // echo $path;
  exec("PYTHONIOENCODING=utf-8 python3 car_non.py 2>&1 $path", $output, $return);
   // todo if output의 길이가 10자 이상일 경우 다시 촬영해 주세요 출력 
  echo $output[0];
  // echo $return[0];
}
else if(isset($_GET['getcarnum'])) {
  exec("PYTHONIOENCODING=utf-8 python3 car_non.py 2>&1", $output, $return);
  // echo $output[0];
    
}
else if(isset($_GET['getcarnumtest'])) {
	echo "12媛�3456";
}
?>
