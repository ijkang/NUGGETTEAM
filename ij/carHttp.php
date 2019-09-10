<?php
header('Content-Type: text/html; charset=ms949');
#!/bin/bash

$fname = null;
$lname = null;
if(isset($_GET['get'])) {
  echo date("Y-m-d H:i:s");
 }

else if(isset($_GET['post'])) {
$val1 = 'param';
// echo shell_exec("python carnum.py");
   $fname = $_POST['first_name'];
    $lname = $_POST['last_name'];

// exec("export LANG=\"ko_KR.utf8\";locale";);
exec("PYTHONIOENCODING=utf-8 python3 car_non.py 2>&1  $val1 $fname $lname", $output, $return);

print_r($return);
print_r($output);

 
if (!$return) {
    echo "Successfully";
    echo "<br>";
//    foreach ($output as $line) {
//        echo "$line\n";
//    }
  print_r($output);
print_r($output);
} else {
    echo "fail";
}   
} 
?>