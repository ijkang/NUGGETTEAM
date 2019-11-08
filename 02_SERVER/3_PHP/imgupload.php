<?php
$data = $_POST["data1"]; //newImage란 값이 넘어옴

$file_path = $data."/"; // newImage밑을 의미하므로 /를 붙여줌

if(is_dir($data)){
	echo "폴더 존재 O"; // pass
} else {
	echo "폴더 존재 X";
	@chmod($data, 0777);
	@mkdir($data, 0777);


}

	// basename : 디렉토리명이 있다면, 그 부분을 제외하고 파일명만 출력, 즉 abc/def/ghi.jpg 면 ghi.jpg만 가져올 수 있음

	$file_name = basename( $_FILES['uploaded_file']['name']);
	$file_path = $file_path . $file_name;



	if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
		echo "file upload success";
		echo $file_path;
			} else{
		echo "file upload fail";
	}
?>
