
$(document).ready(function (){
   init();
});

var init = function () {

}
function setImage(path) {
    var img = document.createElement("IMG");
    img.setAttribute("src", path);
    alert(path);
    document.body.appendChild(img);

    /*$("#imagePath").text(tag);
    $("#imageDiv").append(tag);*/
}

function cameraButton(){
 Android.takePicture();
}

function clickTest() {
 alert("자바스크립트 실행");
}