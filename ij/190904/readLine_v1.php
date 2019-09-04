<?php
 header('Content-Type: text/html; charset=UTF-8');
#!/bin/bash
$val1 = 'param';
// echo shell_exec("python car_non.py");

exec("python3 car_non.py 2>&1  $val1, 'param2'", $output, $return);

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
?>