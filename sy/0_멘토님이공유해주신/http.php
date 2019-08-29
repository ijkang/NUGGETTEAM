<?php

//echo "test for get and post";

if(isset($_GET['get'])) {
  //echo date("Y-m-d H:i:s");
  echo "book url~~~~~~~~~~~";
}
else if(isset($_GET['post'])) {
  // reading post value
  $fname = $_POST['first_name'];
  $lname = $_POST['last_name'];
  echo "Your name is " .$fname." ".$lname;
}

?>
