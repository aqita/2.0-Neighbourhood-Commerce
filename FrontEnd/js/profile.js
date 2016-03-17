function password(){
            var oldpass = $('#oldpass').val();
            var newpass = $('#newpass').val();
            var conpass = $('#conpass').val();
            var useid = getCookie("userId");
            if(conpass == newpass && newpass !== ""){
               $.get( "http://52.76.118.161:7070/api/v1/updatePassword",{userid:useid, oldpassword:oldpass, newpassword:newpass}, function( data ) {
                      data2 = JSON.parse(data);
                      
                          var json_data = JSON.parse(data);
                          document.cookie = json_data.storeId;
                          if(data == "true"){
                            $("#error2").html("Passwords changed");
                            $('#oldpass').val("");
                            $('#newpass').val("");
                            $('#conpass').val("");
                          }
                          else{
                            $("#error2").html("Old password incorrect");
                          }
                  });
            }
            else{
              $("#error2").html("Passwords dont match");
            }
        }
        
        function info(){
            var nam = $('#name').val();
            var phon = $('#phone').val(); 
            var add1 = $('#add1').val();
            var add2 = $('#add2').val();
            var lan = $('#landmark').val();
            var useid = getCookie("userId");
            if(nam && phon && add1 && add2){
               $.get( "http://52.76.118.161:7070/api/v1/updateUser",{userid:useid, name:nam, phone:phon, addressline1:add1, addressline2:add2,landmark:lan }, function( data ) {
                      if(data == "true"){
                          $("#error").html("Details Updated");
                          setCookie("name", nam, 30);
                          setCookie("phone", phon, 30);
                          setCookie("address1", add1, 30);
                          setCookie("address2", add2, 30);
                          setCookie("landmark", lan, 30);
                      }
                      else{
                        $("#error").html("Something Went wrong we are working on it");
                      }
                  });
            }
            else{
              $("#error2").html("Please enter all fields");
            }
        }
$(document).ready(function() {
  $.ripple(".btn", {
    debug: false,
    on: 'mousedown',
    opacity: 0.4, 
    color: "auto",
    multi: false,
    duration: 0.7,
    rate: function(pxPerSecond) {
        return pxPerSecond;
    },

    easing: 'linear'
});
  if(getCookie("name")=="undefined"){
    
  }else{ $("#name").val(getCookie("name"));
  }
  if(getCookie("phone")=="undefined"){
    
  }else{ $("#phone").val(getCookie("phone"));
  }
  if(getCookie("address1")=="undefined"){
    
  }else{ $("#add1").val(getCookie("address1"));
  }
  if(getCookie("address2")=="undefined"){
    
  }else{ $("#add2").val(getCookie("address2"));
  }
  if(getCookie("landmark")=="undefined"){
    
  }else{  $("#landmark").val(getCookie("landmark"));
  }
  if(!getCookie("userId")){
      window.location.replace("login.html");
    }

 });